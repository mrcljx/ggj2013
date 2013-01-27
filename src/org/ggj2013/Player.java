package org.ggj2013;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import android.util.Log;

public class Player extends Entity {

	public Player(String name) {
		super(name);
	}

	float heartbeatLevel = 1f;

	public float[] getBalanceForSoundFrom(Entity e) {
		Vector3D normalized = relativeOrientationFor(e);

		float[] balance = new float[2];

		balance[0] = 0.5f - (float) normalized.getX() * 0.5f;
		balance[1] = 0.5f + (float) normalized.getX() * 0.5f;

		float distance = distanceTo(e);
		float distanceVolume = MathUtils.lerp(distance, 1, e.volume(), 1, 0);

		// if (distance > HEARING_MINIMUM) {
		// distanceVolume = 1f - ((distance - HEARING_MINIMUM) / (e.volume() -
		// HEARING_MINIMUM));
		// }

		// distanceVolume = (float) Math.pow(
		// MathUtils.clamp(distanceVolume, 0, 1), 2);

		if (normalized.getY() < 0) {
			/*
			 * if (balance[0] <= 0.5f) { balance[0] = 0; } else if (balance[1]
			 * <= 0.5f) { balance[1] = 0; }
			 */

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

	public boolean doCollision(float minX, float minY, float maxX, float maxY) {
		if (MathUtils.ComputeOutCode(position.getX(), position.getY(), minX,
				minY, maxX, maxY) == MathUtils.INSIDE) {
			return false;
		}

		float centerX = minX + (maxX - minX) * 0.5f;
		float centerY = minY + (maxY - minY) * 0.5f;

		double[] collision = MathUtils.CohenSutherlandLineClipAndDraw(centerX,
				centerY, position.getX(), position.getY(), minX, minY, maxX,
				maxY);

		if (collision == null) {
			throw new RuntimeException(
					"Line from center of room to player was outside of screen!?");
		}

		position = new Vector3D(collision[2], collision[3], 0);

		Log.i("Player", "Collsion moved player to " + position.toString());

		return true;
	}

	public void playHeartBeatSound(SoundManager soundManager) {
		String soundId = soundIdForHeartbeat(heartbeatLevel);

		if (soundId.equals(SoundPackStandard.HEARTBEAT_05)) {
			soundManager.play("player_heartbeat",
					SoundPackStandard.HEARTBEAT_05,
					SoundManager.BALANCE_CENTER, 1f,
					SoundManager.LOOPS_INFINITE);

		} else if (soundId.equals(SoundPackStandard.HEARTBEAT_04)) {
			soundManager.play("player_heartbeat",
					SoundPackStandard.HEARTBEAT_04,
					SoundManager.BALANCE_CENTER, 0.7f,
					SoundManager.LOOPS_INFINITE);
		} else if (soundId.equals(SoundPackStandard.HEARTBEAT_03)) {
			soundManager.play("player_heartbeat",
					SoundPackStandard.HEARTBEAT_03,
					SoundManager.BALANCE_CENTER, 0.5f,
					SoundManager.LOOPS_INFINITE);
		} else if (soundId.equals(SoundPackStandard.HEARTBEAT_02)) {
			soundManager.play("player_heartbeat",
					SoundPackStandard.HEARTBEAT_02,
					SoundManager.BALANCE_CENTER, 0.3f,
					SoundManager.LOOPS_INFINITE);
		} else if (soundId.equals(SoundPackStandard.HEARTBEAT_01)) {
			soundManager.play("player_heartbeat",
					SoundPackStandard.HEARTBEAT_01,
					SoundManager.BALANCE_CENTER, 0.2f,
					SoundManager.LOOPS_INFINITE);
		}
	}

	public String soundIdForHeartbeat(float level) {
		if (level > 0.8f) {
			return SoundPackStandard.HEARTBEAT_05;
		} else if (level > 0.6f) {
			return SoundPackStandard.HEARTBEAT_04;
		} else if (level > 0.4f) {
			return SoundPackStandard.HEARTBEAT_03;
		} else if (level > 0.2f) {
			return SoundPackStandard.HEARTBEAT_01;
		} else {
			return SoundPackStandard.HEARTBEAT_01;
		}
	}

	public void updateHeartbeatLevel(float newLevel, SoundManager soundManager) {
		newLevel = MathUtils.clamp(newLevel, 0, 1);

		String oldSoundId = soundIdForHeartbeat(heartbeatLevel);
		String newSoundId = soundIdForHeartbeat(newLevel);

		heartbeatLevel = newLevel;

		if (!oldSoundId.equals(newSoundId)) {
			Log.d("Player", "New heartbeat level: " + newSoundId);
			playHeartBeatSound(soundManager);
		}

	}
}
