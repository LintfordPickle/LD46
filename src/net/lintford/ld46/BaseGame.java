package net.lintford.ld46;

import net.lintford.library.GameInfo;
import net.lintford.library.core.LintfordCore;

public class BaseGame extends LintfordCore {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String APPLICATION_NAME = "Ludum Dare #46";
	public static final String WINDOW_TITLE = "Ludum Dare #46 © John Hampson";

	// ---------------------------------------------
	// Entry Point
	// ---------------------------------------------

	public static void main(String[] pArgs) {
		GameInfo lGameInfo = new GameInfo() {
			@Override
			public String applicationName() {
				return APPLICATION_NAME;
			}

			@Override
			public String windowTitle() {
				return WINDOW_TITLE;
			}

		};

		final var lBaseGame = new BaseGame(lGameInfo, pArgs);
		lBaseGame.createWindow();

	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public BaseGame(GameInfo pGameInfo, String[] pArgs) {
		super(pGameInfo, pArgs, false);

	}

}
