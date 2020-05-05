package net.lintford.ld46.renderers;

import net.lintford.ld46.controllers.CarController;
import net.lintford.ld46.data.cars.Car;
import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.camera.ICamera;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.texturebatch.SubPixelTextureBatch;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class CarRenderer extends BaseRenderer {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String RENDERER_NAME = "Car Renderer";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	protected CarController mCarController;
	private SubPixelTextureBatch mTextureBatch;

	protected Texture mCarTexturePlayer;
	protected Texture mCarTextureEnemy;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		return mCarController != null;

	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public CarRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

		mTextureBatch = new SubPixelTextureBatch();

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		mCarController = (CarController) pCore.controllerManager().getControllerByNameRequired(CarController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mCarTexturePlayer = pResourceManager.textureManager().loadTexture("TEXTURE_VEHICLE_01", "res/textures/textureVehicle01.png", entityGroupID());
		mCarTextureEnemy = pResourceManager.textureManager().loadTexture("TEXTURE_VEHICLE_02", "res/textures/textureVehicle02.png", entityGroupID());

		mTextureBatch.loadGLContent(pResourceManager);

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

		mTextureBatch.unloadGLContent();
	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

	}

	@Override
	public void draw(LintfordCore pCore) {
		final var lPlayerCar = mCarController.carManager().playerCar();
		drawCar(pCore, pCore.gameCamera(), lPlayerCar);

		final var lListOfOpponents = mCarController.carManager().cars();
		final int lNumOfOpponents = lListOfOpponents.size();

		for (int i = 0; i < lNumOfOpponents; i++) {
			final var lOpponentCar = lListOfOpponents.get(i);
			drawCar(pCore, pCore.gameCamera(), lOpponentCar);

		}
	}

	public void draw(LintfordCore pCore, ICamera pCamera) {

	}

	public void draw(LintfordCore pCore, ICamera pCamera, int pCarIndex) {
		final var lListOfOpponents = mCarController.carManager().cars();
		final var lSelectOpponentCar = lListOfOpponents.get(pCarIndex);

		drawCar(pCore, pCamera, lSelectOpponentCar);

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void drawCar(LintfordCore pCore, ICamera pCamera, Car pCar) {
		// Yuck!
		final float lScale = 4.f;

		mTextureBatch.begin(pCamera);
		mTextureBatch.update(pCore);
		mTextureBatch.pixelSize(lScale);

		var lTexture = mCarTextureEnemy;
		if (pCar.isPlayerCar) {
			lTexture = mCarTexturePlayer;
		}

		{// MainBody

			final float lSourceX = 1.f;
			final float lSourceY = 2.f;
			final float lSourceW = 34.f;
			final float lSourceH = 73.f;

			final float lDestW = lSourceW;
			final float lDestH = lSourceH;

			mTextureBatch.draw(lTexture, lSourceX, lSourceY, lSourceW, lSourceH, pCar.x, pCar.y, lDestW, lDestH, -0.01f, pCar.r, 0f, 0f, lScale, 1f, 1f, 1f, 1f);

		}

		{ // Rear Left Wheel
			final var lRLWheelBody = pCar.box2dEntityInstance().getBodyByName("WheelRearLeft");

			if (lRLWheelBody != null) {
				final float lWorldPositionX = lRLWheelBody.mBody.getWorldCenter().x * Box2dWorldController.UNITS_TO_PIXELS;
				final float lWorldPositionY = lRLWheelBody.mBody.getWorldCenter().y * Box2dWorldController.UNITS_TO_PIXELS;
				final float lRotation = lRLWheelBody.mBody.getAngle();

				final float lSourceX = 38.f;
				final float lSourceY = 24.f;
				final float lSourceW = 12.f;
				final float lSourceH = 20.f;

				final float lDestW = lSourceW;
				final float lDestH = lSourceH;

				mTextureBatch.draw(lTexture, lSourceX, lSourceY, lSourceW, lSourceH, lWorldPositionX, lWorldPositionY, lDestW, lDestH, -0.03f, lRotation, 0f, 0f, lScale, 1f, 1f, 1f, 1f);

			}

		}

		{ // Rear Right Wheel
			final var lRRWheelBody = pCar.box2dEntityInstance().getBodyByName("WheelRearRight");

			if (lRRWheelBody != null) {
				final float lWorldPositionX = lRRWheelBody.mBody.getWorldCenter().x * Box2dWorldController.UNITS_TO_PIXELS;
				final float lWorldPositionY = lRRWheelBody.mBody.getWorldCenter().y * Box2dWorldController.UNITS_TO_PIXELS;
				final float lRotation = lRRWheelBody.mBody.getAngle();

				final float lSourceX = 38.f;
				final float lSourceY = 24.f;
				final float lSourceW = 12.f;
				final float lSourceH = 20.f;

				final float lDestW = lSourceW;
				final float lDestH = lSourceH;

				mTextureBatch.draw(lTexture, lSourceX, lSourceY, lSourceW, lSourceH, lWorldPositionX, lWorldPositionY, lDestW, lDestH, -0.03f, lRotation, 0f, 0f, lScale, 1f, 1f, 1f, 1f);

			}

		}

		{ // Front Left Wheel
			final var lFLWheelBody = pCar.box2dEntityInstance().getBodyByName("WheelFrontLeft");

			if (lFLWheelBody != null) {
				final float lWorldPositionX = lFLWheelBody.mBody.getWorldCenter().x * Box2dWorldController.UNITS_TO_PIXELS;
				final float lWorldPositionY = lFLWheelBody.mBody.getWorldCenter().y * Box2dWorldController.UNITS_TO_PIXELS;
				final float lRotation = lFLWheelBody.mBody.getAngle();

				final float lSourceX = 38.f;
				final float lSourceY = 2.f;
				final float lSourceW = 10.f;
				final float lSourceH = 20.f;

				final float lDestW = lSourceW;
				final float lDestH = lSourceH;

				mTextureBatch.draw(lTexture, lSourceX, lSourceY, lSourceW, lSourceH, lWorldPositionX, lWorldPositionY, lDestW, lDestH, -0.03f, lRotation, 0f, 0f, lScale, 1f, 1f, 1f, 1f);

			}
		}

		{ // Front Right Wheel
			final var lFRWheelBody = pCar.box2dEntityInstance().getBodyByName("WheelFrontRight");

			if (lFRWheelBody != null) {
				// "x": 1, "y": 138, "w": 18, "h": 34
				final float lWorldPositionX = lFRWheelBody.mBody.getWorldCenter().x * Box2dWorldController.UNITS_TO_PIXELS;
				final float lWorldPositionY = lFRWheelBody.mBody.getWorldCenter().y * Box2dWorldController.UNITS_TO_PIXELS;
				final float lRotation = lFRWheelBody.mBody.getAngle();

				final float lSourceX = 38.f;
				final float lSourceY = 2.f;
				final float lSourceW = 10.f;
				final float lSourceH = 20.f;

				final float lDestW = lSourceW;
				final float lDestH = lSourceH;

				mTextureBatch.draw(lTexture, lSourceX, lSourceY, lSourceW, lSourceH, lWorldPositionX, lWorldPositionY, lDestW, lDestH, -0.03f, lRotation, 0f, 0f, lScale, 1f, 1f, 1f, 1f);

			}

		}

		mTextureBatch.end();

	}

}
