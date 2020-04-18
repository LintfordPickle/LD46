package net.lintford.ld46.data;

public class CarInput {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	public boolean isTurningLeft;
	public boolean isTurningRight;
	public boolean isGas;
	public boolean isBrake;
	public boolean isHandBrake;

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void reset() {
		isTurningLeft = false;
		isTurningRight = false;
		isGas = false;
		isBrake = false;
		isHandBrake = false;

	}
}
