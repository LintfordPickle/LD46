package net.lintford.ld46.data.particles;

import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.controllers.core.particles.ParticleFrameworkController;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.core.particles.particlesystems.ParticleSystemInstance;

public class WispController {

	// --------------------------------------
	// Variables
	// --------------------------------------

	private ControllerManager mControllerManager;
	private ParticleFrameworkController mParticleController;
	private ParticleSystemInstance mWispParticleSystem;

	private int mEntityGroupId;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public WispController(ControllerManager pControllerManager, int pEntityGroupId) {
		mControllerManager = pControllerManager;
		mEntityGroupId = pEntityGroupId;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	public void initialize(LintfordCore pCore) {
		mParticleController = (ParticleFrameworkController) mControllerManager.getControllerByNameRequired(ParticleFrameworkController.CONTROLLER_NAME, mEntityGroupId);

		mWispParticleSystem = mParticleController.particleFrameworkData().particleSystemManager().getParticleSystemByName("PARTICLESYSTEM_WISP");

	}

	public void update(LintfordCore pCore) {

		{ // Spawn new particles
			if (RandomNumbers.getRandomChance(15)) {
				final var lMinX = pCore.gameCamera().getMinX();
				final var lMaxX = pCore.gameCamera().getMaxX();

				final var lMinY = pCore.gameCamera().getMinY();
				final var lMaxY = pCore.gameCamera().getMaxY();

				final float lWorldPositionX = RandomNumbers.random(lMinX, lMaxX);
				final float lWorldPositionY = RandomNumbers.random(lMinY, lMaxY);

				final float lWorldVelocityX = 0;
				final float lWorldVelocityY = 0;

				mWispParticleSystem.spawnParticle(lWorldPositionX, lWorldPositionY, lWorldVelocityX, lWorldVelocityY);
			}

		}

	}

}
