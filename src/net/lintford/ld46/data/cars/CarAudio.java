package net.lintford.ld46.data.cars;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.ResourceManager;
import net.lintford.library.core.audio.AudioListener;
import net.lintford.library.core.audio.AudioManager;
import net.lintford.library.core.audio.AudioSource;
import net.lintford.library.core.audio.data.AudioData;
import net.lintford.library.core.maths.MathHelper;

public class CarAudio {

	// ---------------------------------------------
	// Variables
	// ---------------------------------------------

	private AudioListener mAudioListener;

	private AudioSource mEngineSource0Low;
	private AudioSource mEngineSource0High;
	private AudioSource mEngineSource1Low;
	private AudioSource mEngineSource1High;

	private AudioData mAudioBuffer0_Low;
	private AudioData mAudioBuffer0_High;
	private AudioData mAudioBuffer1_Low;
	private AudioData mAudioBuffer1_High;

	// ---------------------------------------------
	// Core-Methods
	// ---------------------------------------------

	public void loadContent(ResourceManager pResourceManager) {

		mAudioListener = pResourceManager.audioManager().listener();

		mEngineSource0Low = pResourceManager.audioManager().getAudioSource(hashCode(), AudioManager.AUDIO_SOURCE_TYPE_SOUNDFX);
		mEngineSource0High = pResourceManager.audioManager().getAudioSource(hashCode(), AudioManager.AUDIO_SOURCE_TYPE_SOUNDFX);
		mEngineSource1Low = pResourceManager.audioManager().getAudioSource(hashCode(), AudioManager.AUDIO_SOURCE_TYPE_SOUNDFX);
		mEngineSource1High = pResourceManager.audioManager().getAudioSource(hashCode(), AudioManager.AUDIO_SOURCE_TYPE_SOUNDFX);

		mAudioBuffer0_Low = pResourceManager.audioManager().getAudioDataBufferByName("SOUND_ENGINE_0_LOW");
		mAudioBuffer0_High = pResourceManager.audioManager().getAudioDataBufferByName("SOUND_ENGINE_0_HIGH");
		mAudioBuffer1_Low = pResourceManager.audioManager().getAudioDataBufferByName("SOUND_ENGINE_1_LOW");
		mAudioBuffer1_High = pResourceManager.audioManager().getAudioDataBufferByName("SOUND_ENGINE_1_HIGH");

		mEngineSource0Low.setGain(0);
		mEngineSource1Low.setGain(0);

		mEngineSource0High.setGain(0);
		mEngineSource1High.setGain(0);

		mEngineSource0Low.setLooping(true);
		mEngineSource0High.setLooping(true);
		mEngineSource1Low.setLooping(true);
		mEngineSource1High.setLooping(true);

	}

	public void unloadContent() {
		mEngineSource0Low.unassign(hashCode());
		mEngineSource0High.unassign(hashCode());
		mEngineSource1Low.unassign(hashCode());
		mEngineSource1High.unassign(hashCode());

		mAudioBuffer0_Low = null;
		mAudioBuffer0_High = null;
		mAudioBuffer1_Low = null;
		mAudioBuffer1_High = null;

	}

	public void update(LintfordCore pCore, Car pCar) {
		final float lGameTimeModifer = pCore.gameTime().timeModifier();
		final float lNormalizedSpeed = pCar.currentSpeedNormalized();

		float lWorldGain = .5f;

		// 3d positional audio
		if (!pCar.isPlayerCar) {
			lWorldGain = 500.f;
			mEngineSource0Low.setPosition(pCar.x, pCar.y, 0f);
			mEngineSource1Low.setPosition(pCar.x, pCar.y, 0f);
			mEngineSource0High.setPosition(pCar.x, pCar.y, 0f);
			mEngineSource1High.setPosition(pCar.x, pCar.y, 0f);

			mEngineSource0Low.setVelocity(pCar.forwardUnitVelocity().x, pCar.forwardUnitVelocity().y, 0f);
			mEngineSource1Low.setVelocity(pCar.forwardUnitVelocity().x, pCar.forwardUnitVelocity().y, 0f);
			mEngineSource0High.setVelocity(pCar.forwardUnitVelocity().x, pCar.forwardUnitVelocity().y, 0f);
			mEngineSource1High.setVelocity(pCar.forwardUnitVelocity().x, pCar.forwardUnitVelocity().y, 0f);

		} else {
			mAudioListener.setPosition(pCar.x, pCar.y, 0f);
			mEngineSource0Low.setPosition(pCar.x, pCar.y, 0f);
			mEngineSource1Low.setPosition(pCar.x, pCar.y, 0f);
			mEngineSource0High.setPosition(pCar.x, pCar.y, 0f);
			mEngineSource1High.setPosition(pCar.x, pCar.y, 0f);

		}

		if (mEngineSource0Low != null) {
			float lHalfNormalized = lNormalizedSpeed * 1.5f * lNormalizedSpeed;
			mEngineSource0Low.setGain((1.f - lNormalizedSpeed) * lWorldGain);
			mEngineSource0High.setGain((lNormalizedSpeed * 1.3f) * lWorldGain);

			float s = MathHelper.clamp(-1f + lNormalizedSpeed * 2.f, 0f, 1f);
			mEngineSource1Low.setGain((0.4f + s) * lWorldGain);
			mEngineSource1High.setGain(0);

			mEngineSource0Low.setPitch(lGameTimeModifer * (1.f + lHalfNormalized));
			mEngineSource1Low.setPitch(lGameTimeModifer * (1.f + lHalfNormalized));
			mEngineSource0High.setPitch(lGameTimeModifer * (1.f + lHalfNormalized));
			mEngineSource1High.setPitch(lGameTimeModifer * (1.f + lHalfNormalized));

		}

	}

	// ---------------------------------------------
	// Methods
	// ---------------------------------------------

	public void startEngine() {
		mEngineSource0Low.play(mAudioBuffer0_Low.bufferID());
		mEngineSource0High.play(mAudioBuffer0_High.bufferID());
		mEngineSource1Low.play(mAudioBuffer1_Low.bufferID());
		mEngineSource1High.play(mAudioBuffer1_High.bufferID());

	}

	public void stopEngine() {
		if (mEngineSource0Low != null) {
			mEngineSource0Low.stop();
		}

		if (mEngineSource0High != null) {
			mEngineSource0High.stop();
		}

		if (mEngineSource1Low != null) {
			mEngineSource1Low.stop();
		}

		if (mEngineSource1High != null) {
			mEngineSource1High.stop();
		}

	}

}
