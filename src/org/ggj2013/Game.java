package org.ggj2013;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.Log;

public class Game {
	private long timeDiff = 2000000; // 2ms
	private long lastUpdate = -1;
	private Room currentRoom;
	private int currentLevel;
	private final FullscreenActivity activity;

	public Game(FullscreenActivity activity) {
		this.activity = activity;
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
		c.setMatrix(new Matrix());

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

		int centerX = c.getClipBounds().centerX();
		int centerY = c.getClipBounds().centerY();

		Path arrow = new Path();
		arrow.moveTo(0, 10);
		arrow.lineTo(-20, 30);
		arrow.lineTo(0, -30);
		arrow.lineTo(20, 30);
		arrow.close();

		c.translate(centerX, centerY);
		c.rotate(-activity.dLastOrientation);
		c.scale(5, 5);
		c.drawPath(arrow, fg);
		// c.drawRect(new Rect(centerX - 30, centerY - 30, 200, 280), fg);

		if (currentRoom != null) {
			currentRoom.onRender(c);
		}
	}
}
