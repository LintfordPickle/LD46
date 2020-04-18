package net.lintford.ld46.data;

import org.jbox2d.dynamics.World;

import net.lintford.library.core.entity.JBox2dEntity;

public class Car extends JBox2dEntity {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final long serialVersionUID = 8546363285956254711L;

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public Car(int pPoolUid) {
		super(pPoolUid);

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public void loadPhysics(World pWorld) {
		super.loadPhysics(pWorld);

		// TODO: add hooks for box2d bodies

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

}
