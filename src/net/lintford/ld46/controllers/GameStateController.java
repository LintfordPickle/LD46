package net.lintford.ld46.controllers;

import net.lintford.ld46.data.GameWorld;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;

public class GameStateController extends BaseController {

	public class CarProgress {
		public int carIndex;
		public float distanceIntoLap;
		public float normalizedPosition;
		public int position;

	}

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "GameState Controller";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private GameWorld mGameWorld;
	private CarProgress[] mCarProgress;

	private int mTotalLaps;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isinitialized() {
		return mGameWorld != null;
	}

	public GameWorld gameWorld() {
		return mGameWorld;
	}

	public CarProgress getCarProgressById(int pCarIndex) {
		return mCarProgress[pCarIndex];
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameStateController(ControllerManager pControllerManager, GameWorld pGameWorld, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mGameWorld = pGameWorld;

	}

	public void startNewGame() {
		mTotalLaps = 3;

	}

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

	}

}
