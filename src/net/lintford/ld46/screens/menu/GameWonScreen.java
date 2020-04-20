package net.lintford.ld46.screens.menu;

import org.lwjgl.glfw.GLFW;

import net.lintford.ld46.screens.GameScreen;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.ScreenManagerConstants.FILLTYPE;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class GameWonScreen extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final int BUTTON_RESTART = 0;
	private static final int BUTTON_EXIT = 1;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Texture mWonScreenTexture;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameWonScreen(ScreenManager pScreenManager) {
		super(pScreenManager, "");

		ListLayout lMenuList = new ListLayout(this);
		lMenuList.layoutFillType(FILLTYPE.FILL_CONTAINER);

		MenuEntry lRestartEntry = new MenuEntry(pScreenManager, lMenuList, "Restart");
		MenuEntry lExitGameEntry = new MenuEntry(pScreenManager, lMenuList, "Exit Game");

		lRestartEntry.registerClickListener(this, BUTTON_RESTART);
		lExitGameEntry.registerClickListener(this, BUTTON_EXIT);

		lMenuList.menuEntries().add(lRestartEntry);
		lMenuList.menuEntries().add(lExitGameEntry);

		layouts().add(lMenuList);

		mESCBackEnabled = false;
		mIsPopup = true;
		mShowInBackground = false;

		// Don't allow game to continue while waiting for this screen to finish
		mBlockInputInBackground = true;

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mWonScreenTexture = pResourceManager.textureManager().loadTexture("TEXTURE_SCREENWON", "res/textures/textureWonScreen.png", entityGroupID());

	}

	@Override
	public void handleInput(LintfordCore pCore, boolean pAcceptMouse, boolean pAcceptKeyboard) {
		super.handleInput(pCore, pAcceptMouse, pAcceptKeyboard);

		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_SPACE) || pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ESCAPE) || pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ENTER)
				|| pCore.input().mouse().isMouseLeftButtonDown()) {

			mScreenManager.uiSounds().play("SOUND_MENU_CLICK");

			exitScreen();

		}

	}

	@Override
	public void update(LintfordCore pCore, boolean pOtherScreenHasFocus, boolean pCoveredByOtherScreen) {
		super.update(pCore, pOtherScreenHasFocus, pCoveredByOtherScreen);

		pCore.time().setGameTimePaused(mScreenState == ScreenState.Active);

	}

	@Override
	public void draw(LintfordCore pCore) {
		super.draw(pCore);

		final var lTextureBatch = mRendererManager.uiTextureBatch();
		lTextureBatch.begin(pCore.HUD());
		lTextureBatch.draw(mWonScreenTexture, 0, 0, 600, 480, -300, -240, 600, 480, -0.001f, 1f, 1f, 1f, 1f);
		lTextureBatch.end();

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_RESTART:
			LoadingScreen.load(mScreenManager, true, new GameScreen(mScreenManager));
			return;

		case BUTTON_EXIT:
			mScreenManager.exitGame();
			return;
		}

	}

}
