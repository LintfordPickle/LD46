package net.lintford.ld46.screens.menu;

import org.lwjgl.glfw.GLFW;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;

public class TutorialMenuScreen extends MenuScreen {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final int BUTTON_CONTINUE = 0;
	private static final int BUTTON_OPTIONS = 1;
	private static final int BUTTON_EXIT = 2;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Texture mTutorialTexture;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TutorialMenuScreen(ScreenManager pScreenManager) {
		super(pScreenManager, "");

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

		mTutorialTexture = pResourceManager.textureManager().loadTexture("TEXTURE_TUTORIAL", "res/textures/textureTutorial.png", entityGroupID());

	}

	@Override
	public void handleInput(LintfordCore pCore, boolean pAcceptMouse, boolean pAcceptKeyboard) {
		super.handleInput(pCore, pAcceptMouse, pAcceptKeyboard);

		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_SPACE) || 
				pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ESCAPE) || 
				pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_ENTER) ||
				pCore.input().mouse().isMouseLeftButtonDown()) {
			exitScreen();

		}

	}

	@Override
	public void draw(LintfordCore pCore) {
		super.draw(pCore);

		final var lTextureBatch = mRendererManager.uiTextureBatch();
		lTextureBatch.begin(pCore.HUD());
		lTextureBatch.draw(mTutorialTexture, 0, 0, 600, 480, -300, -240, 600, 480, -0.001f, 1f, 1f, 1f, 1f);
		lTextureBatch.end();

	}

	// ---------------------------------------------
	// Methods
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
