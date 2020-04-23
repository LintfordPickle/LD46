package net.lintford.ld46.renderers;

import org.lwjgl.opengl.GL11;

import net.lintford.ld46.controllers.CarController;
import net.lintford.ld46.controllers.GameStateController;
import net.lintford.ld46.controllers.TelekinesisController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.fonts.FontManager.FontUnit;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.renderers.RendererManager;
import net.lintford.library.renderers.windows.UIWindow;

public class GameStateRenderer extends UIWindow {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "GameState Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private CarController mCarController;
	private TelekinesisController mTelekinesisController;
	private GameStateController mGameStateController;

	private FontUnit mHudFontUnit;

	private Texture mUiTexture;
	private Texture mPanelsTexture;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return mGameStateController != null;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameStateRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		final var lControllerManager = pCore.controllerManager();

		mGameStateController = (GameStateController) lControllerManager.getControllerByNameRequired(GameStateController.CONTROLLER_NAME, entityGroupID());
		mCarController = (CarController) lControllerManager.getControllerByNameRequired(CarController.CONTROLLER_NAME, entityGroupID());
		mTelekinesisController = (TelekinesisController) lControllerManager.getControllerByNameRequired(TelekinesisController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mHudFontUnit = pResourceManager.fontManager().loadNewFont("FONT_HUD", "res/fonts/fontPixeled.ttf", 16, true);
		mHudFontUnit.loadGLContent(pResourceManager);

		// TODO: Subpixel sampling
		mPanelsTexture = pResourceManager.textureManager().loadTexture("TEXTURE_PANELS", "res/textures/textureGamePanel.png", GL11.GL_NEAREST, entityGroupID());
		mUiTexture = pResourceManager.textureManager().loadTexture("TEXTURE_UI", "res/textures/textureUI.png", GL11.GL_NEAREST, entityGroupID());

	}

	@Override
	public void draw(LintfordCore pCore) {
		final var lHudBoundingBox = pCore.HUD();

		final var lTextureBatch = mRendererManager.uiTextureBatch();

		lTextureBatch.begin(pCore.HUD());
		// Panels
		lTextureBatch.draw(mPanelsTexture, 0, 0, 128, 64, -lHudBoundingBox.getWidth() * 0.5f, lHudBoundingBox.getHeight() * 0.5f - 128, 128 * 2f, 64 * 2f, -0.001f, 1f, 1f, 1f, 1f);

		// Telekinesis bar
		lTextureBatch.draw(mUiTexture, 2, 95, 119, 19, -lHudBoundingBox.getWidth() * 0.5f + 256f + 10f, lHudBoundingBox.getHeight() * 0.5f - 10f - 40, 119f * 2f, 19f * 2f, -0.001f, 1f, 1f, 1f, 1f);

		final float lCurrentTele = mTelekinesisController.currentTelekinesisPower();
		final float lMaxTele = 100f;
		float lWidthT = MathHelper.scaleToRange(lCurrentTele, 0.f, lMaxTele, 0.f, 119);
		lTextureBatch.draw(mUiTexture, 2, 121, lWidthT, 19, -lHudBoundingBox.getWidth() * 0.5f + 256f + 10f, lHudBoundingBox.getHeight() * 0.5f - 10f - 40, lWidthT * 2f, 19f * 2f, -0.001f, 1f, 1f, 1f, 1f);

		// Speed bar
		lTextureBatch.draw(mUiTexture, 1, 53, 124, 34, lHudBoundingBox.getWidth() * 0.5f - 124f * 2f - 10f, lHudBoundingBox.getHeight() * 0.5f - 10f - 34f * 2f, 124f * 2f, 34f * 2f, -0.001f, 1f, 1f, 1f, 1f);

		final float lCurrentSpeed = mCarController.carManager().playerCar().currentSpeed();
		final float lMaxSpeed = 100f;
		float lWidth = MathHelper.scaleToRange(lCurrentSpeed, 0.f, lMaxSpeed, 0.f, 124);
		lTextureBatch.draw(mUiTexture, 1, 16, lWidth, 34, lHudBoundingBox.getWidth() * 0.5f - 124f * 2f - 10f, lHudBoundingBox.getHeight() * 0.5f - 10f - 34f * 2f, lWidth * 2f, 34f * 2f, -0.001f, 1f, 1f, 1f, 1f);
		lTextureBatch.end();

		float lLinePosY = 35.f;
		final float lLineHeight = 35.f;
		final float lLinePosOffsetY = -lHudBoundingBox.getMinY() * 0.5f;

		final var lPlayerCar = mCarController.carManager().playerCar();
		final var lPlayerCarProgress = lPlayerCar.carProgress();

		{ // countdown timer
			if (!mGameStateController.mHasRaceStarted) {
				final var lTitleFont = mRendererManager.titleFont();

				float lScale = 2.f + (MathHelper.clamp(mGameStateController.mCountDownTimer * 0.001f, 0.f, 1.f));
				final String lText = mGameStateController.mStartCountDown + "";
				final float lTextWidth = lTitleFont.bitmap().getStringWidth(lText, lScale);
				final float lTextHeight = lTitleFont.bitmap().getStringWidth(lText, lScale);

				lTitleFont.begin(pCore.HUD());
				lTitleFont.draw(lText, 0 - lTextWidth * 0.5f, 0 - lTextHeight * 0.5f - 240f, -0.1f, 1, 1, 1, 1, lScale, -1);
				lTitleFont.end();

			}
		}

		mHudFontUnit.begin(pCore.HUD());
		mHudFontUnit.draw("Laps:  ", lHudBoundingBox.getMinX() + 5.f, lLinePosOffsetY + (lLinePosY), 1.f);
		mHudFontUnit.draw((lPlayerCarProgress.currentLapNumber) + "/" + ((int)MathHelper.clampi(mGameStateController.totalLaps(), 0, GameStateController.numLaps)), lHudBoundingBox.getMinX() + 5.f + 160f, lLinePosOffsetY + (lLinePosY), 1.f);
		mHudFontUnit.draw("Position:  ", lHudBoundingBox.getMinX() + 5.f, lLinePosOffsetY + (lLinePosY += lLineHeight), 1.f);
		mHudFontUnit.draw(mGameStateController.getPlayerPosition() + "/" + mGameStateController.totalRacers(), lHudBoundingBox.getMinX() + 5.f + 160f, lLinePosOffsetY + (lLinePosY), 1.f);

		mHudFontUnit.end();

	}

}
