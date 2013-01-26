package org.ggj2013;

import java.util.LinkedList;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.ggj2013.FullscreenActivity.Movement;

import android.graphics.Canvas;
import android.widget.Toast;

public class Room {

	enum Status {
		ACTIVE, WON, LOST
	}

	public LinkedList<Enemy> enemies;
	public Player player;
	public Damsel damsel;
	public Status status = Status.ACTIVE;
	private final FullscreenActivity context;
	private final SoundManager soundManager;

	public Room(FullscreenActivity context) {
		this.context = context;

		player = new Player("player");
		damsel = new Damsel("damsel");
		damsel.position = player.position.add(new Vector3D(0, 10, 0));
		enemies = new LinkedList<Enemy>();

		Enemy enemy = new Enemy("enemy-1");
		enemy.position = new Vector3D(5, 5, 0);
		enemies.add(enemy);

		enemy = new Enemy("enemy-2");
		enemy.position = new Vector3D(-3, 8, 0);
		enemies.add(enemy);

		soundManager = new SoundManager(context.getApplicationContext());
		soundManager.loadSoundPack(new SoundPackStandard());
	}

	public boolean startedSound = false;

	public void tryStartSound() {
		if (soundManager.loaded && !startedSound) {
			startedSound = true;

			soundManager.play(damsel.name, SoundPackStandard.BEAT,
					SoundManager.BALANCE_CENTER, 1f, -1);

			for (Enemy e : enemies) {
				soundManager.play(e.name, SoundPackStandard.BREATH,
						SoundManager.BALANCE_CENTER, 1f, -1);
			}
		}
	}

	public void onUpdate(float timeDiff) {
		if (this.status != Status.ACTIVE) {
			return;
		}

		tryStartSound();

		if (player.collidesWith(damsel)) {
			this.status = Status.WON;
			onWonGame();
		} else {
			for (Entity e : enemies) {
				if (player.collidesWith(e)) {
					this.status = Status.LOST;
					onLostGame(e);
					break;
				}
			}
		}

		player.orientation = context.lastOrientation;

		if (context.lastActivity == Movement.MOVING) {
			player.moveForward(timeDiff);
		}

		if (startedSound) {
			float[] balance = player.getBalanceForSoundFrom(damsel);
			soundManager.changeVolume(damsel.name, balance, 1);

			for (Enemy enemy : enemies) {
				balance = player.getBalanceForSoundFrom(enemy);
				soundManager.changeVolume(enemy.name, balance, 1);
			}
		}
	}

	private void onWonGame() {
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, "WON!", Toast.LENGTH_LONG).show();
			}
		});
	}

	private void onLostGame(Entity e) {
		context.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, "LOST!", Toast.LENGTH_LONG).show();
			}
		});
	}

	public void onRender(Canvas c) {
		// Whoa! Fancy graphics... not! :-P
	}
}
