package net.lintford.ld46.data.cars;

import java.util.ArrayList;
import java.util.List;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.entity.JBox2dEntity;

// tutorial here: http://www.iforce2d.net/b2dtut/top-down-car
public class Car extends JBox2dEntity {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 8546363285956254711L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private CarInput mCarInput;
	private List<CarWheel> mWheels;

	private float mMaxForwardSpeed;
	private float mMaxBackwardSpeed;
	private float mMaxDriveForce;

	private float mMaxLateralForce;
	private float mSteeringAngleLockDeg;
	private float mTurnSpeedPerSecond;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

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

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public Car(int pPoolUid) {
		super(pPoolUid);

		mCarInput = new CarInput();
		mWheels = new ArrayList<>();

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

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

		final int lNumWheels = mWheels.size();
		for (int i = 0; i < lNumWheels; i++) {
			mWheels.get(i).update(pCore);
		}

		mCarInput.reset();

		// TEST

		final var lBody = mJBox2dEntityInstance.mainBody().mBody;
		Vec2 mTempVec2 = new Vec2();
		Vec2 mLatVec2 = new Vec2();

		mTempVec2.set(lBody.getWorldVector(new Vec2(.1f, 0.f)));
		final float lDotRightNormal = Vec2.dot(mTempVec2, lBody.getLinearVelocity());
		mLatVec2.set(mTempVec2.x * lDotRightNormal, mTempVec2.y * lDotRightNormal);

		float lMass = 2.f;

		// Offset the lateral force to prevent sideways movement of the wheels
		Vec2 lImpulse = new Vec2();

		// but first allow for some amount of skidding, by limiting the maximum return force
		lImpulse.set(-mLatVec2.x * lMass, -mLatVec2.y * lMass);
		final var lMaxLateralForce = maxLateralForce();
		if (lImpulse.length() > lMaxLateralForce)
			lImpulse.set(lImpulse.mul(lMaxLateralForce / lImpulse.length()));

		lBody.applyLinearImpulse(lImpulse, lBody.getWorldCenter(), true);

		// reduce the angular velocity of the wheel
		// with the follow, the vehicle is stable
		// lBody.applyAngularImpulse(1f * lBody.getInertia() * -lBody.getAngularVelocity());
		lBody.applyAngularImpulse(.4f * lBody.getInertia() * -lBody.getAngularVelocity());

		//

	}

	public void draw(LintfordCore pCore) {
		final int lNumWheels = mWheels.size();
		for (int i = 0; i < lNumWheels; i++) {
			mWheels.get(i).draw(pCore);
		}

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

}
