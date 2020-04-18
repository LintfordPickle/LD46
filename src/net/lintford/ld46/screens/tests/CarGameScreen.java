package net.lintford.ld46.screens.tests;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.lwjgl.opengl.GL11;

import net.lintford.ld46.controllers.CarController;
import net.lintford.ld46.data.cars.CarManager;
import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.controllers.camera.CameraZoomController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.renderers.debug.DebugBox2dDrawer;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;

public class CarGameScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final Vec2 BOX2D_GRAVITY = new Vec2(0, 0);

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	// Data
	private World mBox2dWorld;
	private CarManager mCarManager;

	// Controllers
	private Box2dWorldController mBox2dWorldController;
	private CarController mCarController;

	// Renderers
	private Texture mGridTexture;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public CarGameScreen(ScreenManager pScreenManager) {
		super(pScreenManager);

		mBox2dWorld = new World(BOX2D_GRAVITY);
		mCarManager = new CarManager();

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize() {
		super.initialize();

		createControllers();

	}

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		createRenderers();

		mGridTexture = pResourceManager.textureManager().loadTexture("TEXTURE_GRID", "res/textures/textureGrid01.png", GL11.GL_NEAREST, entityGroupID());

	}

	@Override
	public void handleInput(LintfordCore pCore, boolean pAcceptMouse, boolean pAcceptKeyboard) {
		super.handleInput(pCore, pAcceptMouse, pAcceptKeyboard);

	}

	@Override
	public void update(LintfordCore pCore, boolean pOtherScreenHasFocus, boolean pCoveredByOtherScreen) {
		super.update(pCore, pOtherScreenHasFocus, pCoveredByOtherScreen);

	}

	@Override
	public void draw(LintfordCore pCore) {
		GL11.glClearColor(0.03f, 0.37f, 0.13f, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		final var lTextureBatch = mRendererManager.uiTextureBatch();
		lTextureBatch.begin(pCore.gameCamera());

		final var lOrigWidth = pCore.gameCamera().getWidth();
		final var lOrigHeight = pCore.gameCamera().getHeight();

		final var lDestWidth = pCore.gameCamera().getWidth() * pCore.gameCamera().getZoomFactorOverOne();
		final var lDestHeight = pCore.gameCamera().getHeight() * pCore.gameCamera().getZoomFactorOverOne();

		lTextureBatch.draw(mGridTexture, -0, -0, lOrigWidth, lOrigHeight, -lDestWidth * 0.5f, -lDestHeight * 0.5f, lDestWidth, lDestHeight, -0.05f, 1f, 1f, 1f, 1f);
		lTextureBatch.end();

		super.draw(pCore);

		final var lFontUnit = mRendererManager.textFont();
		lFontUnit.begin(pCore.HUD());
		lFontUnit.draw("Camera Zoom: " + pCore.gameCamera().getZoomFactor(), 0, 0, 1f);
		lFontUnit.end();

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void createControllers() {
		final var lCore = mScreenManager.core();
		final var lControllerManager = lCore.controllerManager();
		final var lGameCamera = lCore.gameCamera();

		mBox2dWorldController = new Box2dWorldController(lControllerManager, mBox2dWorld, entityGroupID());
		mBox2dWorldController.initialize(lCore);

		mCarController = new CarController(lControllerManager, mCarManager, entityGroupID());
		mCarController.initialize(lCore);

		final var lZoomController = new CameraZoomController(lControllerManager, lGameCamera, entityGroupID());
		lZoomController.setZoomConstraints(0.25f, 50.0f);

	}

	private void createRenderers() {
		new DebugBox2dDrawer(mRendererManager, mBox2dWorld, entityGroupID());

	}

}
