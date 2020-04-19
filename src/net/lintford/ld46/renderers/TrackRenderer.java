package net.lintford.ld46.renderers;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.lintford.ld46.controllers.TrackController;
import net.lintford.ld46.data.tracks.Track;
import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.graphics.shaders.ShaderMVP_PT;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.maths.Matrix4f;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.core.maths.spline.Spline;
import net.lintford.library.core.maths.spline.SplinePoint;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class TrackRenderer extends BaseRenderer {

	// ---------------------------------------------
	// Constants
	// ---------------------------------------------

	// The number of bytes an element has (all elements are floats here)
	protected static final int elementBytes = 4;

	// Elements per parameter
	protected static final int positionElementCount = 4;
	protected static final int colorElementCount = 4;
	protected static final int textureElementCount = 2;

	// Bytes per parameter
	protected static final int positionBytesCount = positionElementCount * elementBytes;
	protected static final int colorBytesCount = colorElementCount * elementBytes;
	protected static final int textureBytesCount = textureElementCount * elementBytes;

	// Byte offsets per parameter
	protected static final int positionByteOffset = 0;
	protected static final int colorByteOffset = positionByteOffset + positionBytesCount;
	protected static final int textureByteOffset = colorByteOffset + colorBytesCount;

	// The amount of elements that a vertex has
	protected static final int elementCount = positionElementCount + colorElementCount + textureElementCount;

	// The size of a vertex in bytes (sizeOf())
	protected static final int stride = positionBytesCount + colorBytesCount + textureBytesCount;

	public static final String RENDERER_NAME = "Track Renderer";

	protected static final String VERT_FILENAME = "/res/shaders/shader_basic_pct.vert";
	protected static final String FRAG_FILENAME = "/res/shaders/shader_basic_pct.frag";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private Track mTrack;
	private FloatBuffer mTrackBuffer;
	private Spline mTrackOuterSpline; // track outer wall // TODO: this is the same as the walls used for box2d (but with different resolution)??
	private Spline mTrackInnerSpline; // track inner wall

	protected int mVaoId = -1;
	protected int mVboId = -1;
	protected int mVertexCount = 0;

	protected ShaderMVP_PT mShader;
	protected Matrix4f mModelMatrix;

	protected boolean mIsTrackGenerated;
	protected Texture mTrackTexture;

	protected TrackController mTrackController;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

	}

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	@Override
	public boolean isInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initialize(LintfordCore pCore) {
		mTrackController = (TrackController) pCore.controllerManager().getControllerByNameRequired(TrackController.CONTROLLER_NAME, entityGroupID());

	}

	@Override
	public void draw(LintfordCore pCore) {

		if (!mTrackController.isinitialized()) {
			return;
		}

		final var lTrack = mTrackController.currentTrack();
		if (lTrack == null)
			return;

		// Render control nodes
		{
			GL11.glPointSize(4f);

			final var lTextFont = mRendererManager.textFont();

			lTextFont.begin(pCore.gameCamera());
			Debug.debugManager().drawers().beginPointRenderer(pCore.gameCamera());

			final int lNumPoints = lTrack.trackSpline().points().size();
			for (int i = 0; i < lNumPoints; i++) {
				final var lPoint = lTrack.trackSpline().points().get(i);
				Debug.debugManager().drawers().drawPoint(lPoint.x, lPoint.y, 1f, 1f, 0f, 1f);
				lTextFont.draw(String.format("%d (%.2f,%.2f)", i, lPoint.x * Box2dWorldController.PIXELS_TO_UNITS / 2.f, lPoint.y * Box2dWorldController.PIXELS_TO_UNITS / 2.f), lPoint.x, lPoint.y, 1f);

			}

			Debug.debugManager().drawers().endPointRenderer();
			lTextFont.end();
		}

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void prepareTrackMesh(Track pTrack) {
		if (pTrack == null)
			return;

		buildTrackMesh(pTrack);

		GL30.glBindVertexArray(mVaoId);

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, mVboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, mTrackBuffer, GL15.GL_STATIC_DRAW);

		GL20.glVertexAttribPointer(0, positionElementCount, GL11.GL_FLOAT, false, stride, positionByteOffset);
		GL20.glVertexAttribPointer(1, colorElementCount, GL11.GL_FLOAT, false, stride, colorByteOffset);
		GL20.glVertexAttribPointer(2, textureElementCount, GL11.GL_FLOAT, false, stride, textureByteOffset);

		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		GL30.glBindVertexArray(0);

		mIsTrackGenerated = true;

	}

	public void buildTrackMesh(Track pTrack) {
		final var lTrackSpline = pTrack.trackSpline();

		final int lNumSplinePoints = lTrackSpline.points().size();
		mTrackBuffer = BufferUtils.createFloatBuffer(lNumSplinePoints * 4 * stride);

		final SplinePoint[] lOuterSplinePoints = new SplinePoint[lNumSplinePoints];
		final SplinePoint[] lInnerSplinePoints = new SplinePoint[lNumSplinePoints];

		Vector2f lTempVector = new Vector2f();
		SplinePoint tempDriveDirection = new SplinePoint();
		SplinePoint tempSideDirection = new SplinePoint();

		for (int i = 0; i < lNumSplinePoints; i++) {
			int nextIndex = i + 1;
			if (nextIndex > lNumSplinePoints - 1) {
				nextIndex = 0;
			}

			tempDriveDirection.x = lTrackSpline.points().get(nextIndex).x - lTrackSpline.points().get(i).x;
			tempDriveDirection.y = lTrackSpline.points().get(nextIndex).y - lTrackSpline.points().get(i).y;

			tempSideDirection.x = tempDriveDirection.y;
			tempSideDirection.y = -tempDriveDirection.x;

			lTempVector.set(tempSideDirection.x, tempSideDirection.y);

			lTempVector.nor();

			SplinePoint lOuterPoint = new SplinePoint();
			SplinePoint lInnerPoint = new SplinePoint();

			float lSegmentWidth = 32f;
			lOuterPoint.x = lTrackSpline.points().get(i).x + tempSideDirection.x * lSegmentWidth / 2;
			lOuterPoint.y = lTrackSpline.points().get(i).y + tempSideDirection.y * lSegmentWidth / 2;

			lInnerPoint.x = lTrackSpline.points().get(i).x - tempSideDirection.x * lSegmentWidth / 2;
			lInnerPoint.y = lTrackSpline.points().get(i).y - tempSideDirection.y * lSegmentWidth / 2;

			final float lSegmentLength = 0;
			// TODO: If I want to texture the segments, need to generate UVs
			addVertToBuffer(lInnerPoint.x, lInnerPoint.y, 0, 0, 1);
			addVertToBuffer(lOuterPoint.x, lOuterPoint.y, 0, 1, 1);

		}

		mTrackOuterSpline = new Spline(lOuterSplinePoints);
		mTrackInnerSpline = new Spline(lInnerSplinePoints);

	}

	private void addVertToBuffer(float x, float y, float z, float u, float v) {

		mTrackBuffer.put(x);
		mTrackBuffer.put(y);
		mTrackBuffer.put(z);
		mTrackBuffer.put(1f);

		mTrackBuffer.put(1f);
		mTrackBuffer.put(1f);
		mTrackBuffer.put(1f);
		mTrackBuffer.put(1f);

		mTrackBuffer.put(u);
		mTrackBuffer.put(v);

		mVertexCount++;

	}

}
