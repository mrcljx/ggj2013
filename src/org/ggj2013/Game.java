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
	public final FullscreenActivity activity;
	public SoundManager soundManager;

	public Game(FullscreenActivity activity) {
		this.activity = activity;
		soundManager = new SoundManager(activity.getApplicationContext());
		soundManager.loadSoundPack(new SoundPackStandard());
		restart();
	}

	public void restart() {
		currentRoom = null;
		currentLevel = 0;
		soundManager.stopAll();
		onNextLevel();
	}

	public void onPause() {
		soundManager.autoPause();
	}

	public void onResume() {
		soundManager.autoResume();
	}

	public void onNextLevel() {
		currentLevel++;

		if (currentLevel == 10) {
			currentRoom = null;
			Log.e("GAME", "WON!");
		} else {
			currentRoom = new Room(this);
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

		int w = c.getWidth();
		int h = c.getHeight();
		float textsize = 64 * w / 1000;

		Paint ar = new Paint();
		ar.setColor(Color.WHITE);
		ar.setAntiAlias(true);
		Paint cp = new Paint();
		cp.setColor(Color.BLACK);
		cp.setTextSize(textsize);
		cp.setStyle(Style.FILL);
		cp.setAntiAlias(true);
		cp.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

		Paint fg = new Paint();
		fg.setColor(Color.BLACK);
		fg.setTextSize(textsize);
		fg.setStyle(Style.FILL);
		fg.setAntiAlias(true);
		fg.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

		c.drawText(String.format("X: %f; Y: %f",
				currentRoom.player.position.getX(),
				currentRoom.player.position.getY()), 30, 80, fg);

		int centerX = c.getClipBounds().centerX();
		int centerY = c.getClipBounds().centerY();

		c.save();

		c.translate(centerX, centerY);
		c.rotate(-currentRoom.player.orientation);
		c.drawCircle(0, 0, (w / 2) - 20, cp);
		c.drawCircle(0, 0, (w / 2) - 30, bg);
		c.drawText("N", 0, -(w / 2) + 20, cp);

		Path arrow = new Path();
		arrow.moveTo(0, 10);
		arrow.lineTo(-20, 30);
		arrow.lineTo(0, -30);
		arrow.lineTo(20, 30);
		arrow.close();

		// c.restore();
		// c.translate(centerX, centerY);
		// c.rotate(-currentRoom.player.orientation);
		// c.scale(5, 5);
		// c.drawPath(arrow, fg);

		c.restore();
		c.translate(centerX, centerY);
		c.rotate(90f - (float) Math.toDegrees(currentRoom.player
				.relativeOrientationFor(currentRoom.damsel).getAlpha()));
		c.scale(5, 5);
		c.drawPath(arrow, ar);

		if (currentRoom != null) {
			currentRoom.onRender(c);
		}
	}
}
