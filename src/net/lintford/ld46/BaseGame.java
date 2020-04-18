package net.lintford.ld46;

import net.lintford.library.GameInfo;
import net.lintford.library.core.LintfordCore;

public class BaseGame extends LintfordCore {

	// ---------------------------------------------
	// Entry Point
	// ---------------------------------------------

	public static void main(String[] pArgs) {
		GameInfo lGameInfo = new GameInfo() {
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
