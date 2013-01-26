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
			currentRoom = new Room(activity);
		}
	}

	public void onUpdate() {
		long now = System.nanoTime();

		if (lastUpdate > 0) {
			timeDiff = now - lastUpdate;
		}

		lastUpdate = now;

		if (currentRoom != null) {
			float timeElapsed = timeDiff / 1000000000f;
			// Log.d("DELTA", Float.toString(timeElapsed));
			currentRoom.onUpdate(timeElapsed);
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
		c.drawText(String.format("X: %f, Y: %f",
				currentRoom.player.position.getX(),
				currentRoom.player.position.getY()), 30, 80, fg);

		int centerX = c.getClipBounds().centerX();
		int centerY = c.getClipBounds().centerY();

		Path arrow = new Path();
		arrow.moveTo(0, 10);
		arrow.lineTo(-20, 30);
		arrow.lineTo(0, -30);
		arrow.lineTo(20, 30);
		arrow.close();

		c.save();
		c.translate(centerX, centerY);
		c.rotate(-currentRoom.player.orientation);
		c.scale(5, 5);
		c.drawPath(arrow, fg);

		c.restore();
		c.translate(centerX, centerY);
		c.rotate(90f - (float) Math.toDegrees(currentRoom.player
				.relativeOrientationFor(currentRoom.damsel).getAlpha()));
		c.drawPath(arrow, bg);

		if (currentRoom != null) {
			currentRoom.onRender(c);
		}
	}
}
