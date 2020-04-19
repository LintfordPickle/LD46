package net.lintford.ld46.controllers;

import org.lwjgl.glfw.GLFW;

import net.lintford.ld46.data.cars.Car;
import net.lintford.ld46.data.cars.CarManager;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.ResourceController;
import net.lintford.library.core.LintfordCore;

public class CarController extends BaseController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "CarController";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private int mCarUidCounter;
	private CarManager mCarManager;
	private ResourceController mResourceController;
	private TrackController mTrackController;
	private Box2dWorldController mBox2dWorldController;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	private int getCarPoolUid() {
		return mCarUidCounter++;
	}

	@Override
	public boolean isinitialized() {
		return mCarManager != null;
	}

	public CarManager carManager() {
		return mCarManager;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public CarController(ControllerManager pControllerManager, CarManager pCarManager, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pEntityGroupID);

		mCarManager = pCarManager;

	}

	@Override
	public void initialize(LintfordCore pCore) {
		mResourceController = (ResourceController) pCore.controllerManager().getControllerByNameRequired(ResourceController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);
		mBox2dWorldController = (Box2dWorldController) pCore.controllerManager().getControllerByNameRequired(Box2dWorldController.CONTROLLER_NAME, entityGroupID());
		mTrackController = (TrackController) pCore.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());

		mCarUidCounter = 0;

		setupPlayerCar();
		setupOpponents(2);

	}

	private void setupPlayerCar() {
		final var lNewPlayerCar = new Car(getCarPoolUid());
		lNewPlayerCar.setCarDriveProperties(150.f, -30.f, 75.f);
		lNewPlayerCar.setCarSteeringProperties(5.5f, 32.0f, 320.0f);

		final var lResourceManager = mResourceController.resourceManager();
		final var lBox2dWorld = mBox2dWorldController.world();

		final var lPObjectInstance = lResourceManager.pobjectManager().getNewInstanceFromPObject(lBox2dWorld, "POBJECT_VEHICLE_01");
		lPObjectInstance.setFixtureCategory(Box2dGameController.CATEGORY_CAR);
		lPObjectInstance.setFixtureBitMask(Box2dGameController.CATEGORY_TRACK);
		lNewPlayerCar.setPhysicsObject(lPObjectInstance);
		lNewPlayerCar.loadPhysics(lBox2dWorld);

		mCarManager.playerCar(lNewPlayerCar);

	}

	private void setupOpponents(final int pNumOpponents) {

	}

	@Override
	public void unload() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean handleInput(LintfordCore pCore) {
		final var lPlayerCar = mCarManager.playerCar();

		// Handle input for player car
		lPlayerCar.input().isTurningLeft = pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_A);
		lPlayerCar.input().isTurningRight = pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_D);
		lPlayerCar.input().isGas = pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_W);
		lPlayerCar.input().isBrake = pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_S);
		lPlayerCar.input().isHandBrake = pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_SPACE);

		return super.handleInput(pCore);

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		final var lPlayerCar = mCarManager.playerCar();

		lPlayerCar.setCarDriveProperties(150.f, -30.f, 75.f);
		lPlayerCar.setCarSteeringProperties(3.25f, 40.0f, 320.0f);

		lPlayerCar.updatePhyics(pCore);

	}

}
