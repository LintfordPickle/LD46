package net.lintford.ld46.data.cars;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.entity.JBox2dEntity;
import net.lintford.library.core.maths.MathHelper;

// tutorial here: http://www.iforce2d.net/b2dtut/top-down-car
public class Car extends JBox2dEntity {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 8546363285956254711L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private CarEngine mCarEngine;
	private CarProgress mCarProgress;
	private CarInput mCarInput;
	private List<CarWheel> mWheels;
	private boolean mIsDestroyed;

	public boolean isPlayerCar;

	private float mMaxForwardSpeed;
	private float mMaxBackwardSpeed;
	private float mMaxDriveForce;

	private float mMaxLateralForce;
	private float mSteeringAngleDeg;
	private float mSteeringAngleLockDeg;
	private float mTurnSpeedPerSecond;

	private float mCurrentSpeed;

	public float pointOnTrackX;
	public float pointOnTrackY;
	public float trackAngle;
	public float wheelAngle;
	public float aiHeadingAngle;

	public float mLastCrashResolverUpdateX;
	public float mLastCrashResolverUpdateY;
	public float mLastCrashResolverCounter;
	public float mLastCrashResolverUpdateTime;

	private Vec2 mImpulseVector = new Vec2();
	private Vec2 mForwardVelocity = new Vec2();

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public List<CarWheel> wheels() {
		return mWheels;
	}

	public float currentSpeed() {
		return mCurrentSpeed;
	}

	public boolean isDestroyed() {
		return mIsDestroyed;
	}

	public void isDestroyed(boolean pNewDestroyedState) {
		mIsDestroyed = pNewDestroyedState;
	}

	public CarProgress carProgress() {
		return mCarProgress;
	}

	public CarInput input() {
		return mCarInput;
	}

	public float maxLateralForce() {
		return mMaxLateralForce;
	}

	public float maxDriveForce() {
		return mMaxDriveForce;
	}

	public float maxBackwardSpeed() {
		return mMaxBackwardSpeed;
	}

	public float maxForwardSpeed() {
		return mMaxForwardSpeed;
	}

	public float steeringAngleLockDeg() {
		return mSteeringAngleLockDeg;
	}

	public float steeringAngleDeg() {
		return mSteeringAngleDeg;
	}

	public void steeringAngleDeg(float pNewSteeringAngleDeg) {
		mSteeringAngleDeg = pNewSteeringAngleDeg;
	}

	public float turnSpeedPerSecond() {
		return mTurnSpeedPerSecond;
	}

	public void setCarDriveProperties(float pMaxForwardSpeed, float pMaxBackwardSpeed, float pMaxDriveForce) {
		mMaxForwardSpeed = pMaxForwardSpeed;
		mMaxBackwardSpeed = pMaxBackwardSpeed;
		mMaxDriveForce = pMaxDriveForce;

	}

	public void setCarSteeringProperties(float pMaxLateralForce, float pSteeringAngleLockDeg, float pTurnSpeedPerSecondDeg) {
		mMaxLateralForce = pMaxLateralForce;
		mSteeringAngleLockDeg = pSteeringAngleLockDeg;
		mTurnSpeedPerSecond = pTurnSpeedPerSecondDeg;

	}

