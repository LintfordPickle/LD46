package net.lintford.ld46.screens.menu;

import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;

public class PauseMenuScreen extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final int BUTTON_CONTINUE = 0;
	private static final int BUTTON_OPTIONS = 1;
	private static final int BUTTON_EXIT = 2;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public PauseMenuScreen(ScreenManager pScreenManager, String pMenuTitle) {
		super(pScreenManager, pMenuTitle);

		mIsPopup = true;
	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_CONTINUE:
			return;

		case BUTTON_OPTIONS:

			return;

		case BUTTON_EXIT:
			return;
		}

	}

}
