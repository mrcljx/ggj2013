package org.ggj2013;

public class Game {
	private long timeDiff = 2000000; // 2ms
	private long lastUpdate = -1;
	private Room currentRoom;

	public Game() {
		currentRoom = new Room();
	}

	public void restart() {
		currentRoom = new Room();
	}

	public void onUpdate() {
		long now = System.nanoTime();

		if (lastUpdate > 0) {
			timeDiff = now - lastUpdate;
			lastUpdate = now;
		}

		if (currentRoom != null) {
			this.currentRoom.onUpdate(timeDiff);
		}
	}

	public void onRender() {
		if (currentRoom != null) {
			this.currentRoom.onRender();
		}
	}
}
