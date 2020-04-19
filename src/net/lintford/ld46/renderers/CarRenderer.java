package net.lintford.ld46.renderers;

import org.lwjgl.opengl.GL11;

import net.lintford.ld46.controllers.CarController;
import net.lintford.ld46.data.cars.Car;
import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.camera.ICamera;
import net.lintford.library.core.debug.Debug;
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

	protected Texture mCarTexture;

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

		mCarTexture = pResourceManager.textureManager().loadTexture("TEXTURE_VEHICLE_01", "res/textures/textureVehicle01.png", entityGroupID());

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

	}

	public void draw(LintfordCore pCore, ICamera pCamera) {
		final var lPlayerCar = mCarController.carManager().playerCar();
		drawCar(pCore, pCamera, lPlayerCar);

		final var lListOfOpponents = mCarController.carManager().opponents();
		final int lNumOfOpponents = lListOfOpponents.size();

		for (int i = 0; i < lNumOfOpponents; i++) {
			final var lOpponentCar = lListOfOpponents.get(i);
			drawCar(pCore, pCamera, lOpponentCar);

		}
	}

	public void draw(LintfordCore pCore, ICamera pCamera, int pCarIndex) {
		final var lListOfOpponents = mCarController.carManager().opponents();
		final var lSelectOpponentCar = lListOfOpponents.get(pCarIndex);

		drawCar(pCore, pCamera, lSelectOpponentCar);

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void drawCar(LintfordCore pCore, ICamera pCamera, Car pCar) {

		GL11.glPointSize(3f);
		Debug.debugManager().drawers().drawPointImmediate(pCamera, pCar.pointOnTrackX, pCar.pointOnTrackY);

		{ // Wheels
			final float lCarPosX = pCar.x;
			final float lCarPosY = pCar.y;
			final float lEndX = lCarPosX + (float) Math.cos(pCar.wheelAngle) * 100.f;
			final float lEndY = lCarPosY + (float) Math.sin(pCar.wheelAngle) * 100.f;

			Debug.debugManager().drawers().drawLineImmediate(pCamera, lCarPosX, lCarPosY, lEndX, lEndY, -0.01f, 1f, 0f, 0f);

		}

		{ // Track
			final float lCarPosX = pCar.x;
			final float lCarPosY = pCar.y;

			final float lEndX = lCarPosX + (float) Math.cos(pCar.trackAngle) * 100.f;
			final float lEndY = lCarPosY + (float) Math.sin(pCar.trackAngle) * 100.f;

			Debug.debugManager().drawers().drawLineImmediate(pCamera, lCarPosX, lCarPosY, lEndX, lEndY, -0.01f, 0f, 1f, 0f);
		}

		{ // Ai HEading
			final float lCarPosX = pCar.x;
			final float lCarPosY = pCar.y;

			final float l1Rad = (float) pCar.wheelAngle;
			final float lEndX = lCarPosX + (float) Math.cos(pCar.aiHeadingAngle+l1Rad) * 100.f;
			final float lEndY = lCarPosY + (float) Math.sin(pCar.aiHeadingAngle+l1Rad) * 100.f;

			Debug.debugManager().drawers().drawLineImmediate(pCamera, lCarPosX, lCarPosY, lEndX, lEndY, -0.01f, 0f, 0f, 1f);
		}

		// Yuck!
		final float lScale = 3.f;

		mTextureBatch.begin(pCamera);
		mTextureBatch.update(pCore);
		mTextureBatch.pixelSize(lScale);

		{ // Rear Left Wheel
			final var lRLWheelBody = pCar.mJBox2dEntityInstance.getBodyByName("WheelRearLeft");

			if (lRLWheelBody != null) {
				final float lWorldPositionX = lRLWheelBody.mBody.getWorldCenter().x * Box2dWorldController.UNITS_TO_PIXELS;
				final float lWorldPositionY = lRLWheelBody.mBody.getWorldCenter().y * Box2dWorldController.UNITS_TO_PIXELS;
				final float lRotation = lRLWheelBody.mBody.getAngle();
				mTextureBatch.draw(mCarTexture, 1, 36, 18, 34, lWorldPositionX, lWorldPositionY, 18, 34, -0.03f, lRotation, 0f, 0f, lScale, 1f, 1f, 1f, 1f);

			}

		}

		{ // Rear Right Wheel
			final var lRRWheelBody = pCar.mJBox2dEntityInstance.getBodyByName("WheelRearRight");

			if (lRRWheelBody != null) {
				final float lWorldPositionX = lRRWheelBody.mBody.getWorldCenter().x * Box2dWorldController.UNITS_TO_PIXELS;
				final float lWorldPositionY = lRRWheelBody.mBody.getWorldCenter().y * Box2dWorldController.UNITS_TO_PIXELS;
				final float lRotation = lRRWheelBody.mBody.getAngle();
				mTextureBatch.draw(mCarTexture, 1, 36, 18, 34, lWorldPositionX, lWorldPositionY, 18, 34, -0.03f, lRotation, 0f, 0f, lScale, 1f, 1f, 1f, 1f);

			}

		}

		{// MainBody
			mTextureBatch.draw(mCarTexture, 0, 71, 50, 66, pCar.x, pCar.y, 50, 66, -0.01f, pCar.r, 0f, 0f, lScale, 1f, 1f, 1f, 1f);

		}

		{ // Front Left Wheel
			final var lFLWheelBody = pCar.mJBox2dEntityInstance.getBodyByName("WheelFrontLeft");

			if (lFLWheelBody != null) {
				final float lWorldPositionX = lFLWheelBody.mBody.getWorldCenter().x * Box2dWorldController.UNITS_TO_PIXELS;
				final float lWorldPositionY = lFLWheelBody.mBody.getWorldCenter().y * Box2dWorldController.UNITS_TO_PIXELS;
				final float lRotation = lFLWheelBody.mBody.getAngle();
				mTextureBatch.draw(mCarTexture, 1, 138, 18, 34, lWorldPositionX, lWorldPositionY, 18, 34, -0.01f, lRotation, 0f, 0f, lScale, 1f, 1f, 1f, 1f);

			}
		}

		{ // Front Right Wheel
			final var lFRWheelBody = pCar.mJBox2dEntityInstance.getBodyByName("WheelFrontRight");

			if (lFRWheelBody != null) {
				// "x": 1, "y": 138, "w": 18, "h": 34
				final float lWorldPositionX = lFRWheelBody.mBody.getWorldCenter().x * Box2dWorldController.UNITS_TO_PIXELS;
				final float lWorldPositionY = lFRWheelBody.mBody.getWorldCenter().y * Box2dWorldController.UNITS_TO_PIXELS;
				final float lRotation = lFRWheelBody.mBody.getAngle();
				mTextureBatch.draw(mCarTexture, 1, 138, 18, 34, lWorldPositionX, lWorldPositionY, 18, 34, -0.01f, lRotation, 0f, 0f, lScale, 1f, 1f, 1f, 1f);

			}

		}

		mTextureBatch.end();

	}

}
