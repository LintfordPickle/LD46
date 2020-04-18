package net.lintford.ld46.controllers;

import net.lintford.ld46.data.cars.Car;
import net.lintford.library.controllers.BaseController;
import net.lintford.library.controllers.camera.CameraController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.camera.ICamera;

public class CameraFollowController extends BaseController {

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public static final String CAMERA_CONTROLLER_NAME = "MainCameraFollowController";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private ICamera mCamera;
	private Car mFollowCar;

	// --------------------------------------
	// Properties
	// --------------------------------------

	@Override
	public boolean isinitialized() {
		return false;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public CameraFollowController(ControllerManager pControllerManager, Car pFollowCar, int pEntityGroupID) {
		super(pControllerManager, CAMERA_CONTROLLER_NAME, pEntityGroupID);

		mFollowCar = pFollowCar;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		final CameraController lCameraController = (CameraController) mControllerManager.getControllerByNameRequired(CameraController.CONTROLLER_NAME, LintfordCore.CORE_ENTITY_GROUP_ID);
		mCamera = lCameraController.camera();

	}

	@Override
	public void unload() {

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

}
