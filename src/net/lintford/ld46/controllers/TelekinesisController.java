package net.lintford.ld46.controllers;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

import net.lintford.ld46.data.TelekinesisManager;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.MathHelper;

public class TelekinesisController extends BaseController {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String CONTROLLER_NAME = "Telekinesis Controller";

	final float MAX_DISTANCE_FOR_TELEKINESIS = 10000.0f;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private TelekinesisManager mTelekinesisManager;
	private CarController mCarController;

	// --------------------------------------
	// Properties
	// --------------------------------------

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

	private void getNextCarOrCancel() {
		final int lNumOpponents = mCarController.carManager().numberOfActiveOpponents();

		if (lNumOpponents == 0)
			return;

		boolean lFound = false;
		while (!lFound) {
			mTelekinesisManager.mSelectedOpponentIndex++;

			if (mTelekinesisManager.mSelectedOpponentIndex >= lNumOpponents) {
				mTelekinesisManager.mSelectedOpponentIndex = -1;
				disableTelekinesis();
				return;

			}

			final var lPlayerCar = mCarController.carManager().playerCar();
			final var lOpponentCar = mCarController.carManager().opponents().get(mTelekinesisManager.mSelectedOpponentIndex);
			float lDistToCar = Vector2f.distance(lPlayerCar.x, lPlayerCar.y, lOpponentCar.x, lOpponentCar.y);
			if (lDistToCar < MAX_DISTANCE_FOR_TELEKINESIS) {
				return;
			}

		}

	}

	private void getPrevCarOrCancel() {
		final int lNumOpponents = mCarController.carManager().numberOfActiveOpponents();

		if (lNumOpponents == 0)
			return;

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

			final var lPlayerCar = mCarController.carManager().playerCar();
			final var lOpponentCar = mCarController.carManager().opponents().get(mTelekinesisManager.mSelectedOpponentIndex);
			float lDistToCar = Vector2f.distance(lPlayerCar.x, lPlayerCar.y, lOpponentCar.x, lOpponentCar.y);
			if (lDistToCar < MAX_DISTANCE_FOR_TELEKINESIS) {
				lFound = true; // noone selected
			}

		}

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		final int lNumOpponents = mCarController.carManager().opponents().size();
		if (lNumOpponents == 0) {
			mTelekinesisManager.isInTelekinesesMode = false;
			return;

		}

		final float lDelta = (float) pCore.time().elapseGameTimeMilli();
		final var lPlayerCar = mCarController.carManager().playerCar();

		if (mTelekinesisManager.isInTelekinesesMode) {
			final int lOpponentCarId = mTelekinesisManager.mSelectedOpponentIndex;

			final var lOpponentCar = mCarController.carManager().opponents().get(lOpponentCarId);
			if (lOpponentCar != null) {
				lOpponentCar.input().copyFrom(mCarController.carManager().playerCar().input());

				float lDistToCar = Vector2f.distance(lPlayerCar.x, lPlayerCar.y, lOpponentCar.x, lOpponentCar.y);

				final float lUsageDrainAmt = 0.1f * MathHelper.clamp(lDistToCar / MAX_DISTANCE_FOR_TELEKINESIS, 0.f, 1.f);
				mTelekinesisManager.currentPower -= lUsageDrainAmt * lDelta;

				if (mTelekinesisManager.currentPower < 0.0f) {
					mTelekinesisManager.currentPower = 0.0f;

					disableTelekinesis();

				}
			} else
				disableTelekinesis();

		} else {
			if (mTelekinesisManager.currentPower < mTelekinesisManager.maxPower) {
				final float lRegenAmt = 0.05f;
				mTelekinesisManager.currentPower += lRegenAmt * lDelta;

				if (mTelekinesisManager.currentPower > mTelekinesisManager.maxPower) {
					mTelekinesisManager.currentPower = mTelekinesisManager.maxPower;
				}

			}

		}

	}

	public void disableTelekinesis() {
		mTelekinesisManager.isInTelekinesesMode = false;
		mTelekinesisManager.mSelectedOpponentIndex = -1;
	}

}
