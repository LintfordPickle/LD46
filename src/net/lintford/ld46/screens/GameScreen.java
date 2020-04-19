package net.lintford.ld46.screens;

import org.lwjgl.opengl.GL11;

import net.lintford.ld46.controllers.CameraCarChaseController;
import net.lintford.ld46.controllers.CarController;
import net.lintford.ld46.controllers.GameStateController;
import net.lintford.ld46.controllers.TelekinesisController;
import net.lintford.ld46.controllers.TrackController;
import net.lintford.ld46.data.GameWorld;
import net.lintford.ld46.renderers.CarRenderer;
import net.lintford.ld46.renderers.GameStateRenderer;
import net.lintford.ld46.renderers.TrackRenderer;
import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.controllers.camera.CameraZoomController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.camera.Camera;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;

public class GameScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	// Data
	private GameWorld mGameWorld;

	private Camera mTelekCamera;

	// Controllers
	private Box2dWorldController mBox2dWorldController;
	private CameraCarChaseController mCameraChaseControler;
	private CarController mCarController;
	private TrackController mTrackController;
	private GameStateController mGameStateController;
	private TelekinesisController mTelekinesisController;

	private CarRenderer mCarRenderer;

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
		initializeControllers();

		mTelekCamera = new Camera(mScreenManager.core().config().display());

		mGameStateController.startNewGame();

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

		if (mGameStateController.getEndConditionFlag() != GameStateController.END_CONDITION_NOT_SET) {
			switch (mGameStateController.getEndConditionFlag()) {
			case GameStateController.END_CONDITION_DESTROYED:
			case GameStateController.END_CONDITION_LOST:
			case GameStateController.END_CONDITION_WON_FIGHTING:
			case GameStateController.END_CONDITION_WON_RACING:
				mScreenManager.exitGame();

			}

		}

	}

	@Override
	public void draw(LintfordCore pCore) {
		GL11.glClearColor(0.03f, 0.37f, 0.13f, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		super.draw(pCore);
		
		{ // Normal frame render

			mCarRenderer.draw(pCore, pCore.gameCamera());

		}

		{ // Telekinesis mode - draw the opponent car

			final var lTelekManager = mGameWorld.telekinesisManager();
			if (lTelekManager.isInTelekinesesMode) {

				final var lSelectedCar = mGameWorld.carManager().opponents().get(lTelekManager.mSelectedOpponentIndex);

				mTelekCamera.setPosition(-lSelectedCar.x, -lSelectedCar.y - 150f);
				mTelekCamera.setZoomFactor(1.f);
				mTelekCamera.update(pCore);

				mCarRenderer.draw(pCore, mTelekCamera, lTelekManager.mSelectedOpponentIndex);

			}

		}
		
		

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void createControllers() {
		final var lCore = mScreenManager.core();
		final var lControllerManager = lCore.controllerManager();
		final var lGameCamera = lCore.gameCamera();

		mBox2dWorldController = new Box2dWorldController(lControllerManager, mGameWorld.box2dWorld(), entityGroupID());

		final var lZoomController = new CameraZoomController(lControllerManager, lGameCamera, entityGroupID());
		lZoomController.setZoomConstraints(0.025f, 50.0f);

		mTrackController = new TrackController(lControllerManager, mGameWorld.trackManager(), entityGroupID());
		mTelekinesisController = new TelekinesisController(lControllerManager, mGameWorld.telekinesisManager(), entityGroupID());
		mCarController = new CarController(lControllerManager, mGameWorld.carManager(), entityGroupID());

		// Needs to be called after the carcontroller is initialized

		mGameStateController = new GameStateController(lControllerManager, mGameWorld, entityGroupID());

	}

	private void initializeControllers() {
		final var lCore = mScreenManager.core();
		final var lControllerManager = lCore.controllerManager();
		final var lGameCamera = lCore.gameCamera();

		mBox2dWorldController.initialize(lCore);
		mTrackController.initialize(lCore);
		mTelekinesisController.initialize(lCore);
		mCarController.initialize(lCore);

		mGameStateController.initialize(lCore);

		final var lPlayerCar = mGameWorld.carManager().playerCar();
		mCameraChaseControler = new CameraCarChaseController(lControllerManager, lGameCamera, lPlayerCar, entityGroupID());
		mCameraChaseControler.initialize(lCore);

	}

	private void createRenderers() {
		final var lCore = mScreenManager.core();

		new TrackRenderer(mRendererManager, entityGroupID()).initialize(lCore);
		new GameStateRenderer(mRendererManager, entityGroupID()).initialize(lCore);
		
		mCarRenderer = new CarRenderer(mRendererManager, entityGroupID());
		mCarRenderer.initialize(lCore);

		// new DebugBox2dDrawer(mRendererManager, mGameWorld.box2dWorld(), entityGroupID()).initialize(lCore);
		
	}

}
