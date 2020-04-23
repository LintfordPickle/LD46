package net.lintford.ld46.controllers;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import net.lintford.ld46.data.TelekinesisManager;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;

public class TelekinesisController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "Telekinesis Controller";

	final float MAX_DISTANCE_FOR_TELEKINESIS = 10000.0f;

	public static final float DRAINAGE_RATE = 0.01f;
	public static final float REGEN_RATE = 0.05f;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private TelekinesisManager mTelekinesisManager;
	private CarController mCarController;

	private float mTimeControlModifier;
	private float mTimeControlModifierVelocity;
	private float mTimeControlModifierTarget;

	// --------------------------------------
	// Properties
	// --------------------------------------

	public boolean isInTelekinesisMode() {
		return mTelekinesisManager.isInTelekinesesMode;
	}

	public int selectedCarIndex() {
		return mTelekinesisManager.mSelectedOpponentIndex;
	}

	public float currentTimeControlModifier() {
		return mTimeControlModifier;

	}

	public TelekinesisManager telekinesisManager() {
		return mTelekinesisManager;
	}

	@Override
	public boolean isinitialized() {
		return false;

	}

	public float maxTelekinesisPower() {
		return mTelekinesisManager.maxPower;
	}

	public float currentTelekinesisPower() {
		return mTelekinesisManager.currentPower;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public TelekinesisController(ControllerManager pControllerManager, TelekinesisManager pTelekinesisManager, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mTelekinesisManager = pTelekinesisManager;
		mTelekinesisManager.isInTelekinesesMode = false;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mCarController = (CarController) (pCore.controllerManager().getControllerByNameRequired(CarController.CONTROLLER_NAME, entityGroupID()));

	}

	@Override
	public void unload() {

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		final var lKeyboard = pCore.input().keyboard();
		final boolean isCancelEvent = lKeyboard.isKeyDown(GLFW.GLFW_KEY_DOWN) || lKeyboard.isKeyDown(GLFW.GLFW_KEY_UP);

		if (isCancelEvent) {
			disableTelekinesis();

		} else {
			if (lKeyboard.isKeyDownTimed(GLFW.GLFW_KEY_LEFT)) {
				getPrevCarOrCancel();

			} else if (lKeyboard.isKeyDownTimed(GLFW.GLFW_KEY_RIGHT)) {
				getNextCarOrCancel();

			}

			mTelekinesisManager.isInTelekinesesMode = mTelekinesisManager.mSelectedOpponentIndex != -1;

		}

		return super.handleInput(pCore);
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		updateTimeControl(pCore);

		final int lNumOpponents = mCarController.carManager().cars().size();
		if (lNumOpponents == 0) {
			mTelekinesisManager.isInTelekinesesMode = false;
			return;

		}

		updateTelekinesis(pCore);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	private void updateTelekinesis(LintfordCore pCore) {
		final float lDelta = (float) pCore.gameTime().elapseTimeMilli();

		if (mTelekinesisManager.isInTelekinesesMode) {
			final int lOpponentCarId = mTelekinesisManager.mSelectedOpponentIndex;

			final var lOpponentCar = mCarController.carManager().cars().get(lOpponentCarId);
			if (lOpponentCar != null) {
				lOpponentCar.input().copyFrom(mCarController.carManager().playerCar().input());

				final float lUsageDrainAmt = DRAINAGE_RATE; // * MathHelper.clamp(lDistToCar / MAX_DISTANCE_FOR_TELEKINESIS, 0.f, 1.f);
				mTelekinesisManager.currentPower -= lUsageDrainAmt * lDelta;

				if (mTelekinesisManager.currentPower < 0.0f) {
					mTelekinesisManager.currentPower = 0.0f;

					disableTelekinesis();

				}
			} else
				disableTelekinesis();

		} else {
			if (mTelekinesisManager.currentPower < mTelekinesisManager.maxPower) {
				final float lRegenAmt = REGEN_RATE;
				mTelekinesisManager.currentPower += lRegenAmt * lDelta;

				if (mTelekinesisManager.currentPower > mTelekinesisManager.maxPower) {
					mTelekinesisManager.currentPower = mTelekinesisManager.maxPower;
				}

			}

		}
	}

	private void updateTimeControl(LintfordCore pCore) {
		final float lMaxModiferValue = 1.0f;
		final float lMinModiferValue = 0.25f;
		mTimeControlModifierTarget = mTelekinesisManager.isInTelekinesesMode ? lMinModiferValue : lMaxModiferValue;

		final float lDelta = (float) pCore.appTime().elapseTimeMilli() * 0.001f;
		final float lVelStepSize = 0.05f;

		if (mTimeControlModifier > mTimeControlModifierTarget) {
			mTimeControlModifierVelocity -= lVelStepSize * lDelta;

		} else if (mTimeControlModifier < mTimeControlModifierTarget) {
			mTimeControlModifierVelocity += lVelStepSize * lDelta;

		}

		mTimeControlModifier += mTimeControlModifierVelocity;

		if (mTimeControlModifier < lMinModiferValue) {
			mTimeControlModifier = lMinModiferValue;
			mTimeControlModifierVelocity = 0.f;
		}

		if (mTimeControlModifier > lMaxModiferValue) {
			mTimeControlModifier = lMaxModiferValue;
			mTimeControlModifierVelocity = 0.f;
		}

		pCore.gameTime().setGameTimeModifier(mTimeControlModifier);

		mTimeControlModifierVelocity *= .95f;

	}

	private void getNextCarOrCancel() {
		final int lNumOpponents = mCarController.carManager().numberOfCars();

		if (lNumOpponents == 0)
			return;

		final var lCarManager = mCarController.carManager();

		boolean lFound = false;
		while (!lFound) {
			mTelekinesisManager.mSelectedOpponentIndex++;

			if (mTelekinesisManager.mSelectedOpponentIndex >= lNumOpponents) {
				mTelekinesisManager.mSelectedOpponentIndex = -1;
				disableTelekinesis();
				return;

			}

			final var lPlayerCar = lCarManager.playerCar();
			final var lOpponentCar = lCarManager.cars().get(mTelekinesisManager.mSelectedOpponentIndex);
			if (!lOpponentCar.equals(lPlayerCar)) {
				float lDistToCar = Vector2f.distance(lPlayerCar.x, lPlayerCar.y, lOpponentCar.x, lOpponentCar.y);
				if (lDistToCar < MAX_DISTANCE_FOR_TELEKINESIS) {
					mCarController.carManager().telekinesisCar(lOpponentCar);
					return;
				}

			}

		}

	}

	private void getPrevCarOrCancel() {
		final int lNumOpponents = mCarController.carManager().numberOfCars();

		if (lNumOpponents == 0)
			return;

		final var lCarManager = mCarController.carManager();

		boolean lFound = false;
		while (!lFound) {
			mTelekinesisManager.mSelectedOpponentIndex--;

			if (mTelekinesisManager.mSelectedOpponentIndex == -1) {
				disableTelekinesis();
				return;
			}

			if (mTelekinesisManager.mSelectedOpponentIndex < -1) {
				mTelekinesisManager.mSelectedOpponentIndex = lNumOpponents - 1;

			}

			final var lPlayerCar = lCarManager.playerCar();
			final var lOpponentCar = lCarManager.cars().get(mTelekinesisManager.mSelectedOpponentIndex);
			if (!lOpponentCar.equals(lPlayerCar)) {
				float lDistToCar = Vector2f.distance(lPlayerCar.x, lPlayerCar.y, lOpponentCar.x, lOpponentCar.y);
				if (lDistToCar < MAX_DISTANCE_FOR_TELEKINESIS) {
					lCarManager.telekinesisCar(lOpponentCar);
					lFound = true; // noone selected
				}

			}

		}

	}

	public void disableTelekinesis() {
		mTelekinesisManager.isInTelekinesesMode = false;
		mTelekinesisManager.mSelectedOpponentIndex = -1;

		mCarController.carManager().telekinesisCar(null);
	}

}
