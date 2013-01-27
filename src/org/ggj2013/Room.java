package org.ggj2013;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.ggj2013.FullscreenActivity.Movement;

import android.graphics.Canvas;
import android.util.Log;
import android.widget.Toast;

public class Room {

	enum Status {
		ACTIVE, WON, LOST
	}

	public LinkedList<Enemy> enemies;
	public Player player;
	public Damsel damsel;
	public Status status = Status.ACTIVE;
	private final Game context;

	private final Vector3D roomTopLeft;
	private final Vector3D roomTopRight;
	private final Vector3D roomBottomLeft;
	private final Vector3D roomBottomRight;

	public Room(RoomConfig cfg) {
		this.context = cfg.context;

		player = new Player("player");
		player.position = cfg.playerPosition;

		damsel = new Damsel("damsel");
		damsel.position = cfg.damselPosition;

		enemies = new LinkedList<Enemy>();
		for (int i = 0; i < cfg.enemies.size(); i++) {
			Map.Entry<Vector3D, Enemy.Size> e = cfg.enemies.entrySet()
					.iterator().next();
			Enemy enemy = new Enemy("enemy-" + i, e.getValue());
			enemy.position = e.getKey();
			enemies.add(enemy);
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

			context.soundManager.play(damsel.name, SoundPackStandard.GOAL,
					SoundManager.BALANCE_CENTER, 1f,
					SoundManager.LOOPS_INFINITE);

			for (Enemy e : enemies) {
				context.soundManager.play(e.name, e.getSoundName(),
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
			this.context.activity.vibrate();
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

		player.orientation = lowPass(context.activity.lastOrientation,
				player.orientation, timeDiff * 10f);

		if (context.activity.lastActivity == Movement.MOVING) {
			player.moveForward(timeDiff);
		}

		if (startedSound) {
			float[] balance = player.getBalanceForSoundFrom(damsel);
			context.soundManager.changeVolume(damsel.name, balance, 1);

			double minDistance = 10;

			for (Enemy enemy : enemies) {
				balance = player.getBalanceForSoundFrom(enemy);
				context.soundManager.changeVolume(enemy.name, balance, 1);

				double distance = player.distanceTo(enemy);
				if (distance < minDistance) {
					minDistance = distance;
				}
			}

			int newHeartbeatLevel;
			if (minDistance > 5) {
				newHeartbeatLevel = 1;
			} else if (minDistance > 4) {
				newHeartbeatLevel = 2;
			} else if (minDistance > 3) {
				newHeartbeatLevel = 3;
			} else if (minDistance > 2) {
				newHeartbeatLevel = 4;
			} else {
				newHeartbeatLevel = 5;
			}

			if (newHeartbeatLevel != player.heartbeatLevel) {
				player.heartbeatLevel = newHeartbeatLevel;
				player.playHeartBeatSound(context.soundManager);
			}
		}
	}

	private void onWonGame() {
		context.soundManager.stopAll();
		context.restart();

		context.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context.activity, "WON!", Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	private void onLostGame(Entity e) {
		context.restart();

		context.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context.activity, "LOST!", Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	public void onRender(Canvas c) {
		// Whoa! Fancy graphics... not! :-P
	}

	public static class RoomConfig {
		Game context;
		Vector3D playerPosition;
		Vector3D damselPosition;
		HashMap<Vector3D, Enemy.Size> enemies;

		Vector3D roomTopLeft;
		Vector3D roomTopRight;
		Vector3D roomBottomLeft;
		Vector3D roomBottomRight;
	}
}
