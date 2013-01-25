package org.ggj2013;

import java.util.LinkedList;

public class Room {

	enum Status {
		ACTIVE, WON, LOST
	}

	public LinkedList<Enemy> enemies;
	public Player player;
	public Damsel damsel;
	public Status status = Status.ACTIVE;

	public Room() {
		this.player = new Player();
		this.damsel = new Damsel();
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
	}

	private void onWonGame() {
		// TODO Auto-generated method stub

	}

	private void onLostGame(Entity e) {
		// TODO Auto-generated method stub

	}

	public void onRender() {
		// Whoa! Fancy graphics... not! :-P
	}
}
