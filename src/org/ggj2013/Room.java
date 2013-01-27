package org.ggj2013;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.ggj2013.FullscreenActivity.Movement;

import android.util.Log;

public class Room {

	enum Status {
		ACTIVE, WON, LOST
	}

	public final List<Enemy> enemies;
	public final Player player;
	public final Damsel damsel;
	public Status status = Status.ACTIVE;
	private final Game game;

	private final Vector3D roomTopLeft;
	private final Vector3D roomTopRight;
	private final Vector3D roomBottomLeft;
	private final Vector3D roomBottomRight;

	public Room(Game game, RoomConfig cfg) {
		this.game = game;

		player = new Player("player");

		if (cfg.playerPosition != null) {
			player.position = cfg.playerPosition;
		}

		damsel = new Damsel("damsel");

		if (cfg.damselPosition != null) {
			damsel.position = cfg.damselPosition;
		}

		enemies = new LinkedList<Enemy>();

		if (cfg.enemies != null) {
			for (int i = 0; i < cfg.enemies.size(); i++) {
				Map.Entry<Vector3D, Enemy.Size> e = cfg.enemies.entrySet()
						.iterator().next();
				Enemy enemy = new Enemy("enemy-" + i, e.getValue());
				enemy.position = e.getKey();
				enemies.add(enemy);
			}
		}

		roomTopLeft = cfg.roomTopLeft;
		roomTopRight = cfg.roomTopRight;
		roomBottomLeft = cfg.roomBottomLeft;
		roomBottomRight = cfg.roomBottomRight;
	}

	public boolean startedSound = false;

	public void tryStartSound() {
		if (!startedSound) {
			startedSound = true;

			game.soundManager.play(damsel.name, SoundPackStandard.DAMSEL,
					SoundManager.BALANCE_CENTER, 1f,
					SoundManager.LOOPS_INFINITE);

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

		if (player.collidesWith(damsel)) {
			this.status = Status.WON;
			onWonGame();
			return;
		} else if (player.hitsWall(roomTopLeft, roomTopRight, roomBottomLeft,
				roomBottomRight)) {
			Log.d("Collision", "Hits wall");
			player.moveForward(-0.5f);
			this.game.activity.vibrate();
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

		player.orientation = lowPass(game.activity.lastOrientation,
				player.orientation, timeDiff * 10f);

		if (game.activity.lastActivity == Movement.MOVING) {
			player.moveForward(timeDiff);
		}

		if (startedSound) {
			float[] balance = player.getBalanceForSoundFrom(damsel);
			game.soundManager.changeVolume(damsel.name, balance, 1);

			double minDistance = 10;

			for (Enemy enemy : enemies) {
				balance = player.getBalanceForSoundFrom(enemy);
				game.soundManager.changeVolume(enemy.name, balance, 1);

				double distance = player.distanceTo(enemy);
				if (distance < minDistance) {
					minDistance = distance;
				}
			}

			int newHeartbeatLevel;
			if (minDistance > 7) {
				newHeartbeatLevel = 1;
			} else if (minDistance > 5) {
				newHeartbeatLevel = 2;
			} else if (minDistance > 4) {
				newHeartbeatLevel = 3;
			} else if (minDistance > 3) {
				newHeartbeatLevel = 4;
			} else {
				newHeartbeatLevel = 5;
			}

			if (newHeartbeatLevel != player.heartbeatLevel) {
				player.heartbeatLevel = newHeartbeatLevel;
				player.playHeartBeatSound(game.soundManager);
			}
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
		Vector3D playerPosition;
		Vector3D damselPosition;

		HashMap<Vector3D, Enemy.Size> enemies;

		Vector3D roomTopLeft = new Vector3D(-10, 10, 0);
		Vector3D roomTopRight = new Vector3D(10, 10, 0);
		Vector3D roomBottomLeft = new Vector3D(-10, -10, 0);
		Vector3D roomBottomRight = new Vector3D(10, -10, 0);
	}
}
