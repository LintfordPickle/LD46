package net.lintford.ld46.data.cars;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.joints.RevoluteJoint;

import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.box2d.entity.Box2dBodyInstance;
import net.lintford.library.core.box2d.entity.Box2dJointInstance;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.maths.MathHelper;

public class CarWheel {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final Vec2 FORWARD_VECTOR = new Vec2(0.f, -1.f);
	private static final Vec2 RIGHT_VECTOR = new Vec2(1.f, 0.f);

	// --------------------------------------
	// Variables
	// --------------------------------------

	public Box2dBodyInstance mBox2dBodyInstance;
	public Box2dJointInstance mBox2dJointInstance;
	public boolean isFrontWheel; // is Steerable?
	public final Car mParentCar;

	private Vec2 mTempVec2;
	public Vec2 mLateralVelocity;
	public Vec2 mForwardVelocity;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public CarWheel(Car pParentCar, boolean pIsFrontWheel) {
		mParentCar = pParentCar;
		isFrontWheel = pIsFrontWheel;

		mTempVec2 = new Vec2();
		mLateralVelocity = new Vec2();
		mForwardVelocity = new Vec2();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	public void update(LintfordCore pCore) {
		final var lBody = mBox2dBodyInstance.mBody;
		if (lBody == null) {
			return;

		}

		updateForwardVelocity(lBody);
		updateLateralVelocity(lBody);
		updateFriction(lBody);

		if (isFrontWheel) {

			updateDrive(lBody);

			{
				if (mBox2dJointInstance != null) {
					final var lWheelJoint = mBox2dJointInstance.joint;
					if (lWheelJoint != null && lWheelJoint instanceof RevoluteJoint) {
						final var lRevJoint = (RevoluteJoint) lWheelJoint;
						updateTurn(lBody, lRevJoint);

					}

				}

			}

		}

	}

	public void draw(LintfordCore pCore) {
		final var lBody = mBox2dBodyInstance.mBody;
		if (lBody == null) {
			return;
		}

		final var lX = lBody.getWorldCenter().x * Box2dWorldController.UNITS_TO_PIXELS;
		final var lY = lBody.getWorldCenter().y * Box2dWorldController.UNITS_TO_PIXELS;

		mTempVec2.set(mForwardVelocity);
		mTempVec2.normalize();
		mTempVec2.mul(2.f);
		final var lFX = lX + mTempVec2.x * Box2dWorldController.UNITS_TO_PIXELS;
		final var lFY = lY + mTempVec2.y * Box2dWorldController.UNITS_TO_PIXELS;

		mTempVec2.set(mLateralVelocity);
		mTempVec2.normalize();
		mTempVec2.mul(2.f);
		final var lLX = lX + mTempVec2.x * Box2dWorldController.UNITS_TO_PIXELS;
		final var lLY = lY + mTempVec2.y * Box2dWorldController.UNITS_TO_PIXELS;

		Debug.debugManager().drawers().drawLineImmediate(pCore.gameCamera(), lX, lY, lFX, lFY, -0.01f, 1f, 0f, 0f);
		Debug.debugManager().drawers().drawLineImmediate(pCore.gameCamera(), lX, lY, lLX, lLY, -0.01f, 1f, 1f, 0f);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	private void updateFriction(Body pBody) {
		float lMass = pBody.getMass();

		// Offset the lateral force to prevent sideways movement of the wheels
		Vec2 lImpulse = mTempVec2;

		// but first allow for some amount of skidding, by limiting the maximum return force
		lImpulse.set(-mLateralVelocity.x * lMass, -mLateralVelocity.y * lMass);
		final var lMaxLateralForce = mParentCar.maxLateralForce();
		if (lImpulse.length() > lMaxLateralForce)
			lImpulse.set(lImpulse.mul(lMaxLateralForce / lImpulse.length()));

		pBody.applyLinearImpulse(lImpulse, pBody.getWorldCenter(), true);

		// reduce the angular velocity of the wheel
		pBody.applyAngularImpulse(.1f * pBody.getInertia() * -pBody.getAngularVelocity());

	}

	private void updateDrive(Body pBody) {
		float lDesiredSpeed = 0.f;
		if (mParentCar.input().isGas)
			lDesiredSpeed = mParentCar.maxForwardSpeed();
		else if (mParentCar.input().isBrake)
			lDesiredSpeed = mParentCar.maxBackwardSpeed();
		else
			return; // do nothing on no input

		// Get current speed in forward direction
		mTempVec2.set(pBody.getWorldVector(FORWARD_VECTOR));
		float lCurrentSpeed = Vec2.dot(mTempVec2, mForwardVelocity);

		// apply force if needed
		float lForce = 0.f;
		if (lDesiredSpeed > lCurrentSpeed)
			lForce = mParentCar.maxDriveForce();
		else if (lDesiredSpeed < lCurrentSpeed)
			lForce = -mParentCar.maxDriveForce();
		else
			return;

		pBody.applyForce(mTempVec2.mul(lForce), pBody.getWorldCenter());

	}

	private void updateTurn(Body pBody, RevoluteJoint pWheeljoint) {
		final float lLockAngleInDegrees = mParentCar.steeringAngleLockDeg();
		float lLockAngle = (float) Math.toRadians(lLockAngleInDegrees);

		float lTurnSpeedPerSecond = (float) Math.toRadians(mParentCar.turnSpeedPerSecond());
		float lTurnPerTimeStep = lTurnSpeedPerSecond / 60.f;

		float lDesiredAngle = 0.f;
		if (mParentCar.input().isTurningLeft)
			lDesiredAngle = lLockAngle;
		else if (mParentCar.input().isTurningRight)
			lDesiredAngle = -lLockAngle;
		// else return;

		float lCurrentAngle = pWheeljoint.getJointAngle();
		float lAngleToTurn = lDesiredAngle - lCurrentAngle;
		lAngleToTurn = MathHelper.clamp(lAngleToTurn, -lTurnPerTimeStep, lTurnPerTimeStep);

		float lNewAngle = lCurrentAngle + lAngleToTurn;
		pWheeljoint.setLimits(lNewAngle, lNewAngle);
		pWheeljoint.enableLimit(true);

		float lDesiredTorque = 0.f;
		if (mParentCar.input().isTurningLeft)
			lDesiredTorque = -15.f;
		else if (mParentCar.input().isTurningRight)
			lDesiredTorque = 15.f;
		else
			return;

		pBody.applyTorque(lDesiredTorque);

	}

	private void updateForwardVelocity(Body pBody) {
		mTempVec2.set(pBody.getWorldVector(FORWARD_VECTOR));
		final float lDotForwardNormal = Vec2.dot(pBody.getLinearVelocity(), mTempVec2);
		mForwardVelocity.set(mTempVec2.x * lDotForwardNormal, mTempVec2.y * lDotForwardNormal);
	}

	private void updateLateralVelocity(Body pBody) {
		mTempVec2.set(pBody.getWorldVector(RIGHT_VECTOR));
		final float lDotRightNormal = Vec2.dot(mTempVec2, pBody.getLinearVelocity());
		mLateralVelocity.set(mTempVec2.x * lDotRightNormal, mTempVec2.y * lDotRightNormal);
	}

}
