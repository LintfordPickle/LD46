package net.lintford.ld46.controllers;

import org.lwjgl.glfw.GLFW;

import net.lintford.ld46.data.cars.Car;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.camera.ICamera;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.Vector2f;

public class CameraCarChaseController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "Camera Car Chase Controller";

	private static final float CAMERA_MAN_MOVE_SPEED = 0.2f;
	
	private static final float MIN_ZOOM = 0.8f;
	private static final float MAX_ZOOM = 0.25f;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private ICamera mGameCamera;
	private Car mTrackedEntity;
	private boolean mAllowManualControl;
	private boolean mIsTrackingPlayer;

	private Vector2f mVelocity;
	public Vector2f mDesiredPosition;
	public Vector2f mPosition;
	public Vector2f mLookAhead;

	public float mZoomFactor;
	public float mZoomVelocity;

	private float mStiffness = 18.0f;
	private float mDamping = 6.0f;
	private float mMass = .5f;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public ICamera gameCamera() {
		return mGameCamera;
	}

	public boolean trackPlayer() {
		return mIsTrackingPlayer;
	}

	public void trackPlayer(boolean pNewValue) {
		mIsTrackingPlayer = pNewValue;
	}

	public boolean allowManualControl() {
		return mAllowManualControl;
	}

	public void allowManualControl(boolean pNewValue) {
		mAllowManualControl = pNewValue;
	}

	@Override
	public boolean isinitialized() {
		return mGameCamera != null;

	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public CameraCarChaseController(ControllerManager pControllerManager, ICamera pCamera, Car pTrackedCar, int pControllerGroup) {
		super(pControllerManager, CONTROLLER_NAME, pControllerGroup);

		mVelocity = new Vector2f();
		mDesiredPosition = new Vector2f();
		mPosition = new Vector2f();
		mLookAhead = new Vector2f();

		mPosition.x = pTrackedCar.x;
		mPosition.y = pTrackedCar.y;

		//
		mGameCamera = pCamera;
		mTrackedEntity = pTrackedCar;
		mIsTrackingPlayer = true;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {

	}

	// FIXME: Duplicate initialize method - rename to something else or clean up design!
	public void initialize(ICamera pGameCamera, Car pTrackedEntity) {
		mGameCamera = pGameCamera;
		mTrackedEntity = pTrackedEntity;

	}

	@Override
	public void unload() {

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		if (mGameCamera == null)
			return false;

		if (mAllowManualControl) {
			final float speed = CAMERA_MAN_MOVE_SPEED;

			// Just listener for clicks - couldn't be easier !!?!
			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_A)) {
				mVelocity.x -= speed;
				mIsTrackingPlayer = false;

			}

			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_D)) {
				mVelocity.x += speed;
				mIsTrackingPlayer = false;

			}

			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_S)) {
				mVelocity.y += speed;
				mIsTrackingPlayer = false;

			}

			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_W)) {
				mVelocity.y -= speed;
				mIsTrackingPlayer = false;

			}

		}

		return false;

	}

	@Override
	public void update(LintfordCore pCore) {
		if (mGameCamera == null)
			return;

		if (mTrackedEntity != null) {
			updateSpring(pCore);

			mGameCamera.setPosition(-mPosition.x, -mPosition.y);

		}

	}

	private void updateSpring(LintfordCore pCore) {
		updatewWorldPositions(pCore);
		updateWorldZoomFactor(pCore);

		float elapsed = (float) pCore.time().elapseGameTimeSeconds();

		// Calculate spring force
		float stretchX = mPosition.x - mDesiredPosition.x;
		float stretchY = mPosition.y - mDesiredPosition.y;

		float forceX = -mStiffness * stretchX - mDamping * mVelocity.x;
		float forceY = -mStiffness * stretchY - mDamping * mVelocity.y;

		// Apply acceleration
		float accelerationX = forceX / mMass;
		float accelerationY = forceY / mMass;

		mVelocity.x += accelerationX * elapsed;
		mVelocity.y += accelerationY * elapsed;

		// Apply velocity
		mPosition.x += mVelocity.x * elapsed;
		mPosition.y += mVelocity.y * elapsed;

	}

	private void updatewWorldPositions(LintfordCore pCore) {
		float lAngle = mTrackedEntity.r + (float) Math.toRadians(-90.f);
		mLookAhead.x = (float) Math.cos(lAngle);
		mLookAhead.y = (float) Math.sin(lAngle);

		float lSpeedMod = mTrackedEntity.currentSpeed() * 20.f;
		mDesiredPosition.x = mTrackedEntity.x + mLookAhead.x * lSpeedMod;
		mDesiredPosition.y = mTrackedEntity.y + mLookAhead.y * lSpeedMod;

	}

	private void updateWorldZoomFactor(LintfordCore pCore) {
		float lTargetZoom = (100f / mTrackedEntity.currentSpeed()) / 10f;
		lTargetZoom = MathHelper.clamp(lTargetZoom, 0.25f, 1.0f);

		final float lVelStepSize = 0.0075f;

		if (lTargetZoom > mZoomFactor) {
			mZoomVelocity += lVelStepSize;

		} else {
			mZoomVelocity -= lVelStepSize;

		}

		mZoomFactor += mZoomVelocity * pCore.time().elapseGameTimeMilli() * 0.001f;
		mZoomVelocity *= 0.987f;
		mZoomVelocity = MathHelper.clamp(mZoomVelocity, -0.25f, 0.25f);
		mZoomFactor = MathHelper.clamp(mZoomFactor, MAX_ZOOM, MIN_ZOOM);
		
		mGameCamera.setZoomFactor(mZoomFactor);
	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void zoomIn(float pZoomFactor) {
		mGameCamera.setZoomFactor(pZoomFactor);

	}

}
