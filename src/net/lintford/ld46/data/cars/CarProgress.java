package net.lintford.ld46.data.cars;

public class CarProgress {
	public int carIndex;
	public int currentLapNumber;
	public int lastVisitedNodeId;
	public int nextControlNodeId;
	public int position;
	public boolean hasCarFinished;
	public boolean isGoingWrongWay;
	public float distanceIntoRace;

	public CarProgress(int pUid, int pNodeId) {
		carIndex = pUid;
		nextControlNodeId = pNodeId;

	}

}
