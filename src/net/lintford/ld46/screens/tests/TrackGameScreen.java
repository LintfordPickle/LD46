package net.lintford.ld46.screens.tests;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.lwjgl.opengl.GL11;

import net.lintford.ld46.controllers.TrackController;
import net.lintford.ld46.data.tracks.TrackManager;
import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.controllers.camera.CameraController;
import net.lintford.library.controllers.camera.CameraZoomController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.maths.spline.SplinePoint;
import net.lintford.library.renderers.debug.DebugBox2dDrawer;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;

public class TrackGameScreen extends BaseGameScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final Vec2 BOX2D_GRAVITY = new Vec2(0, 0);

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	// Data
	private World mBox2dWorld;
	private TrackManager mTrackManger;

	// Controllers
	private Box2dWorldController mBox2dWorldController;
	private TrackController mTrackController;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackGameScreen(ScreenManager pScreenManager) {
		super(pScreenManager);

		mBox2dWorld = new World(BOX2D_GRAVITY);
		mTrackManger = new TrackManager();

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

		{
			final var offset = mTrackManger.currentTrack().trackSpline().isLooped() ? 0f : 3f;
			for (float t = 0; t < mTrackManger.currentTrack().trackSpline().points().size() - offset; t += 0.025f) {
				SplinePoint lPoint = mTrackManger.currentTrack().trackSpline().getPointOnSpline(t);
				if (lPoint.x == 9) {

				}
				Debug.debugManager().drawers().drawPointImmediate(pCore.gameCamera(), lPoint.x, lPoint.y, -0.01f, 1f, 1f, 0f, 1f);
			}
		}

		{
			GL11.glPointSize(4f);

			final var lTextFont = mRendererManager.textFont();

			lTextFont.begin(pCore.HUD());
			Debug.debugManager().drawers().beginPointRenderer(pCore.gameCamera());

			final int lNumPoints = mTrackManger.currentTrack().trackSpline().points().size();
			for (int i = 0; i < lNumPoints; i++) {
				final var lPoint = mTrackManger.currentTrack().trackSpline().points().get(i);
				Debug.debugManager().drawers().drawPoint(lPoint.x, lPoint.y, 1f, 1f, 0f, 1f);
				lTextFont.draw(String.format("%d (%.2f)", i, lPoint.length), lPoint.x, lPoint.y, 1f);

			}

			Debug.debugManager().drawers().endPointRenderer();
			lTextFont.end();
		}

		final var lFontUnit = mRendererManager.textFont();
		lFontUnit.begin(pCore.HUD());

		final float lLeft = pCore.HUD().boundingRectangle().left();
		final float lTop = pCore.HUD().boundingRectangle().top();

		lFontUnit.draw("Track Screen", lLeft + 5f, lTop + 5, 1f);
		lFontUnit.draw("Camera Zoom: " + pCore.gameCamera().getZoomFactor(), lLeft + 5f, lTop + 25, 1f);
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
		lZoomController.setZoomConstraints(0.25f, 50.0f);

		new CameraController(lControllerManager, lGameCamera, entityGroupID());

		mTrackController = new TrackController(lControllerManager, mTrackManger, entityGroupID());
		mTrackController.initialize(lCore);

	}

	private void createRenderers() {
		new DebugBox2dDrawer(mRendererManager, mBox2dWorld, entityGroupID());

	}

}
