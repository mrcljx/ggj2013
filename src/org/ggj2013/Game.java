package org.ggj2013;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.Log;

public class Game {
	private long timeDiff = 2000000; // 2ms
	private long lastUpdate = -1;
	private Room currentRoom;
	private int currentLevel;

	public Game() {
		restart();
	}

	public void restart() {
		currentRoom = null;
		currentLevel = 0;
		onNextLevel();
	}

	public void onNextLevel() {
		currentLevel++;

		if (currentLevel == 10) {
			currentRoom = null;
			Log.e("GAME", "WON!");
		} else {
			currentRoom = new Room();
		}
	}

	public void onUpdate() {
		long now = System.nanoTime();

		if (lastUpdate > 0) {
			timeDiff = now - lastUpdate;
			lastUpdate = now;
		}

		if (currentRoom != null) {
			currentRoom.onUpdate(timeDiff);
		}
	}

	public void onRender(Canvas c) {
		Paint bg = new Paint();
		bg.setColor(Color.RED);
		c.drawRect(c.getClipBounds(), bg);

		Paint fg = new Paint();
		fg.setColor(Color.BLACK);
		fg.setTextSize(64);
		fg.setStyle(Style.FILL);
		fg.setAntiAlias(true);
		fg.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		c.drawText("Hello", 50, 50, fg);

		if (currentRoom != null) {
			currentRoom.onRender(c);
		}
	}
}
