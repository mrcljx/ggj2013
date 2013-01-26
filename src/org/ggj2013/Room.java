package org.ggj2013;

import java.util.LinkedList;

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
	private final FullscreenActivity context;

	public Room(FullscreenActivity context) {
		this.player = new Player();
		this.damsel = new Damsel();
		this.damsel.position = this.damsel.position.add(new Vector3D(0, 10, 0));
		this.enemies = new LinkedList<Enemy>();
		this.context = context;
	}

	public void onUpdate(float timeDiff) {
		if (this.status != Status.ACTIVE) {
			return;
		}

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

		if (context.lastActivity == Movement.MOVING) {
			Vector3D movement = new Vector3D(0, 1, 0);
			Rotation rotation = new Rotation(Vector3D.PLUS_J,
					context.dLastOrientation);
			movement = rotation.applyTo(movement);
			player.position = player.position.add(timeDiff, movement);

			Log.d("Room Player Pos", player.position.toString());
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
