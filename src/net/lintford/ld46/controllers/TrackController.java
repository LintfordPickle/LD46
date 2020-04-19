package net.lintford.ld46.controllers;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import net.lintford.ld46.data.tracks.Track;
import net.lintford.ld46.data.tracks.TrackManager;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.core.maths.spline.Spline;
import net.lintford.library.core.maths.spline.SplinePoint;

public class TrackController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Track Controller";

	float lSegmentWidth = 512f;
	float lTrackScale = 2.f;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------{

	private TrackManager mTrackManager;
	private Box2dWorldController mBox2dWorldController;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isinitialized() {
		return mTrackManager != null;
	}

	public Track currentTrack() {
		return mTrackManager.currentTrack();
	}

	public TrackManager trackManager() {
		return mTrackManager;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackController(ControllerManager pControllerManager, TrackManager pTrackManager, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mTrackManager = pTrackManager;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mBox2dWorldController = (Box2dWorldController) pCore.controllerManager().getControllerByNameRequired(Box2dWorldController.CONTROLLER_NAME, entityGroupID());

		loadTrack();

	}

	public void loadTrack() {
		mTrackManager.loadTrack("res/defs/tracks/defTrack01.json");

		if (mTrackManager.currentTrack() != null) {
			final var lTrackSpline = mTrackManager.currentTrack().trackSpline();

			// Need to rescale the control nodes
			final var lPointsList = lTrackSpline.points();
			final int lNumSplineNodes = lPointsList.size();
			for (int i = 0; i < lNumSplineNodes; i++) {
				final var lSplinePoint = lPointsList.get(i);

				lSplinePoint.x *= lTrackScale;
				lSplinePoint.y *= lTrackScale;

			}

			buildBox2dCollisionTrack(mBox2dWorldController.world(), getHiResSpline(lTrackSpline));

		} else {
			throw new RuntimeException("Could not load Box2d geometry for track");
		}

	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private Spline getHiResSpline(Spline pSpline) {
		List<SplinePoint> lNewSplinePoints = new ArrayList<>();
		final var offset = pSpline.isLooped() ? 0f : 3f;
		for (float t = 0; t < pSpline.points().size() - offset; t += 0.105f) {
			final var lPoint = pSpline.getPointOnSpline(t);
			SplinePoint lNewSplinePoint = new SplinePoint(lPoint.x, lPoint.y);

			lNewSplinePoints.add(lNewSplinePoint);
		}

		SplinePoint[] lSplinePoints = new SplinePoint[lNewSplinePoints.size()];
		lNewSplinePoints.toArray(lSplinePoints);

		return new Spline(lSplinePoints);
	}

	private void buildBox2dCollisionTrack(World pWorld, Spline pSpline) {
		final var lNumSplinePoints = pSpline.points().size();

		Vector2f lTempVector = new Vector2f();
		SplinePoint tempDriveDirection = new SplinePoint();
		SplinePoint tempSideDirection = new SplinePoint();

		final Vec2[] lInnerVertices = new Vec2[lNumSplinePoints + 1];
		final Vec2[] lOuterVertices = new Vec2[lNumSplinePoints + 1];

		final float lScaledSegWidth = lSegmentWidth * lTrackScale;

		for (int i = 0; i < lNumSplinePoints; i++) {
			int nextIndex = i + 1;
			if (nextIndex > lNumSplinePoints - 1) {
				nextIndex = 0;
			}

			tempDriveDirection.x = pSpline.points().get(nextIndex).x - pSpline.points().get(i).x;
			tempDriveDirection.y = pSpline.points().get(nextIndex).y - pSpline.points().get(i).y;

			tempSideDirection.x = tempDriveDirection.y;
			tempSideDirection.y = -tempDriveDirection.x;

			lTempVector.set(tempSideDirection.x, tempSideDirection.y);
			lTempVector.nor();

			final var lOuterPoint = new Vec2();
			final var lInnerPoint = new Vec2();

			lOuterPoint.x = (pSpline.points().get(i).x + lTempVector.x * lScaledSegWidth / 2) * Box2dWorldController.PIXELS_TO_UNITS;
			lOuterPoint.y = (pSpline.points().get(i).y + lTempVector.y * lScaledSegWidth / 2) * Box2dWorldController.PIXELS_TO_UNITS;

			lInnerPoint.x = (pSpline.points().get(i).x - lTempVector.x * lScaledSegWidth / 2) * Box2dWorldController.PIXELS_TO_UNITS;
			lInnerPoint.y = (pSpline.points().get(i).y - lTempVector.y * lScaledSegWidth / 2) * Box2dWorldController.PIXELS_TO_UNITS;

			lInnerVertices[i] = lInnerPoint;
			lOuterVertices[i] = lOuterPoint;

		}

		// Add the last two vertices to close the track
		lInnerVertices[lNumSplinePoints] = lInnerVertices[0];
		lOuterVertices[lNumSplinePoints] = lOuterVertices[0];

		// End

		ChainShape lOuterChainShape = new ChainShape();
		lOuterChainShape.createChain(lOuterVertices, lNumSplinePoints + 1);

		ChainShape lInnerChainShape = new ChainShape();
		lInnerChainShape.createChain(lInnerVertices, lNumSplinePoints + 1);

		BodyDef lTrackBodyDef = new BodyDef();
		final var lBody = pWorld.createBody(lTrackBodyDef);

		FixtureDef lInnerFixtureDef = new FixtureDef();
		lInnerFixtureDef.filter.categoryBits = Box2dGameController.CATEGORY_TRACK;
		lInnerFixtureDef.filter.maskBits = Box2dGameController.CATEGORY_CAR;

		lInnerFixtureDef.setShape(lInnerChainShape);

		FixtureDef lOuterFixtureDef = new FixtureDef();
		lOuterFixtureDef.filter.categoryBits = Box2dGameController.CATEGORY_TRACK;
		lOuterFixtureDef.filter.maskBits = Box2dGameController.CATEGORY_CAR;
		lOuterFixtureDef.setShape(lOuterChainShape);

		lBody.createFixture(lInnerFixtureDef);
		lBody.createFixture(lOuterFixtureDef);

	}

}
