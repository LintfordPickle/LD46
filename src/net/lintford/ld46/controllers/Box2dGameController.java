package net.lintford.ld46.controllers;

import org.jbox2d.dynamics.World;

import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.controllers.core.ControllerManager;

public class Box2dGameController extends Box2dWorldController {

	// I am
	public static final int CATEGORY_CAR = 0b00000001;
	public static final int CATEGORY_TRACK = 0b00000010;

	// I collide with mask_bits

	public Box2dGameController(ControllerManager pControllerManager, World pWorld, int pEntityGroupID) {
		super(pControllerManager, pWorld, pEntityGroupID);

	}

}
