package net.lintford.ld46.data.cars;

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

	public void copyFrom(CarInput pOtherInput) {
		isTurningLeft = pOtherInput.isTurningLeft;
		isTurningRight = pOtherInput.isTurningRight;
		isGas = pOtherInput.isGas;
		isBrake = pOtherInput.isBrake;
		isHandBrake = pOtherInput.isHandBrake;

	}

}