	public float currentSpeedNormalized() {
		return MathHelper.clamp(mCurrentSpeed / 100.f, 0.f, 1.f);
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public Car(int pPoolUid) {
		super(pPoolUid);

		mCarProgress = new CarProgress(0, 0);
		mCarEngine = new CarEngine();

		mCarInput = new CarInput();
		mWheels = new ArrayList<>();

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	public void loadContent(ResourceManager pResourceManager) {
		mCarEngine.loadContent(pResourceManager);

	}

	public void unloadContent() {
		mCarEngine.unloadContent();
	}

	@Override
	public void loadPhysics(World pWorld) {
		super.loadPhysics(pWorld);

		// add hooks for box2d bodies
		final var lFrontLeftWheel = mJBox2dEntityInstance.getBodyByName("WheelFrontLeft");
		if (lFrontLeftWheel != null) {
			final var lNewCarWheel = new CarWheel(this, true);
			lNewCarWheel.mBox2dBodyInstance = lFrontLeftWheel;
			mWheels.add(lNewCarWheel);

			final var lFrontLeftJoint = mJBox2dEntityInstance.getJointByName("FrontLeftJoint");
			if (lFrontLeftJoint != null) {
				lNewCarWheel.mBox2dJointInstance = lFrontLeftJoint;
			}

		}

		final var lFrontRightWheel = mJBox2dEntityInstance.getBodyByName("WheelFrontRight");
		if (lFrontRightWheel != null) {
			final var lNewCarWheel = new CarWheel(this, true);
			lNewCarWheel.mBox2dBodyInstance = lFrontRightWheel;
			mWheels.add(lNewCarWheel);

			final var lFrontRightJoint = mJBox2dEntityInstance.getJointByName("FrontRightJoint");
			if (lFrontRightJoint != null) {
				lNewCarWheel.mBox2dJointInstance = lFrontRightJoint;
			}

		}

		final var lRearLeftWheel = mJBox2dEntityInstance.getBodyByName("WheelRearLeft");
		if (lRearLeftWheel != null) {
			final var lNewCarWheel = new CarWheel(this, false);
			lNewCarWheel.mBox2dBodyInstance = lRearLeftWheel;
			mWheels.add(lNewCarWheel);

			// Not really needed for non steerable wheels
			final var lRearLeftJoint = mJBox2dEntityInstance.getJointByName("RearLeftJoint");
			if (lRearLeftJoint != null) {
				lNewCarWheel.mBox2dJointInstance = lRearLeftJoint;
			}

		}

		final var lRearRightWheel = mJBox2dEntityInstance.getBodyByName("WheelRearRight");
		if (lRearRightWheel != null) {
			final var lNewCarWheel = new CarWheel(this, false);
			lNewCarWheel.mBox2dBodyInstance = lRearRightWheel;
			mWheels.add(lNewCarWheel);

			// Not really needed for non steerable wheels
			final var lRearRightJoint = mJBox2dEntityInstance.getJointByName("RearRightJoint");
			if (lRearRightJoint != null) {
				lNewCarWheel.mBox2dJointInstance = lRearRightJoint;
			}

		}

	}

	@Override
	public void updatePhyics(LintfordCore pCore) {
		super.updatePhyics(pCore);

		// TODO: Move
		mCarEngine.update(pCore, this);

		if (mCarProgress.hasCarFinished) {
			mCarEngine.killEngine();

		}

		mSteeringAngleDeg = MathHelper.clamp(mSteeringAngleDeg, -mSteeringAngleLockDeg, mSteeringAngleLockDeg);

		final int lNumWheels = mWheels.size();
		for (int i = 0; i < lNumWheels; i++) {
			mWheels.get(i).update(pCore);
		}

		// mCarInput.reset();

		// TEST

		final var lBody = mJBox2dEntityInstance.mainBody().mBody;
		Vec2 mTempVec2 = new Vec2();
		Vec2 mLatVec2 = new Vec2();

		mTempVec2.set(lBody.getWorldVector(new Vec2(.1f, 0.f)));
		final float lDotRightNormal = Vec2.dot(mTempVec2, lBody.getLinearVelocity());
		mLatVec2.set(mTempVec2.x * lDotRightNormal, mTempVec2.y * lDotRightNormal);

		float lMass = 2.f;

		// Offset the lateral force to prevent sideways movement of the wheels
		// but first allow for some amount of skidding, by limiting the maximum return force
		mImpulseVector.set(-mLatVec2.x * lMass, -mLatVec2.y * lMass);
		final var lMaxLateralForce = maxLateralForce();
		if (mImpulseVector.length() > lMaxLateralForce)
			mImpulseVector.set(mImpulseVector.mul(lMaxLateralForce / mImpulseVector.length()));

		lBody.applyLinearImpulse(mImpulseVector, lBody.getWorldCenter(), true);
		lBody.applyAngularImpulse(.4f * lBody.getInertia() * -lBody.getAngularVelocity());

		mTempVec2.set(lBody.getWorldVector(CarWheel.FORWARD_VECTOR));
		final float lDotForwardNormal = Vec2.dot(lBody.getLinearVelocity(), mTempVec2);
		mForwardVelocity.set(mTempVec2.x * lDotForwardNormal, mTempVec2.y * lDotForwardNormal);

		// Get current speed
		mTempVec2.set(lBody.getWorldVector(CarWheel.FORWARD_VECTOR));
		mCurrentSpeed = Vec2.dot(mTempVec2, mForwardVelocity);

	}

}
