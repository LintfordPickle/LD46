package net.lintford.ld46;

import net.lintford.ld46.screens.IntroScreen;
import net.lintford.ld46.screens.TrackGameScreen;
import net.lintford.library.GameInfo;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.screenmanager.IMenuAction;
import net.lintford.library.screenmanager.Screen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class BaseGame extends LintfordCore {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String APPLICATION_NAME = "Ludum Dare #46";
	public static final String WINDOW_TITLE = "Ludum Dare #46 © John Hampson";

	private static final boolean SKIP_INTRO = true;
	private static final boolean SKIP_MENU = true;

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
	// Variables
	// ---------------------------------------------

	private ScreenManager mScreenManager;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public BaseGame(GameInfo pGameInfo, String[] pArgs) {
		super(pGameInfo, pArgs, false);

		mScreenManager = new ScreenManager(this);

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	@Override
	protected void onInitializeApp() {
		super.onInitializeApp();

		mScreenManager.initialize();

		if (!SKIP_INTRO) {
			final var lIntroScreen = new IntroScreen(mScreenManager, "res/textures/textureIntro.png");
			lIntroScreen.stretchBackgroundToFit(true);
			lIntroScreen.setTimerFinishedCallback(new IMenuAction() {

				@Override
				public void TimerFinished(Screen pScreen) {
					addMenuScreens();

				}
			});

			mScreenManager.addScreen(lIntroScreen);
		}

		if (!SKIP_MENU) {
			addMenuScreens();

		} else {
			// LoadingScreen.load(mScreenManager, true, new GameScreen(mScreenManager));
			
			LoadingScreen.load(mScreenManager, true, new TrackGameScreen(mScreenManager));
			
		}

	}

	private void addMenuScreens() {

	}

	@Override
	protected void oninitializeGL() {
		// TODO Auto-generated method stub
		super.oninitializeGL();
	}

	@Override
	protected void onLoadGLContent() {
		super.onLoadGLContent();

		mScreenManager.loadGLContent(mResourceManager);

		// Load game resources here
		mResourceManager.pobjectManager().definitionRepository().loadDefinitionsFromMetaFile("res/pobjects/meta.json");

	}

	@Override
	protected void onUnloadGLContent() {
		super.onUnloadGLContent();

	}

	@Override
	protected void onHandleInput() {
		super.onHandleInput();

		mScreenManager.handleInput(this);

	}

	@Override
	protected void onUpdate() {
		super.onUpdate();

		mScreenManager.update(this);

	}

	@Override
	protected void onDraw() {
		super.onDraw();

		mScreenManager.draw(this);

	}

}
