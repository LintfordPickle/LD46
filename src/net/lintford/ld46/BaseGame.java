package net.lintford.ld46;

import static org.lwjgl.opengl.GL11.glClearColor;

import org.lwjgl.opengl.GL11;

import net.lintford.ld46.screens.GameScreen;
import net.lintford.ld46.screens.IntroScreen;
import net.lintford.ld46.screens.menu.TutorialMenuScreen;
import net.lintford.library.GameInfo;
import net.lintford.library.controllers.music.MusicController;
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
	public static final String WINDOW_TITLE = "Ludum Dare #46 - John Hampson";

	private static final boolean SKIP_INTRO = false;
	private static final boolean SKIP_MENU = false;

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
	// Core-Methods
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
					if (!SKIP_MENU) {
						addMenuScreens();

					} else {
						LoadingScreen.load(mScreenManager, true, new GameScreen(mScreenManager));

					}

				}
			});

			mScreenManager.addScreen(lIntroScreen);
		} else if (!SKIP_MENU) {
			addMenuScreens();

		} else {
			LoadingScreen.load(mScreenManager, true, new GameScreen(mScreenManager));

		}

	}

	private void addMenuScreens() {
		LoadingScreen.load(mScreenManager, true, new GameScreen(mScreenManager), new TutorialMenuScreen(mScreenManager));

	}

	@Override
	protected void oninitializeGL() {
		super.oninitializeGL();

		mScreenManager.loadGLContent(mResourceManager);

	}

	@Override
	protected void onLoadGLContent() {
		super.onLoadGLContent();

		mScreenManager.loadGLContent(mResourceManager);

		// Load game resources here
		mResourceManager.pobjectManager().definitionRepository().loadDefinitionsFromMetaFile("res/pobjects/meta.json");
		mResourceManager.audioManager().loadAudioFilesFromMetafile("res/sounds/meta.json");
		mResourceManager.musicManager().loadMusicFromMetaFile("res/music/meta.json");

		final var lControlerManager = mScreenManager.core().controllerManager();
		final var lMusic = new MusicController(lControlerManager, mResourceManager.musicManager(), LintfordCore.CORE_ENTITY_GROUP_ID);

		lMusic.nextSong();
		
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

		glClearColor(0.0f / 255.0f, 9.0f / 255.0f, 0.0f / 255.0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		mScreenManager.draw(this);

	}

}
