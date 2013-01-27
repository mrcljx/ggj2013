package org.ggj2013;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.ggj2013.FullscreenActivity.Movement;

import android.util.Log;

public class Room {

	enum Status {
		ACTIVE, WON, LOST
	}

	public static final String TAG = Room.class.getSimpleName();

	public final List<Enemy> enemies = new LinkedList<Enemy>();
	public final Player player;
	public final Damsel damsel;
	public Status status = Status.ACTIVE;
	private final Game game;

	private final float roomLeft;
	private final float roomRight;
	private final float roomTop;
	private final float roomBottom;

	public Room(Game game, RoomConfig cfg) {
		this.game = game;

		player = new Player("player");
		player.position = cfg.playerPosition;

		damsel = new Damsel("damsel");
		damsel.position = cfg.damselPosition;

		if (cfg.enemies != null) {
			int i = 0;
			for (Vector3D ePos : cfg.enemies.keySet()) {
				Enemy enemy = new Enemy("enemy-" + i++, cfg.enemies.get(ePos));
				enemy.position = ePos;
				enemies.add(enemy);
			}
		}

		roomLeft = cfg.left;
		roomRight = cfg.top;
		roomTop = cfg.top;
		roomBottom = cfg.bottom;

		if (roomLeft >= roomRight) {
			throw new RuntimeException("left bust be smaller than right");
		}

		if (roomBottom >= roomTop) {
			throw new RuntimeException("bottom bust be smaller than top");
		}
	}

	/**
	 * float[0] front float[1] left float[2] right float[3] back
	 * 
	 * @return
	 */
	public float[] getWallDistance(Player e) {
		float[] distance = new float[4];
		distance[0] = distance(Vector3D.PLUS_J);
		distance[1] = distance(Vector3D.MINUS_I);
		distance[2] = distance(Vector3D.PLUS_I);
		distance[3] = distance(Vector3D.MINUS_J);
		return distance;
	}

	private float distance(Vector3D direction) {

		Vector3D pos = player.position;
		Vector3D look = player.getLookDirection();
		Rotation rot = new Rotation(look, Entity.FORWARD);

		Vector3D dir = rot.applyInverseTo(direction);
		Vector3D other = pos.add(dir.scalarMultiply(100));

		double[] clip = MathUtils.CohenSutherlandLineClipAndDraw(pos.getX(),
				pos.getY(), other.getX(), other.getY(), roomLeft, roomBottom,
				roomRight, roomTop);

		if (clip != null) {
			Vector3D hit = new Vector3D(clip[2], clip[3], 0);
			float dist = (float) hit.subtract(pos).getNorm() - player.size;
			return Math.round(MathUtils.lerp(dist, 1, 3, 4, 0));
		} else {
			return 0;
		}
	}

	public boolean startedSound = false;

	private float ignoreMovementFor;

	public void tryStartSound() {
		if (!startedSound) {
			startedSound = true;

			game.soundManager.play(damsel.name, SoundPackStandard.DAMSEL,
					SoundManager.BALANCE_CENTER, 1f,
					SoundManager.LOOPS_INFINITE);
			int[] suitableSeekPoints = new int[] { 0, 21000, 37000, 55000 };

			int damselSeekTo = suitableSeekPoints[(int) (Math.random()
					* (3 - 0) + 0)];

			Log.d("Damsel seek", new Integer(damselSeekTo).toString());

			game.soundManager.streams.get(damsel.name).seekTo(damselSeekTo);

			for (Enemy e : enemies) {
				game.soundManager.play(e.name, e.getSoundName(),
						SoundManager.BALANCE_CENTER, 1f,
						SoundManager.LOOPS_INFINITE);
			}
		}
	}

	protected float lowPass(float newValue, float oldValue, float delta) {
		delta = MathUtils.clamp(delta, 0, 1);

		Vector3D newV = new Vector3D(Math.toRadians(newValue), 0);
		Vector3D oldV = new Vector3D(Math.toRadians(oldValue), 0);
		Rotation r = new Rotation(oldV, newV);
		float diff = (float) Math.toDegrees(r.getAngle());

		if (r.getAxis().getZ() < 0) {
			diff = -diff;
		}

		oldValue = MathUtils.wrap(oldValue + delta * diff, -180f, 180f);
		return oldValue;
	}

	public void onUpdate(float timeDiff) {
		if (this.status != Status.ACTIVE) {
			return;
		}

		tryStartSound();

		if (player.doCollision(roomLeft, roomBottom, roomRight, roomTop)) {
			Log.d(TAG, "Collision: Hit wall");
			player.moveForward(-0.5f);
			this.game.activity.vibrate(50);
			ignoreMovementFor = 0.25f + timeDiff;
		} else if (ignoreMovementFor <= 0) {
			player.orientation = lowPass(game.activity.lastOrientation,
					player.orientation, timeDiff * 10f);

			if (game.activity.lastActivity == Movement.MOVING) {
				player.moveForward(timeDiff);
			}
		}

		ignoreMovementFor -= timeDiff;
		ignoreMovementFor = Math.max(0, ignoreMovementFor);

		if (player.collidesWith(damsel)) {
			this.status = Status.WON;
			onWonGame();
			return;
		} else {
			for (Entity e : enemies) {
				if (player.collidesWith(e)) {
					this.status = Status.LOST;
					onLostGame(e);
					return;
				}
			}
		}

		if (startedSound) {
			float[] balance = player.getBalanceForSoundFrom(damsel);
			game.soundManager.changeVolume(damsel.name, balance, 1);

			double minDistance = 10;

			for (Enemy enemy : enemies) {
				balance = player.getBalanceForSoundFrom(enemy);
				game.soundManager.changeVolume(enemy.name, balance, 1);
				minDistance = Math.min(minDistance, player.distanceTo(enemy));
			}

			float newHeartbeatLevel = MathUtils.lerp((float) minDistance, 0, 7,
					1, 0);
			player.updateHeartbeatLevel(newHeartbeatLevel, game.soundManager);
		}
	}

	private void onWonGame() {
		game.isWon = true;
		game.soundManager.stopAll();

		game.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				game.soundManager.play("victory", SoundPackStandard.VICTORY,
						SoundManager.BALANCE_CENTER, 1f, SoundManager.LOOPS_0);
			}
		});
	}

	private void onLostGame(Entity e) {
		game.isLost = true;
		game.soundManager.stopAll();

		game.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				game.soundManager.play("killed_by_enemy",
						SoundPackStandard.KILLED_BY_ENEMY,
						SoundManager.BALANCE_CENTER, 1f, SoundManager.LOOPS_0);

			}
		});
	}

	// //////////////////////////////////////////////////////////////////////////
	// RoomConfig

	public static class RoomConfig {
		Vector3D playerPosition = Vector3D.ZERO;
		Vector3D damselPosition = new Vector3D(0, 5, 0);

		HashMap<Vector3D, Enemy.Size> enemies;

		float left = -10;
		float right = 10;
		float top = 10;
		float bottom = -10;
	}
}
