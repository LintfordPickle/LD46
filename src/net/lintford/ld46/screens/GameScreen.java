package net.lintford.ld46.screens;

import org.lwjgl.opengl.GL11;

import net.lintford.ld46.controllers.CarController;
import net.lintford.ld46.controllers.GameStateController;
import net.lintford.ld46.controllers.TrackController;
import net.lintford.ld46.data.GameWorld;
import net.lintford.ld46.renderers.GameStateRenderer;
import net.lintford.ld46.renderers.TrackRenderer;
import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.controllers.camera.CameraFollowController;
import net.lintford.library.controllers.camera.CameraZoomController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.renderers.debug.DebugBox2dDrawer;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;

public class GameScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	// Data
	private GameWorld mGameWorld;

	// Controllers
	private Box2dWorldController mBox2dWorldController;
	private CameraFollowController mCameraFollowController;
	private CarController mCarController;
	private TrackController mTrackController;
	private GameStateController mGameStateController;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameScreen(ScreenManager pScreenManager) {
		super(pScreenManager);

		mGameWorld = new GameWorld();

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

		super.draw(pCore);

		final var lFontUnit = mRendererManager.textFont();
		final var lZoomText = "Camera Zoom: " + pCore.gameCamera().getZoomFactor();
		final var lHudBoundingBox = pCore.HUD().boundingRectangle();
		final var lZoomTextWidth = lFontUnit.bitmap().getStringWidth(lZoomText);

		lFontUnit.begin(pCore.HUD());
		lFontUnit.draw(lZoomText, lHudBoundingBox.w() * .5f - 5.f - lZoomTextWidth, -lHudBoundingBox.h() * 0.5f, 1f);
		lFontUnit.end();

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void createControllers() {
		final var lCore = mScreenManager.core();
		final var lControllerManager = lCore.controllerManager();
		final var lGameCamera = lCore.gameCamera();

		mBox2dWorldController = new Box2dWorldController(lControllerManager, mGameWorld.box2dWorld(), entityGroupID());
		mBox2dWorldController.initialize(lCore);

		final var lZoomController = new CameraZoomController(lControllerManager, lGameCamera, entityGroupID());
		lZoomController.setZoomConstraints(0.025f, 50.0f);

		mTrackController = new TrackController(lControllerManager, mGameWorld.trackManager(), entityGroupID());
		mTrackController.initialize(lCore);

		mCarController = new CarController(lControllerManager, mGameWorld.carManager(), entityGroupID());
		mCarController.initialize(lCore);

		final var lPlayerCar = mGameWorld.carManager().playerCar();
		mCameraFollowController = new CameraFollowController(lControllerManager, lGameCamera, lPlayerCar, entityGroupID());
		mCameraFollowController.initialize(lCore);

		mGameStateController = new GameStateController(lControllerManager, mGameWorld, entityGroupID());
		mGameStateController.initialize(lCore);

	}

	private void createRenderers() {
		final var lCore = mScreenManager.core();

		new TrackRenderer(mRendererManager, entityGroupID()).initialize(lCore);
		new GameStateRenderer(mRendererManager, entityGroupID()).initialize(lCore);
		new DebugBox2dDrawer(mRendererManager, mGameWorld.box2dWorld(), entityGroupID()).initialize(lCore);

	}

}
