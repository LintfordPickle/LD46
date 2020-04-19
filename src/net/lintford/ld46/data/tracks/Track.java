package net.lintford.ld46.data.tracks;

import org.lwjgl.glfw.GLFW;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.core.LintfordCore;
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

	private Spline mTrackSpline;

	private boolean mInUpdateMode;
	private int mSelectedControlPointIndex;

	// ---------------------------------------------
	// Properties
	// ---------------------------------------------

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
	// Core-Methods
	// ---------------------------------------------

	public void handleInput(LintfordCore pCore) {

		final float lDeltaTime = (float) pCore.time().elapseGameTimeMilli();

		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_X)) {
			mSelectedControlPointIndex++;
			if (mSelectedControlPointIndex >= mTrackSpline.points().size()) {
				mSelectedControlPointIndex = 0;
			}
		}
		if (pCore.input().keyboard().isKeyDownTimed(GLFW.GLFW_KEY_Z)) {
			mSelectedControlPointIndex--;
			if (mSelectedControlPointIndex < 0) {
				mSelectedControlPointIndex = mTrackSpline.points().size() - 1;
			}
		}

		{
			boolean lUpdateKeyPressed = false;
			final float lMovementSpeed = .1f;
			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_LEFT)) {
				mTrackSpline.points().get(mSelectedControlPointIndex).x -= lMovementSpeed * lDeltaTime;
				mInUpdateMode = true;
				lUpdateKeyPressed = true;
			}

			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
				mTrackSpline.points().get(mSelectedControlPointIndex).x += lMovementSpeed * lDeltaTime;
				mInUpdateMode = true;
				lUpdateKeyPressed = true;
			}

			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_UP)) {
				mTrackSpline.points().get(mSelectedControlPointIndex).y -= lMovementSpeed * lDeltaTime;
				mInUpdateMode = true;
				lUpdateKeyPressed = true;
			}

			if (pCore.input().keyboard().isKeyDown(GLFW.GLFW_KEY_DOWN)) {
				mTrackSpline.points().get(mSelectedControlPointIndex).y += lMovementSpeed * lDeltaTime;
				mInUpdateMode = true;
				lUpdateKeyPressed = true;
			}

			if (mInUpdateMode && !lUpdateKeyPressed) {
				mTrackSpline.calculateSegmentLength();
				mInUpdateMode = false;
			}
		}

	}

	public void draw(LintfordCore pCore) {

		{ // Draw the spline
			final var offset = mTrackSpline.isLooped() ? 0f : 3f;
			for (float t = 0; t < mTrackSpline.points().size() - offset; t += 0.025f) {
				final var lPoint = mTrackSpline.getPointOnSpline(t);

				Debug.debugManager().drawers().drawPointImmediate(pCore.gameCamera(), lPoint.x, lPoint.y, -0.01f, 1f, 1f, 0f, 1f);

			}
		}

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
