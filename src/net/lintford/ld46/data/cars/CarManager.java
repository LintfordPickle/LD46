package net.lintford.ld46.data.cars;

import java.util.ArrayList;
import java.util.List;

public class CarManager {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Car mPlayerCar;
	private Car mTelekinesisCar;

	private List<Car> mCarsList;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public Car playerCar() {
		return mPlayerCar;
	}

	public void playerCar(Car pNewCar) {
		mPlayerCar = pNewCar;
	}

	public Car telekinesisCar() {
		return mTelekinesisCar;
	}

	public void telekinesisCar(Car pTelekinesisCar) {
		mTelekinesisCar = pTelekinesisCar;
	}

	public int numberOfCars() {
		return mCarsList.size();
	}

	public int numberOfActiveOpponents() {
		int lReturnNumber = 0;

		final int lNumberOpponents = mCarsList.size();
		for (int i = 0; i < lNumberOpponents; i++) {
			if (!mCarsList.get(i).isDestroyed() && !mCarsList.get(i).isPlayerCar)
				lReturnNumber++;

		}

		return lReturnNumber;
	}

	public List<Car> cars() {
		return mCarsList;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public CarManager() {
		mCarsList = new ArrayList<Car>();

	}

}
