package net.lintford.ld46.controllers;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;

import net.lintford.library.controllers.box2d.Box2dContactController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;

public class GameCollisionController extends Box2dContactController {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	public static final String CONTROLLER_NAME = "GameCollisionController";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private CarController mCarController;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameCollisionController(ControllerManager pControllerManager, World pWorld, int pEntityGroupID) {
		super(pControllerManager, CONTROLLER_NAME, pWorld, pEntityGroupID);

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void initialize(LintfordCore pCore) {
		super.initialize(pCore);

		mCarController = (CarController) pCore.controllerManager().getControllerByNameRequired(CarController.CONTROLLER_NAME, entityGroupID());

	}

	// ---------------------------------------------
	// Solver Methods
	// ---------------------------------------------

	@Override
	public void preSolve(Contact arg0, Manifold arg1) {

	}

	@Override
	public void postSolve(Contact arg0, ContactImpulse arg1) {

	}

	// ---------------------------------------------
	// Collision Methods
	// ---------------------------------------------

	@Override
	public void beginContact(Contact pContact) {
		mCarController.playCrashSound();

	}

	@Override
	public void endContact(Contact pContact) {

	}

}
