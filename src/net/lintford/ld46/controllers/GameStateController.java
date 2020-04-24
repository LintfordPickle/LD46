package net.lintford.ld46.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.lintford.ld46.data.GameWorld;
import net.lintford.ld46.data.cars.Car;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;

public class GameStateController extends BaseController {

	public interface ICountDownListener {
		public void onCountDown(int pCurrentSecondsToBegin);
	}

	public class CarPositionSorter implements Comparator<Car> {

		@Override
		public int compare(Car o1, Car o2) {
			return (o1.carProgress().distanceIntoRace > o2.carProgress().distanceIntoRace) ? -1 : 1;
		}
	}

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final int END_CONDITION_NOT_SET = 0;
	public static final int END_CONDITION_DESTROYED = 1;
	public static final int END_CONDITION_WON_RACING = 2;
	public static final int END_CONDITION_LOST = 3;
	public static final int END_CONDITION_WON_FIGHTING = 4;

	public static final String CONTROLLER_NAME = "GameState Controller";

	public static final int numLaps = 5;
	public static final int countDownStart = 5;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private GameWorld mGameWorld;
	private boolean mIsGameScreenActive;
	private ICountDownListener mCountDownCallback;

	private int mTotalLaps;

	private int mNumberCarsFinished;
	private int mEndConditionFlag;
	private boolean mIsGameFinished;

	private boolean mHasRaceStarted;
	private int mStartCountDown;
	private float mCountDownTimer;

	private List<Car> mPositionsList;
	private CarPositionSorter mCarPositionSorter = new CarPositionSorter();

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public boolean isGameScreenActive() {
		return mIsGameScreenActive;
	}

	public void isGameScreenActive(boolean pNewValue) {
		mIsGameScreenActive = pNewValue;
	}

	public void countDownCallBack(ICountDownListener pCallbackOnObject) {
		mCountDownCallback = pCallbackOnObject;
	}

	public int countDown() {
		return mStartCountDown;
	}

	public boolean hasRaceStarted() {
		return mHasRaceStarted;
	}

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
		return mGameWorld.carManager().numberOfCars();
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

	public int getPlayerPosition() {
		final var lPlayerCar = mGameWorld.carManager().playerCar();
		final int lNumberCars = mPositionsList.size();
		for (int i = 0; i < lNumberCars; i++) {
			if (mPositionsList.get(i).equals(lPlayerCar))
				return i + 1;
		}
		return 1;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameStateController(ControllerManager pControllerManager, GameWorld pGameWorld, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mGameWorld = pGameWorld;
		mStartCountDown = countDownStart;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mPositionsList = new ArrayList<>();

		final var lCarManager = mGameWorld.carManager();
		final int lNumberCars = lCarManager.numberOfCars();
		for (int i = 0; i < lNumberCars; i++) {
			mPositionsList.add(lCarManager.cars().get(i));

		}

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

		if (!mIsGameScreenActive)
			return;

		if (!mHasRaceStarted) {
			// update the countdown
			final float lDelta = (float) pCore.appTime().elapseTimeMilli();
			mCountDownTimer -= lDelta;
			if (mCountDownTimer < 0.f) {
				mCountDownTimer += 1000.f;
				mStartCountDown--;
				if (mCountDownCallback != null) {
					mCountDownCallback.onCountDown(mStartCountDown);
				}

				if (mStartCountDown <= 0) {
					startRace();
					pCore.gameTime().setPaused(false);
					return;
				}

			}

			pCore.gameTime().setPaused(true);

			return;

		}

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
		mTotalLaps = numLaps;

	}

	private void updatePositions() {

		final var lOpponentVehicles = mGameWorld.carManager().cars();
		final int lNumVehicles = lOpponentVehicles.size();
		for (int i = 0; i < lNumVehicles; i++) {
			final var lCar = lOpponentVehicles.get(i);

			if (!lCar.carProgress().hasCarFinished && lCar.carProgress().currentLapNumber > mTotalLaps) {
				lCar.carProgress().hasCarFinished = true;
				mNumberCarsFinished++;

			}

		}

		mPositionsList.sort(mCarPositionSorter);

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
		boolean lFInishedFirst = mNumberCarsFinished <= 1; // the player being the one who just finished
		boolean AllOpponentsDead = mGameWorld.carManager().numberOfActiveOpponents() <= 0;

		if (AllOpponentsDead) {
			mEndConditionFlag = lFInishedFirst ? END_CONDITION_WON_FIGHTING : END_CONDITION_LOST;
			return true;

		}

		// Top 3 over the finish line
		if (lFInishedFirst) {
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

	public void startRace() {
		mHasRaceStarted = true;

	}

}
