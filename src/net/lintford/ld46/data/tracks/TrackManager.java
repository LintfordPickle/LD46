package net.lintford.ld46.data.tracks;

public class TrackManager {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Track mCurrentTrack;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public Track currentTrack() {
		return mCurrentTrack;
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackManager() {

	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public void loadTrack(String pTrackFilename) {
		mCurrentTrack = new Track();
		mCurrentTrack.loadTrackFromFile(pTrackFilename);

	}

}
