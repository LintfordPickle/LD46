package net.lintford.ld46.renderers;

import net.lintford.ld46.controllers.GameStateController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class GameStateRenderer extends BaseRenderer {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "GameState Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private GameStateController mGameStateController;

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

	}

	@Override
	public void draw(LintfordCore pCore) {
		final var lHudBoundingBox = pCore.HUD();

		final var lTextureBatch = mRendererManager.uiTextureBatch();
		final var lFontUnit = mRendererManager.textFont();

		lTextureBatch.begin(pCore.HUD());
		lTextureBatch.end();

		float lLinePosY = 5.f;
		final float lLineHeight = 20.f;
		final float lLinePosOffsetY = lHudBoundingBox.getMinY();

		lFontUnit.begin(pCore.HUD());
		lFontUnit.draw("Current Lap:", lHudBoundingBox.getMinX() + 5.f, lLinePosOffsetY + (lLinePosY), 1.f);
		lFontUnit.draw("Current Position:", lHudBoundingBox.getMinX() + 5.f, lHudBoundingBox.getMinY() + (lLinePosY += lLineHeight), 1.f);

		lFontUnit.end();

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

}
