package net.lintford.ld46.renderers;

import org.lwjgl.opengl.GL11;

import net.lintford.ld46.controllers.CarController;
import net.lintford.ld46.controllers.TelekinesisController;
import net.lintford.ld46.controllers.TrackController;
import net.lintford.ld46.data.cars.Car;
import net.lintford.ld46.data.tracks.Track;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.pointbatch.PointBatch;
import net.lintford.library.core.maths.spline.Spline;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class MinimapRenderer extends BaseRenderer {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Minimap Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private CarController mCarController;
	private TrackController mTrackController;
	private TelekinesisController mTelekinesisController;

	private PointBatch mPointBatch;

	private float mMiniMapPositionOffsetX;
	private float mMiniMapPositionOffsetY;

	private float mMiniMapTrackScale = 0.01f;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return mTrackController != null;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public MinimapRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

		mPointBatch = new PointBatch();

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		final var lControllerManager = pCore.controllerManager();

		mTrackController = (TrackController) lControllerManager.getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());
		mCarController = (CarController) lControllerManager.getControllerByNameRequired(CarController.CONTROLLER_NAME, entityGroupID());
		mTelekinesisController = (TelekinesisController) lControllerManager.getControllerByNameRequired(TelekinesisController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mPointBatch.loadGLContent(pResourceManager);

	}

	@Override
	public void draw(LintfordCore pCore) {
		final var lHudBoundingBox = pCore.HUD();

		final var lTrack = mTrackController.currentTrack();
		final var lTrackSpline = lTrack.trackSpline();

		mMiniMapPositionOffsetX = lHudBoundingBox.getWidth() * 0.35f;
		mMiniMapPositionOffsetY = -lHudBoundingBox.getHeight() * 0.35f - 50f;

		drawMiniMapTrack(pCore, lTrackSpline);

		{
			mPointBatch.begin(pCore.HUD());
			GL11.glPointSize(8f);

			final var lPlayerCar = mCarController.carManager().playerCar();
			drawPointOnMiniMap(pCore, lTrack, lPlayerCar, 1f, 1f, 1f);

			final var lOpponentCarList = mCarController.carManager().cars();
			final int lNumberOpponents = lOpponentCarList.size();
			for (int i = 0; i < lNumberOpponents; i++) {
				final var lOpponentCar = lOpponentCarList.get(i);

				float lR = .2f;
				float lG = .2f;
				float lB = .2f;
				if (mTelekinesisController.isInTelekinesisMode()) {
					final int lSelectedCarIndex = mTelekinesisController.selectedCarIndex();
					if (lSelectedCarIndex == i) {
						lR = 1f;
						lG = 0f;
						lB = 0f;
					}
				}

				drawPointOnMiniMap(pCore, lTrack, lOpponentCar, lR, lG, lB);

			}

			mPointBatch.end();
		}

	}

	private void drawMiniMapTrack(LintfordCore pCore, final Spline pTrackSpline) {
		final var lLineBatch = mRendererManager.uiLineBatch();
		lLineBatch.begin(pCore.HUD());
		lLineBatch.lineType(GL11.GL_LINE_STRIP);
		lLineBatch.lineWidth(2);

		{
			final var offset = pTrackSpline.isLooped() ? 0f : 3f;
			for (float t = 0f; t < pTrackSpline.points().size() - offset; t += 0.125f) {
				final var lSplinePoint = pTrackSpline.getPointOnSpline(t);

				final float pPointPosX = mMiniMapPositionOffsetX + (lSplinePoint.x * mMiniMapTrackScale);
				final float pPointPosY = mMiniMapPositionOffsetY + (lSplinePoint.y * mMiniMapTrackScale);

				lLineBatch.draw(pPointPosX, pPointPosY, -0.01f, 1f, 1f, 0f, 1f);

			}

			// Fill in the last segment
			final var lSplinePoint = pTrackSpline.getPointOnSpline(0);
			final float pPointPosX = mMiniMapPositionOffsetX + (lSplinePoint.x * mMiniMapTrackScale);
			final float pPointPosY = mMiniMapPositionOffsetY + (lSplinePoint.y * mMiniMapTrackScale);
			lLineBatch.draw(pPointPosX, pPointPosY, -0.01f, 1f, 1f, 0f, 1f);

		}

		lLineBatch.end();
	}

	private void drawPointOnMiniMap(LintfordCore pCore, Track pTrack, Car pCar, float pR, float pG, float pB) {
		final int lNumControlNodes = pTrack.trackSpline().numberSplineControlPoints();
		final int lLastNodeId = (int) ((pCar.carProgress().lastVisitedNodeId + 1 >= lNumControlNodes) ? 0 : pCar.carProgress().lastVisitedNodeId);
		final float lCarPositionAlongSpling = pTrack.trackSpline().getNormalizedPositionAlongSpline(lLastNodeId, pCar.x, pCar.y);
		final var lTrackSplinePoint = pTrack.trackSpline().getPointOnSpline(lLastNodeId + lCarPositionAlongSpling);

		final float pPointPosX = mMiniMapPositionOffsetX + (lTrackSplinePoint.x * mMiniMapTrackScale);
		final float pPointPosY = mMiniMapPositionOffsetY + (lTrackSplinePoint.y * mMiniMapTrackScale);

		mPointBatch.draw(pPointPosX, pPointPosY, -0.01f, pR, pG, pB, 1f);

	}

}
