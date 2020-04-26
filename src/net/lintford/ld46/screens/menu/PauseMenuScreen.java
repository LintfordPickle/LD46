package net.lintford.ld46.screens.menu;

import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.ScreenManagerConstants.FILLTYPE;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.AudioOptionsScreen;

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

	private MenuEntry mContinueEntry;
	private MenuEntry mOptionsEntry;
	private MenuEntry mExitEntry;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public PauseMenuScreen(ScreenManager pScreenManager) {
		super(pScreenManager, "Paused");

		ListLayout lPauseMenuList = new ListLayout(this);
		lPauseMenuList.setDrawBackground(true, .4f, .4f, .94f, 0.85f);
		lPauseMenuList.layoutFillType(FILLTYPE.TAKE_WHATS_NEEDED);

		mContinueEntry = new MenuEntry(mScreenManager, lPauseMenuList, "Continue");
		mOptionsEntry = new MenuEntry(mScreenManager, lPauseMenuList, "Options");
		mExitEntry = new MenuEntry(mScreenManager, lPauseMenuList, "Exit");

		mContinueEntry.registerClickListener(this, BUTTON_CONTINUE);
		mOptionsEntry.registerClickListener(this, BUTTON_OPTIONS);
		mExitEntry.registerClickListener(this, BUTTON_EXIT);

		lPauseMenuList.menuEntries().add(mContinueEntry);
		lPauseMenuList.menuEntries().add(mOptionsEntry);
		lPauseMenuList.menuEntries().add(mExitEntry);

		layouts().add(lPauseMenuList);

		mIsPopup = true;
		mBlockInputInBackground = false;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_CONTINUE:
			exitScreen();
			return;

		case BUTTON_OPTIONS:
			mScreenManager.addScreen(new AudioOptionsScreen(mScreenManager));
			return;

		case BUTTON_EXIT:
			mScreenManager.exitGame();
			return;
		}

	}

}
