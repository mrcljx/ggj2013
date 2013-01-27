package org.ggj2013;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class Player extends Entity {

	public Player(String name) {
		super(name);
	}

	float HEARING_MINIMUM = 3f;
	int heartbeatLevel = 1;

	public float[] getBalanceForSoundFrom(Entity e) {
		Vector3D normalized = relativeOrientationFor(e);

		float[] balance = new float[2];

		balance[0] = 0.5f - (float) normalized.getX() * 0.5f;
		balance[1] = 0.5f + (float) normalized.getX() * 0.5f;

		float distance = distanceTo(e);
		float distanceVolume = 1;

		if (distance > HEARING_MINIMUM) {
			distanceVolume = 1f - ((distance - HEARING_MINIMUM) / (e.volume() - HEARING_MINIMUM));
		}

		distanceVolume = (float) Math.pow(
				MathUtils.clamp(distanceVolume, 0, 1), 2);

		if (normalized.getY() < 0) {
			if (balance[0] <= 0.5f) {
				balance[0] = 0;
			} else if (balance[1] <= 0.5f) {
				balance[1] = 0;
			}

			distanceVolume *= (float) Math
					.max(0, 1f + normalized.getY() * 1.3f);
		}

		balance[0] *= distanceVolume;
		balance[1] *= distanceVolume;

		return balance;
	}

	public Vector3D relativeOrientationFor(Entity e) {
		Vector3D look = getLookDirection();
		Rotation lookToOrigin = new Rotation(look, Entity.FORWARD);
		Vector3D toOther = vectorTo(e);

		if (toOther.getNormSq() == 0) {
			return Entity.FORWARD;
		} else {
			return lookToOrigin.applyTo(toOther.normalize());
		}
	}

	public boolean hitsWall(Vector3D wallTopLeft, Vector3D wallTopRight,
			Vector3D wallBottomLeft, Vector3D wallBottomRight) {

		if (wallTopLeft.getX() < this.position.getX() - this.size
				&& wallBottomLeft.getX() < this.position.getX() - this.size
				&& wallTopLeft.getY() > this.position.getY() + this.size
				&& wallBottomLeft.getY() < this.position.getY() - this.size

				&& wallTopRight.getX() > this.position.getX() + this.size
				&& wallBottomRight.getX() > this.position.getX() + this.size
				&& wallTopRight.getY() > this.position.getY() + this.size
				&& wallBottomRight.getY() < this.position.getY() - this.size) {
			return false;
		} else {
			return true;
		}
	}

	public void playHeartBeatSound(SoundManager soundManager) {
		if (heartbeatLevel == 5) {
			soundManager.play("player_heartbeat",
					SoundPackStandard.HEARTBEAT_05,
					SoundManager.BALANCE_CENTER, 1f,
					SoundManager.LOOPS_INFINITE);

		} else if (heartbeatLevel == 4) {
			soundManager.play("player_heartbeat",
					SoundPackStandard.HEARTBEAT_04,
					SoundManager.BALANCE_CENTER, 0.7f,
					SoundManager.LOOPS_INFINITE);
		} else if (heartbeatLevel == 3) {
			soundManager.play("player_heartbeat",
					SoundPackStandard.HEARTBEAT_03,
					SoundManager.BALANCE_CENTER, 0.5f,
					SoundManager.LOOPS_INFINITE);
		} else if (heartbeatLevel == 2) {
			soundManager.play("player_heartbeat",
					SoundPackStandard.HEARTBEAT_02,
					SoundManager.BALANCE_CENTER, 0.3f,
					SoundManager.LOOPS_INFINITE);
		} else {
			soundManager.play("player_heartbeat",
					SoundPackStandard.HEARTBEAT_01,
					SoundManager.BALANCE_CENTER, 0.2f,
					SoundManager.LOOPS_INFINITE);
		}
	}
}
