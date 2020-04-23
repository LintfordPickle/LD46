package net.lintford.ld46.controllers;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.lintford.ld46.data.cars.Car;
import net.lintford.ld46.data.cars.CarManager;
import net.lintford.ld46.data.tracks.Track;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.ResourceController;
import net.lintford.library.controllers.core.particles.ParticleFrameworkController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.audio.AudioManager;
import net.lintford.library.core.audio.AudioSource;
import net.lintford.library.core.audio.data.AudioData;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.core.maths.spline.SplinePoint;

public class CarController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "CarController";

	public static final int NUMBER_OPPONENTS = 4;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private int mCarUidCounter;
	private CarManager mCarManager;
	private ResourceController mResourceController;
	private TrackController mTrackController;
	private Box2dWorldController mBox2dWorldController;
	private ParticleFrameworkController mParticleController;
	private List<Car> mCarResolverList;
	private float mCarResolverTimer;

	private AudioSource mCrashAudio;
	private AudioData mCrashAudioData;

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
		mCarResolverList = new ArrayList<>();

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	public void playCrashSound() {
		mCrashAudio.play(mCrashAudioData.bufferID());
	}

	@Override
	public void initialize(LintfordCore pCore) {
		mResourceController = (ResourceController) pCore.controllerManager().getControllerByNameRequired(ResourceController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);
		mBox2dWorldController = (Box2dWorldController) pCore.controllerManager().getControllerByNameRequired(Box2dWorldController.CONTROLLER_NAME, entityGroupID());
		mTrackController = (TrackController) pCore.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());
		mParticleController = (ParticleFrameworkController) pCore.controllerManager().getControllerByNameRequired(ParticleFrameworkController.CONTROLLER_NAME, entityGroupID());

		mCarUidCounter = 0;

		setupPlayerCar();
		setupOpponents(NUMBER_OPPONENTS);

		mCrashAudio = pCore.resources().audioManager().getAudioSource(hashCode(), AudioManager.AUDIO_SOURCE_TYPE_SOUNDFX);
		mCrashAudio.setLooping(false);

		mCrashAudioData = pCore.resources().audioManager().getAudioDataBufferByName("SOUND_CRASH");

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

		if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_A)) {
			lPlayerCar.input().isTurningLeft = true;

		}

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

		final var lTrack = mTrackController.currentTrack();
		if (lTrack == null)
			return;

		final var lCarsList = mCarManager.cars();
		final int lNumCars = lCarsList.size();

		for (int i = 0; i < lNumCars; i++) {
			final var lCarToUpdate = lCarsList.get(i);
			lCarToUpdate.updatePhyics(pCore);

			updateCarProgress(pCore, lCarToUpdate, lTrack);

			if (!lCarToUpdate.isPlayerCar) {
				if (mCarManager.telekinesisCar() != null && mCarManager.telekinesisCar().equals(lCarToUpdate)) {
					updateCarAiWithPlayerInput(pCore, lCarToUpdate);

				} else {
					updateCarAI(pCore, lCarToUpdate);

				}

				updateCrashResolver(pCore, lCarToUpdate);

			}

		}

		// Resolve crashes one vehicle at a time
		if (mCarResolverList.size() > 0) {
			mCarResolverTimer -= pCore.appTime().elapseTimeMilli();
			if (mCarResolverTimer < 0.0f) {
				mCarResolverTimer = 1000.0f;

				final var lCar = mCarResolverList.remove(0);

				resetCarToCenter(pCore, lCar);

			}
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

		{ // DEBUG
			final var lTrack = mTrackController.currentTrack();
			final var lProgress = pCar.carProgress();

			float distance = 0.f;
			distance += lTrack.getTrackDistance() * lProgress.currentLapNumber;
			distance += lTrack.trackSpline().getControlPoint(pCar.carProgress().lastVisitedNodeId).accLength;
			distance += getCarDistanceIntoSegment(pCar);

			pCar.carProgress().distanceIntoRace = distance;

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

		final float lNode0X = pCar.x;// pointOnTrackX;
		final float lNode0Y = pCar.y;// pointOnTrackY;

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

		if (pCar.aiHeadingAngle == Math.abs(0.15f)) {
			pCar.input().isBrake = true;

		}

		pCar.steeringAngleDeg((float) Math.toDegrees(pCar.aiHeadingAngle));
		pCar.input().isGas = true;

	}

	private void updateCarAiWithPlayerInput(LintfordCore pCore, Car pCar) {
		final var lPlayerCar = mCarManager.playerCar();

		if (lPlayerCar.input().isTurningLeft) {
			pCar.steeringAngleDeg(pCar.steeringAngleLockDeg());
		} else if (lPlayerCar.input().isTurningRight) {
			pCar.steeringAngleDeg(-pCar.steeringAngleLockDeg());
		}

		pCar.input().isGas = true;

	}

	private void updateCrashResolver(LintfordCore pCore, Car pCar) {
		final float lTimer = 1250.0f;
		final float lMinDist = 75f;

		pCar.mLastCrashResolverUpdateTime -= pCore.appTime().elapseTimeMilli();
		if (pCar.mLastCrashResolverUpdateTime < 0.0f) {
			final float lDistTravelled = Vector2f.distance(pCar.mLastCrashResolverUpdateX, pCar.mLastCrashResolverUpdateY, pCar.x, pCar.y);

			if (lDistTravelled < lMinDist) {
				pCar.mLastCrashResolverCounter++;

				if (pCar.mLastCrashResolverCounter > 2.f) {
					mCarResolverList.add(pCar);

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

	}

	static float turnToFace(float pTrackHeading, float pCurrentAngle, float pTurnSpeed) {
		float difference = wrapAngle(pTrackHeading - pCurrentAngle);

		// clamp
		difference = MathHelper.clamp(difference, -pTurnSpeed, pTurnSpeed);

		return wrapAngle(difference);

	}

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
		lNewPlayerCar.setCarDriveProperties(230.f, -30.f, 155.f);
		lNewPlayerCar.setCarSteeringProperties(12.f, 32.0f, 320.0f);

		final var lResourceManager = mResourceController.resourceManager();
		final var lBox2dWorld = mBox2dWorldController.world();

		final var lPObjectInstance = lResourceManager.pobjectManager().getNewInstanceFromPObject(lBox2dWorld, "POBJECT_VEHICLE_01");
		lPObjectInstance.setFixtureCategory(Box2dGameController.CATEGORY_CAR);
		lPObjectInstance.setFixtureBitMask(Box2dGameController.CATEGORY_TRACK | Box2dGameController.CATEGORY_CAR);
		lNewPlayerCar.setPhysicsObject(lPObjectInstance);
		lNewPlayerCar.loadPhysics(lBox2dWorld);
		lNewPlayerCar.loadContent(mResourceController.resourceManager());

		float lAngle = getTrackGradientAtVehicleLocation(lNewPlayerCar);
		lPObjectInstance.setTransform(0.f, 0.f, lAngle);

		lNewPlayerCar.isPlayerCar = true;

		final int lNumWheels = lNewPlayerCar.wheels().size();
		for (int j = 0; j < lNumWheels; j++) {
			final var lWheel = lNewPlayerCar.wheels().get(j);
			lWheel.mSmokeEmitter = mParticleController.particleFrameworkData().emitterManager().getNewParticleEmitterInstanceByDefName("EMITTER_SMOKE");

		}

		mCarManager.cars().add(lNewPlayerCar);
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
			final var lSplinePoint = lTrack.trackSpline().getPointOnSpline(lCurrentMarker += lNormalizedDistanceBetweenSpawns);

			final float lX = lSplinePoint.x * Box2dWorldController.PIXELS_TO_UNITS;
			final float lY = lSplinePoint.y * Box2dWorldController.PIXELS_TO_UNITS;

			final var lNewOpponent = new Car(getCarPoolUid());
			// Make slow for testing
			// lNewOpponent.setCarDriveProperties(80.f, -30.f, 35.f);

			final float lRandSpeedAdd = RandomNumbers.random(-20.f, 30.f);
			final float lRandPowerAdd = RandomNumbers.random(-5.f, 10.f);

			final float lRandSkidCoefff = RandomNumbers.random(-5.f, 5.f);

			lNewOpponent.setCarDriveProperties(150.f + lRandSpeedAdd, -30.f, 125.f + lRandPowerAdd);
			lNewOpponent.setCarSteeringProperties(27.5f + lRandSkidCoefff, 32.0f, 320.0f);

			final var lPObjectInstance = lResourceManager.pobjectManager().getNewInstanceFromPObject(lBox2dWorld, "POBJECT_VEHICLE_01");
			lPObjectInstance.setFixtureCategory(Box2dGameController.CATEGORY_CAR);
			lPObjectInstance.setFixtureBitMask(Box2dGameController.CATEGORY_TRACK | Box2dGameController.CATEGORY_CAR);

			lPObjectInstance.setPosition(lX, lY);
			lNewOpponent.setPhysicsObject(lPObjectInstance);
			lNewOpponent.loadPhysics(lBox2dWorld);

			lNewOpponent.carProgress().lastVisitedNodeId = (int) lCurrentMarker;
			lNewOpponent.carProgress().nextControlNodeId = (int) lCurrentMarker + 1;
			float lAngle = getTrackGradientAtVehicleLocation(lNewOpponent);
			lPObjectInstance.setTransform(lX, lY, lAngle);

			final int lNumWheels = lNewOpponent.wheels().size();
			for (int j = 0; j < lNumWheels; j++) {
				final var lWheel = lNewOpponent.wheels().get(j);
				lWheel.mSmokeEmitter = mParticleController.particleFrameworkData().emitterManager().getNewParticleEmitterInstanceByDefName("EMITTER_SMOKE");

			}

			mCarManager.cars().add(lNewOpponent);

		}

	}

	private float getCarDistanceIntoSegmentNormalized(Car pCar) {
		final var lTrack = mTrackController.currentTrack();

		final int lNumControlNodes = lTrack.trackSpline().numberSplineControlPoints();
		final int lLastNodeId = (int) ((pCar.carProgress().lastVisitedNodeId + 1 >= lNumControlNodes) ? 0 : pCar.carProgress().lastVisitedNodeId);

		return lTrack.trackSpline().getNormalizedPositionAlongSpline(lLastNodeId, pCar.x, pCar.y);
	}

	private float getCarDistanceIntoSegment(Car pCar) {
		final var lTrack = mTrackController.currentTrack();

		final int lNumControlNodes = lTrack.trackSpline().numberSplineControlPoints();
		final int lLastNodeId = (int) ((pCar.carProgress().lastVisitedNodeId + 1 >= lNumControlNodes) ? 0 : pCar.carProgress().lastVisitedNodeId);
		final var lSegment = lTrack.trackSpline().getControlPoint(lLastNodeId);

		return lTrack.trackSpline().getNormalizedPositionAlongSpline(lLastNodeId, pCar.x, pCar.y) * lSegment.length;
	}

	private float getTrackGradientAtVehicleLocation(Car pCar) {
		final var lTrack = mTrackController.currentTrack();

		final int lNumControlNodes = lTrack.trackSpline().numberSplineControlPoints();
		final int lLastNodeId = (int) ((pCar.carProgress().lastVisitedNodeId >= lNumControlNodes) ? 0 : pCar.carProgress().lastVisitedNodeId);
		final int lNextNodeId = (int) ((lLastNodeId + 1 >= lNumControlNodes) ? 0 : lLastNodeId + 1);

		final var lCarPositionAlongSpling = getCarDistanceIntoSegmentNormalized(pCar);

		SplinePoint lTrackSplinePoint = lTrack.trackSpline().getPointOnSpline(lLastNodeId + lCarPositionAlongSpling);

		final float lNode0X = lTrackSplinePoint.x;
		final float lNode0Y = lTrackSplinePoint.y;

		lTrackSplinePoint = lTrack.trackSpline().getControlPoint(lNextNodeId);

		final float lNode1X = lTrackSplinePoint.x;
		final float lNode1Y = lTrackSplinePoint.y;

		final float lHeadingVecX = lNode1X - lNode0X;
		final float lHeadingVecY = lNode1Y - lNode0Y;

		return (float) Math.atan2(lHeadingVecX, -lHeadingVecY);
	}
}
