package net.lintford.ld46.data.tracks;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.maths.spline.Spline;
import net.lintford.library.core.maths.spline.SplinePoint;
import net.lintford.library.core.storage.FileUtils;

public class Track {

	public class TrackDefinition {
		public float[] x;
		public float[] y;
	}

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Spline mHiResTrackSpline;
	private Spline mTrackSpline;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

	public Spline hiResSpline() {
		return mHiResTrackSpline;
	}

	public void hiResSpline(Spline pSetSpline) {
		mHiResTrackSpline = pSetSpline;
	}

	public Spline trackSpline() {
		return mTrackSpline;
	}

	public float getTrackDistance() {
		return mTrackSpline.totalSplineLength();
	}

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public Track() {

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void loadTrackFromFile(String pTrackDefinitionFilename) {

		final var lGson = new GsonBuilder().create();

		String lTrackRawFileContents = null;
		TrackDefinition lTrackDefinition = null;

		try {
			lTrackRawFileContents = FileUtils.loadString(pTrackDefinitionFilename);
			lTrackDefinition = lGson.fromJson(lTrackRawFileContents, TrackDefinition.class);

		} catch (JsonSyntaxException ex) {
			Debug.debugManager().logger().printException(getClass().getSimpleName(), ex);

		}

		if (lTrackDefinition == null) {
			Debug.debugManager().logger().e(getClass().getSimpleName(), "There was an error reading the track information (" + pTrackDefinitionFilename + ")");
			return;

		}

		final int lNumPoints = lTrackDefinition.x.length;

		final SplinePoint[] lPoints = new SplinePoint[lNumPoints];

		for (int i = 0; i < lNumPoints; i++) {
			final float lX = (float) lTrackDefinition.x[i] * Box2dWorldController.UNITS_TO_PIXELS;
			final float lY = (float) lTrackDefinition.y[i] * Box2dWorldController.UNITS_TO_PIXELS;

			lPoints[i] = new SplinePoint(lX, lY);

		}

		mTrackSpline = new Spline(lPoints);
		mTrackSpline.isLooped(true);

	}

}
