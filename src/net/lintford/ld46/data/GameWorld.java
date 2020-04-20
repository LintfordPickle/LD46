package net.lintford.ld46.data;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

import net.lintford.ld46.data.cars.CarManager;
import net.lintford.ld46.data.tracks.TrackManager;

public class GameWorld {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	private static final Vec2 BOX2D_GRAVITY = new Vec2(0, 0);

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private World mBox2dWorld;
	private CarManager mCarManager;
	private TrackManager mTrackManager;
	private TelekinesisManager mTelekinesisManager;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public World box2dWorld() {
		return mBox2dWorld;
	}

	public CarManager carManager() {
		return mCarManager;
	}

	public TrackManager trackManager() {
		return mTrackManager;
	}

	public TelekinesisManager telekinesisManager() {
		return mTelekinesisManager;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public GameWorld() {
		mBox2dWorld = new World(BOX2D_GRAVITY);
		mCarManager = new CarManager();
		mTrackManager = new TrackManager();
		mTelekinesisManager = new TelekinesisManager();

	}

}
