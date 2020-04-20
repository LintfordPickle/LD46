package net.lintford.ld46.controllers;

import net.lintford.ld46.data.GameWorld;
import net.lintford.ld46.data.cars.Car;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;

public class GameStateController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final int END_CONDITION_NOT_SET = 0;
	public static final int END_CONDITION_DESTROYED = 1;
	public static final int END_CONDITION_WON_RACING = 2;
	public static final int END_CONDITION_LOST = 3;
	public static final int END_CONDITION_WON_FIGHTING = 4;

	public static final String CONTROLLER_NAME = "GameState Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private GameWorld mGameWorld;

	private int mTotalLaps;

	private int mNumberCarsFinished;
	private int mEndConditionFlag;
	private boolean mIsGameFinished;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean isGameFinished() {
		return mIsGameFinished;
	}

	public int getEndConditionFlag() {
		return mEndConditionFlag;
	}

	public int totalLaps() {
		return mTotalLaps;
	}

	public int totalRacers() {
		return mGameWorld.carManager().numberOfActiveOpponents();
	}

	@Override
	public boolean isinitialized() {
		return mGameWorld != null;
	}

	public GameWorld gameWorld() {
		return mGameWorld;
	}

	public Car playerCar() {
		return mGameWorld.carManager().playerCar();
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameStateController(ControllerManager pControllerManager, GameWorld pGameWorld, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mGameWorld = pGameWorld;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {

	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		return super.handleInput(pCore);

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		updatePositions();

		if (checkPlayerEndRaceCondition()) {
			mIsGameFinished = true;

		}

		if (checkLoseCondition()) { // destroyed
			mIsGameFinished = true;

		}

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void startNewGame() {
		mTotalLaps = 3;

	}

	private void updatePositions() {

		final var lOpponentVehicles = mGameWorld.carManager().opponents();
		final int lNumVehicles = lOpponentVehicles.size();
		for (int i = 0; i < lNumVehicles; i++) {
			final var lCar = lOpponentVehicles.get(i);

			if (!lCar.carProgress().hasCarFinished && lCar.carProgress().currentLapNumber > mTotalLaps) {
				lCar.carProgress().hasCarFinished = true;
				mNumberCarsFinished++;

			}

		}

	}

	private boolean checkPlayerEndRaceCondition() {
		final var lPlayerCar = mGameWorld.carManager().playerCar();

		lPlayerCar.carProgress().hasCarFinished = lPlayerCar.carProgress().currentLapNumber > mTotalLaps;

		if (lPlayerCar.carProgress().hasCarFinished) {
			if (checkWinCondition()) {

			} else {
				mEndConditionFlag = END_CONDITION_LOST; // finished 3+

			}

			return true;

		}

		return false;
	}

	private boolean checkWinCondition() {
		boolean lTop3Finish = mNumberCarsFinished < 3;
		boolean AllOpponentsDead = mGameWorld.carManager().numberOfActiveOpponents() <= 0;

		if (AllOpponentsDead) {
			mEndConditionFlag = lTop3Finish ? END_CONDITION_WON_FIGHTING : END_CONDITION_LOST;
			return true;

		}

		// Top 3 over the finish line
		if (lTop3Finish) {
			mEndConditionFlag = END_CONDITION_WON_RACING; // finished top 3
			return true;
		}

		return false;

	}

	private boolean checkLoseCondition() {
		final var lPlayerCar = mGameWorld.carManager().playerCar();
		if (lPlayerCar.isDestroyed()) {
			mEndConditionFlag = END_CONDITION_DESTROYED;
		}

		// Car destroyed
		return false;
	}

}
