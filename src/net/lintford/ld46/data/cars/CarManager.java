package net.lintford.ld46.data.cars;

import java.util.ArrayList;
import java.util.List;

import net.lintford.ld46.data.Car;

public class CarManager {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Car mPlayerCar;
	private List<Car> mOpponentCars;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public Car playerCar() {
		return mPlayerCar;
	}

	public void playerCar(Car pNewCar) {
		mPlayerCar = pNewCar;
	}

	public List<Car> opponents() {
		return mOpponentCars;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public CarManager() {
		mOpponentCars = new ArrayList<Car>();

	}

}
