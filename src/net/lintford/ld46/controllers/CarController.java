package net.lintford.ld46.controllers;

import org.lwjgl.glfw.GLFW;

import net.lintford.ld46.data.cars.Car;
import net.lintford.ld46.data.cars.CarManager;
import net.lintford.ld46.data.tracks.Track;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.ResourceController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.core.maths.spline.SplinePoint;

public class CarController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "CarController";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private int mCarUidCounter;
	private CarManager mCarManager;
	private ResourceController mResourceController;
	private TrackController mTrackController;
	private Box2dWorldController mBox2dWorldController;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	private int getCarPoolUid() {
		return mCarUidCounter++;
	}

	@Override
	public boolean isinitialized() {
		return mCarManager != null;
	}

	public CarManager carManager() {
		return mCarManager;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public CarController(ControllerManager pControllerManager, CarManager pCarManager, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mCarManager = pCarManager;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mResourceController = (ResourceController) pCore.controllerManager().getControllerByNameRequired(ResourceController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);
		mBox2dWorldController = (Box2dWorldController) pCore.controllerManager().getControllerByNameRequired(Box2dWorldController.CONTROLLER_NAME, entityGroupID());
		mTrackController = (TrackController) pCore.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());

		mCarUidCounter = 0;

		setupPlayerCar();
		setupOpponents(5);

	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		final var lPlayerCar = mCarManager.playerCar();

		// Handle input for player car
		lPlayerCar.input().isTurningLeft = pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_A);
		lPlayerCar.input().isTurningRight = pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_D);
		lPlayerCar.input().isGas = pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_W);
		lPlayerCar.input().isBrake = pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_S);
		lPlayerCar.input().isHandBrake = pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_SPACE);

		if (lPlayerCar.input().isTurningLeft) {
			lPlayerCar.steeringAngleDeg(lPlayerCar.steeringAngleLockDeg());
		}

		else if (lPlayerCar.input().isTurningRight) {
			lPlayerCar.steeringAngleDeg(-lPlayerCar.steeringAngleLockDeg());
		} else
			lPlayerCar.steeringAngleDeg(0);

		return super.handleInput(pCore);

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		final var lPlayerCar = mCarManager.playerCar();

		lPlayerCar.updatePhyics(pCore);

		final var lTrack = mTrackController.currentTrack();
		if (lTrack == null)
			return;

		updateCarProgress(pCore, lPlayerCar, lTrack);

		// OPPONENTS

		final var lOpponentsList = mCarManager.opponents();
		final int lNumOpponents = lOpponentsList.size();

		for (int i = 0; i < lNumOpponents; i++) {
			final var lOpponentCar = lOpponentsList.get(i);
			lOpponentCar.updatePhyics(pCore);

			updateCarProgress(pCore, lOpponentCar, lTrack);

			updateCarAI(pCore, lOpponentCar);

			updateCrashResolver(pCore, lOpponentCar);

		}

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void updateCarProgress(LintfordCore pCore, Car pCar, Track pTrack) {
		final int lNumNodes = pTrack.trackSpline().numberSplineControlPoints();
		final int lNextNodeId = pCar.carProgress().nextControlNodeId;

		if (lNextNodeId >= lNumNodes) {
			// We lapped
			pCar.carProgress().currentLapNumber++;
			pCar.carProgress().nextControlNodeId = 0;

		}

		// We check the next node and also all 'futher' nodes (only until the end), incase the cars got past
		for (int j = 0; j < lNumNodes; j++) {

			final var lNode = pTrack.trackSpline().getControlPoint(j);

			final float lDist2 = Vector2f.distance2(pCar.x, pCar.y, lNode.x, lNode.y);
			if (lDist2 < 512f * 512) {
				if (j != lNextNodeId) {
					// going the wrong way

					if (pCar.carProgress().lastVisitedNodeId != j) {
						if (j + 5000 > pCar.carProgress().lastVisitedNodeId + 5000) {
							pCar.carProgress().isGoingWrongWay = false;
						} else
							pCar.carProgress().isGoingWrongWay = true;

					}

				} else {
					pCar.carProgress().nextControlNodeId++;
					pCar.carProgress().isGoingWrongWay = false;

				}

				pCar.carProgress().lastVisitedNodeId = j;

			}

		}

	}

	private void updateCarAI(LintfordCore pCore, Car pCar) {
		final var lTrack = mTrackController.currentTrack();

		final int lNumControlNodes = lTrack.trackSpline().numberSplineControlPoints();
		final int lLastNodeId = pCar.carProgress().lastVisitedNodeId;
		final int lNextNodeId = (int) ((lLastNodeId + 1 >= lNumControlNodes) ? 0 : lLastNodeId + 1);

		final float lCarPositionAlongSpling = lTrack.trackSpline().getNormalizedPositionAlongSpline(lLastNodeId, pCar.x, pCar.y);
		SplinePoint lTrackSplinePoint = lTrack.trackSpline().getPointOnSpline(lLastNodeId + lCarPositionAlongSpling);

		pCar.pointOnTrackX = lTrackSplinePoint.x;
		pCar.pointOnTrackY = lTrackSplinePoint.y;

		final float lNode0X = pCar.pointOnTrackX;
		final float lNode0Y = pCar.pointOnTrackY;

		lTrackSplinePoint = lTrack.trackSpline().getControlPoint(lNextNodeId);

		final float lNode1X = lTrackSplinePoint.x;
		final float lNode1Y = lTrackSplinePoint.y;

		final float lHeadingVecX = lNode1X - lNode0X;
		final float lHeadingVecY = lNode1Y - lNode0Y;

		if (pCar.wheels().size() > 0) {
			final var lWheel = pCar.wheels().get(0);
			if (lWheel != null) {
				final var lWheelBody = lWheel.mBox2dBodyInstance.mBody;

				if (lWheelBody != null) {
					pCar.wheelAngle = lWheelBody.getAngle() + (float) Math.toRadians(-90f);
				}

			}

		}

		pCar.trackAngle = (float) Math.atan2(lHeadingVecY, lHeadingVecX);
		pCar.aiHeadingAngle = turnToFace(pCar.wheelAngle, pCar.trackAngle, 0.15f);

		pCar.steeringAngleDeg((float) Math.toDegrees(pCar.aiHeadingAngle));

		if (pCar.aiHeadingAngle > 0) {
			pCar.input().isTurningLeft = true;
			pCar.input().isTurningRight = false;
		}

		if (pCar.aiHeadingAngle < 0) {
			pCar.input().isTurningLeft = false;
			pCar.input().isTurningRight = true;
		}

		pCar.input().isGas = true;

	}

	private void updateCrashResolver(LintfordCore pCore, Car pCar) {
		final float lTimer = 500.0f;
		final float lMinDist = 100f;

		pCar.mLastCrashResolverUpdateTime -= pCore.time().elapseGameTimeMilli();
		if (pCar.mLastCrashResolverUpdateTime < 0.0f) {
			final float lDistTravelled = Vector2f.distance(pCar.mLastCrashResolverUpdateX, pCar.mLastCrashResolverUpdateY, pCar.x, pCar.y);

			if (lDistTravelled < lMinDist) {
				pCar.mLastCrashResolverCounter++;

				if (pCar.mLastCrashResolverCounter > 2.f) {
					resetCarToCenter(pCore, pCar);

					pCar.mLastCrashResolverUpdateX = .0f;
					pCar.mLastCrashResolverUpdateY = .0f;
					pCar.mLastCrashResolverCounter = .0f;

				}

			} else {
				pCar.mLastCrashResolverUpdateX = pCar.x;
				pCar.mLastCrashResolverUpdateY = pCar.y;
			}

			pCar.mLastCrashResolverUpdateTime = lTimer;
		}

	}

	private void resetCarToCenter(LintfordCore pCore, Car pCar) {

		float lAngle = getTrackGradientAtVehicleLocation(pCar);

		pCar.mJBox2dEntityInstance.setTransform(pCar.pointOnTrackX * Box2dWorldController.PIXELS_TO_UNITS, pCar.pointOnTrackY * Box2dWorldController.PIXELS_TO_UNITS, lAngle);
		System.out.println("resolved crash");

	}

	static float turnToFace(float pTrackHeading, float pCurrentAngle, float pTurnSpeed) {
		float difference = wrapAngle(pTrackHeading - pCurrentAngle);

		// clamp
		difference = MathHelper.clamp(difference, -pTurnSpeed, pTurnSpeed);

		return wrapAngle(difference);

	}

	/** wraps to -PI / PI */
	public static float wrapAngle(float radians) {
		while (radians < -Math.PI) {
			radians += Math.PI * 2;
		}
		while (radians > Math.PI) {
			radians -= Math.PI * 2;
		}
		return radians;
	}

	private void setupPlayerCar() {
		final var lNewPlayerCar = new Car(getCarPoolUid());
		lNewPlayerCar.setCarDriveProperties(150.f, -30.f, 75.f);
		lNewPlayerCar.setCarSteeringProperties(5.5f, 32.0f, 320.0f);

		final var lResourceManager = mResourceController.resourceManager();
		final var lBox2dWorld = mBox2dWorldController.world();

		final var lPObjectInstance = lResourceManager.pobjectManager().getNewInstanceFromPObject(lBox2dWorld, "POBJECT_VEHICLE_01");
		lPObjectInstance.setFixtureCategory(Box2dGameController.CATEGORY_CAR);
		lPObjectInstance.setFixtureBitMask(Box2dGameController.CATEGORY_TRACK | Box2dGameController.CATEGORY_CAR);
		lPObjectInstance.setWorldRotation((float) Math.toRadians(-90.f));
		lPObjectInstance.setPosition(200 * Box2dWorldController.PIXELS_TO_UNITS, 50 * Box2dWorldController.PIXELS_TO_UNITS);
		lNewPlayerCar.setPhysicsObject(lPObjectInstance);
		lNewPlayerCar.loadPhysics(lBox2dWorld);

		mCarManager.playerCar(lNewPlayerCar);

	}

	private void setupOpponents(final int pNumOpponents) {
		final var lResourceManager = mResourceController.resourceManager();
		final var lBox2dWorld = mBox2dWorldController.world();

		final float lNormalizedDistanceBetweenSpawns = 0.3f;
		float lCurrentMarker = lNormalizedDistanceBetweenSpawns;
		final var lTrack = mTrackController.currentTrack();

		if (lTrack == null) {
			return;
		}

		for (int i = 0; i < pNumOpponents; i++) {
			// Work out a start position
			final var lSplinePoint = lTrack.trackSpline().getPointOnSpline(lCurrentMarker += lNormalizedDistanceBetweenSpawns);

			final var lNewOpponent = new Car(getCarPoolUid());
			lNewOpponent.setCarDriveProperties(150.f, -30.f, 75.f);
			lNewOpponent.setCarSteeringProperties(5.5f, 32.0f, 320.0f);

			final var lPObjectInstance = lResourceManager.pobjectManager().getNewInstanceFromPObject(lBox2dWorld, "POBJECT_VEHICLE_01");
			lPObjectInstance.setFixtureCategory(Box2dGameController.CATEGORY_CAR);
			lPObjectInstance.setFixtureBitMask(Box2dGameController.CATEGORY_TRACK | Box2dGameController.CATEGORY_CAR);
			lPObjectInstance.setWorldPosition(lSplinePoint.x * Box2dWorldController.PIXELS_TO_UNITS, lSplinePoint.y * Box2dWorldController.PIXELS_TO_UNITS);

			lPObjectInstance.setWorldRotation((float) Math.toRadians(90.f));

			lNewOpponent.setPhysicsObject(lPObjectInstance);
			lNewOpponent.loadPhysics(lBox2dWorld);

			mCarManager.opponents().add(lNewOpponent);

		}

	}

	private float getTrackAngle(Car pCar) {
		final var lTrack = mTrackController.currentTrack();

		final int lNumControlNodes = lTrack.trackSpline().numberSplineControlPoints();
		final int lLastNodeId = pCar.carProgress().lastVisitedNodeId;
		final int lNextNodeId = (int) ((lLastNodeId + 1 >= lNumControlNodes) ? 0 : lLastNodeId + 1);

		final float lCarPositionAlongSpling = lTrack.trackSpline().getNormalizedPositionAlongSpline(lLastNodeId, pCar.x, pCar.y);
		SplinePoint lTrackSplinePoint = lTrack.trackSpline().getPointOnSpline(lLastNodeId + lCarPositionAlongSpling);

		pCar.pointOnTrackX = lTrackSplinePoint.x;
		pCar.pointOnTrackY = lTrackSplinePoint.y;

		final float lNode0X = pCar.pointOnTrackX;
		final float lNode0Y = pCar.pointOnTrackY;

		lTrackSplinePoint = lTrack.trackSpline().getControlPoint(lNextNodeId);

		final float lNode1X = lTrackSplinePoint.x;
		final float lNode1Y = lTrackSplinePoint.y;

		final float lHeadingVecX = lNode1X - lNode0X;
		final float lHeadingVecY = lNode1Y - lNode0Y;

		return (float) Math.atan2(lHeadingVecY, lHeadingVecX);

	}

	private float getTrackGradientAtVehicleLocation(Car pCar) {
		final var lTrack = mTrackController.currentTrack();

		final int lNumControlNodes = lTrack.trackSpline().numberSplineControlPoints();
		final int lLastNodeId = pCar.carProgress().lastVisitedNodeId;
		final int lNextNodeId = (int) ((lLastNodeId + 1 >= lNumControlNodes) ? 0 : lLastNodeId + 1);

		final float lCarPositionAlongSpling = lTrack.trackSpline().getNormalizedPositionAlongSpline(lLastNodeId, pCar.x, pCar.y);
		SplinePoint lTrackSplinePoint = lTrack.trackSpline().getPointOnSpline(lLastNodeId + lCarPositionAlongSpling);

		final float lNode0X = lTrackSplinePoint.x;
		final float lNode0Y = lTrackSplinePoint.y;

		lTrackSplinePoint = lTrack.trackSpline().getControlPoint(lNextNodeId);

		final float lNode1X = lTrackSplinePoint.x;
		final float lNode1Y = lTrackSplinePoint.y;

		final float lHeadingVecX = lNode1X - lNode0X;
		final float lHeadingVecY = lNode1Y - lNode0Y;

		return (float) Math.toDegrees(Math.atan2(-lHeadingVecY, lHeadingVecX));
	}
}
