package net.lintford.ld46.renderers;

import java.nio.FloatBuffer;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.lintford.ld46.controllers.TrackController;
import net.lintford.ld46.data.tracks.Track;
import net.lintford.library.controllers.box2d.Box2dWorldController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.graphics.shaders.ShaderSubPixel;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.maths.Matrix4f;
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
	protected static final String FRAG_FILENAME = "res/shaders/shaderTrack.frag";

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private FloatBuffer mTrackBuffer;

	protected int mVaoId = -1;
	protected int mVboId = -1;
	protected int mVertexCount = 0;

	protected ShaderSubPixel mShader;
	protected Matrix4f mModelMatrix;

	protected boolean mIsTrackGenerated;
	protected Texture mTrackTexture;
	protected Texture mTrackPropsTexture;
	protected Texture mTrackGrassTexture;

	private float mCheckeredStartX;
	private float mCheckeredStartY;
	private float mCheckeredStartRotation;

	protected TrackController mTrackController;

	// ---------------------------------------------
	// Constructor
	// ---------------------------------------------

	public TrackRenderer(RendererManager pRendererManager, int pEntityGroupID) {
		super(pRendererManager, RENDERER_NAME, pEntityGroupID);

		mShader = new ShaderSubPixel("TrackShader", VERT_FILENAME, FRAG_FILENAME);

		mModelMatrix = new Matrix4f();

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
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		final var lTrack = mTrackController.currentTrack();

		if (mVaoId == -1)
			mVaoId = GL30.glGenVertexArrays();

		if (mVboId == -1)
			mVboId = GL15.glGenBuffers();

		mShader.loadGLContent(pResourceManager);

		mTrackTexture = pResourceManager.textureManager().loadTexture("TEXTURE_TRACK", "res/textures/textureTrack.png", GL11.GL_LINEAR, entityGroupID());
		mTrackPropsTexture = pResourceManager.textureManager().loadTexture("TEXTURE_TRACK_PROPS", "res/textures/textureTrackProps.png", GL11.GL_LINEAR, entityGroupID());
		mTrackGrassTexture = pResourceManager.textureManager().loadTexture("TEXTURE_TRACK_GRASS", "res/textures/textureTrackGrass.png", GL11.GL_LINEAR, entityGroupID());

		loadTrackMesh(lTrack);

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

		mShader.unloadGLContent();
		mTrackTexture = null;

		if (mVaoId > -1)
			GL30.glDeleteVertexArrays(mVaoId);

		if (mVboId > -1)
			GL15.glDeleteBuffers(mVboId);

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		final var lDesktopWidth = pCore.config().display().desktopWidth();
		final var lDesktopHeight = pCore.config().display().desktopHeight();
		mShader.screenResolutionWidth(lDesktopWidth);
		mShader.screenResolutionHeight(lDesktopHeight);

		final var lCamera = pCore.gameCamera();
		mShader.cameraResolutionWidth(lCamera.getWidth());
		mShader.cameraResolutionHeight(lCamera.getHeight());

		mShader.pixelSize(3f);

	}

	@Override
	public void draw(LintfordCore pCore) {
		if (!mTrackController.isinitialized()) {
			return;
		}

		final var lTrack = mTrackController.currentTrack();
		if (lTrack == null)
			return;

		final var lTextureBatch = mRendererManager.uiTextureBatch();
//		lTextureBatch.begin(pCore.HUD());
//
//		final var lBoundingRect = pCore.HUD().boundingRectangle();
//		final float lHudWidth = lBoundingRect.width();
//		final float lHudHeight = lBoundingRect.height();
//		
//		GL11.glBindTexture(GL11.GL_TEXTURE_2D, mTrackGrassTexture.getTextureID());
//
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
//
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_REPEAT);
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_REPEAT);
//
//		float lGrassScale = 2.f;
//		float lOffsetX = pCore.gameCamera().getZoomFactor() + pCore.gameCamera().getMinX() * 0.01f;
//		float lOffsetY = pCore.gameCamera().getZoomFactor() + pCore.gameCamera().getMinY() * 0.01f;
//
//		lTextureBatch.draw(mTrackGrassTexture, 0 + lOffsetX, 0 + lOffsetY, 800, 600, -lHudWidth * .5f, -lHudHeight * .5f, lHudWidth * lGrassScale, lHudHeight * lGrassScale, -0.1f, 1f, 1f, 1f, 1f);
//		lTextureBatch.end();

		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, mTrackTexture.getTextureID());

		GL30.glBindVertexArray(mVaoId);

		mShader.projectionMatrix(pCore.gameCamera().projection());
		mShader.viewMatrix(pCore.gameCamera().view());
		mModelMatrix.setIdentity();
		mModelMatrix.translate(0, 0f, -6f);
		mShader.modelMatrix(mModelMatrix);

		mShader.bind();

		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, mVertexCount);

		mShader.unbind();

		GL30.glBindVertexArray(0);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		lTextureBatch.begin(pCore.gameCamera());
		final var lScale = 4.0f;
		lTextureBatch.draw(mTrackPropsTexture, 0, 0, 256, 62, mCheckeredStartX, mCheckeredStartY, 256 * lScale, 62 * lScale, -0.1f, mCheckeredStartRotation + (float) Math.toRadians(90), 0f, 0f, 1f, 1f, 1f, 1f, 1f);
		lTextureBatch.end();

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	private void loadTrackMesh(Track pTrack) {
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
		final var lInnerVertices = mTrackController.lInnerVertices;
		final var lOuterVertices = mTrackController.lOuterVertices;

		final int lNumSplinePoints = lInnerVertices.length;
		mTrackBuffer = BufferUtils.createFloatBuffer(lNumSplinePoints * 4 * stride);

		float lDistanceTravelled = 0.f;
		float lLengthOfSegment = 0.f;

		float lCurX = 0.f;
		float lCurY = 0.f;
		float lPrevX = 0.f;
		float lPrevY = 0.f;

		mCheckeredStartX = (lInnerVertices[0].x + (lOuterVertices[0].x - lInnerVertices[0].x) * .5f) * Box2dWorldController.UNITS_TO_PIXELS;
		mCheckeredStartY = (lInnerVertices[0].y + (lOuterVertices[0].y - lInnerVertices[0].y) * .5f) * Box2dWorldController.UNITS_TO_PIXELS;

		mCheckeredStartRotation = (float) Math.atan2(-lOuterVertices[1].x - lOuterVertices[0].x, lOuterVertices[1].x - lOuterVertices[0].x);

		for (int i = 0; i < lNumSplinePoints; i++) {

			lCurX = lInnerVertices[i].x * Box2dWorldController.UNITS_TO_PIXELS;
			lCurY = lInnerVertices[i].y * Box2dWorldController.UNITS_TO_PIXELS;

			lLengthOfSegment = Vector2f.distance(lCurX, lCurY, lPrevX, lPrevY) / 1024.f;
			lDistanceTravelled += lLengthOfSegment;

			final float lInnerPointX = lInnerVertices[i].x * Box2dWorldController.UNITS_TO_PIXELS;
			final float lInnerPointY = lInnerVertices[i].y * Box2dWorldController.UNITS_TO_PIXELS;
			final float lOuterPointX = lOuterVertices[i].x * Box2dWorldController.UNITS_TO_PIXELS;
			final float lOuterPointY = lOuterVertices[i].y * Box2dWorldController.UNITS_TO_PIXELS;

			addVertToBuffer(lInnerPointX, lInnerPointY, 0, 0.f, lDistanceTravelled);
			addVertToBuffer(lOuterPointX, lOuterPointY, 0, 1.f, lDistanceTravelled);

			lPrevX = lCurX;
			lPrevY = lCurY;

		}

		// TODO: Add last two vertices?

		mTrackBuffer.flip();

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
