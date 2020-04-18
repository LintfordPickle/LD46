package net.lintford.ld46.screens;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.lwjgl.opengl.GL11;

import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.controllers.camera.CameraZoomController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;

public class GameScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final Vec2 BOX2D_GRAVITY = new Vec2(0, 0);

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private World mBox2dWorld;

	private Box2dWorldController mBox2dWorldController;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameScreen(ScreenManager pScreenManager) {
		super(pScreenManager);

		mBox2dWorld = new World(BOX2D_GRAVITY);

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
	public void draw(LintfordCore pCore) {
		super.draw(pCore);

		GL11.glClearColor(0.03f, 0.37f, 0.13f, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

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

		final var lZoomController = new CameraZoomController(lControllerManager, lGameCamera, entityGroupID());
		lZoomController.setZoomConstraints(0.5f, 50.0f);

	}

	private void createRenderers() {
		final var lRendererManager = mRendererManager;

	}

}
