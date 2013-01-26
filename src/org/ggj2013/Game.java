package org.ggj2013;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

public class Game {
	private long timeDiff = 2000000; // 2ms
	private long lastUpdate = -1;
	private Room currentRoom;
	private int currentLevel;
	public final FullscreenActivity activity;
	public SoundManager soundManager;

	public Rect settingsBounds;
	public Rect resetBounds;

	private Path arrow;

	private Paint red;
	private Paint white;
	private Paint black;
	private Paint yellow;
	private Paint green;

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

		int w = c.getWidth();
		int h = c.getHeight();
		float textsize = 64 * w / 1000;

		if (red == null)
			red = createPaint(Color.RED, textsize);
		if (white == null)
			white = createPaint(Color.WHITE, textsize * 2);
		if (black == null)
			black = createPaint(Color.BLACK, textsize);
		if (yellow == null)
			yellow = createPaint(Color.YELLOW, textsize);
		if (green == null)
			green = createPaint(Color.GREEN, textsize);

		int centerX = c.getClipBounds().centerX();
		int centerY = c.getClipBounds().centerY();

		c.save();

		// bg
		c.drawRect(c.getClipBounds(), black);

		// debug
		c.drawText(String.format("x: %.2f / y: %.2f",
				currentRoom.player.position.getX(),
				currentRoom.player.position.getY()), centerX, 80, green);

		// settings
		int left = w - (h / 10);
		int top = h - (h / 10);
		int right = w - 10;
		int bottom = h - 10;
		settingsBounds = new Rect(left, top, right, bottom);
		c.drawRect(settingsBounds, red);
		c.drawText("S", left + textsize / 2, top + textsize * 2, white);

		left = 10;
		right = left + settingsBounds.width();
		resetBounds = new Rect(left, top, right, bottom);
		c.drawRect(resetBounds, red);
		c.drawText("R", left + textsize / 2, top + textsize * 2, white);

		if (currentRoom != null) {
			// compass
			c.translate(centerX, centerY);
			c.rotate(-currentRoom.player.orientation);
			c.drawCircle(0, 0, (w / 2) - 20, white);
			c.drawCircle(0, 0, (w / 2) - 30, black);
			c.drawText("N", 0, -(w / 2) + 20, white);

			// direction
			if (arrow == null) {
				arrow = new Path();
				arrow.moveTo(0, 10);
				arrow.lineTo(-20, 30);
				arrow.lineTo(0, -30);
				arrow.lineTo(20, 30);
				arrow.close();
			}

			c.restore();
			c.save();
			c.translate(centerX, centerY);
			c.rotate(90f - (float) Math.toDegrees(currentRoom.player
					.relativeOrientationFor(currentRoom.damsel).getAlpha()));
			c.scale(5, 5);
			c.drawPath(arrow, red);
			c.restore();
			c.save();
			c.drawText(
					String.format("%.1f",
							currentRoom.player.distanceTo(currentRoom.damsel)),
					centerX - textsize, centerY - textsize, black);

			// enemies
			for (int i = 0; i < currentRoom.enemies.size(); i++) {
				Enemy e = currentRoom.enemies.get(i);
				c.restore();
				c.save();
				float x = centerX
						+ ((w / 5) * (i - currentRoom.enemies.size() + 2));
				float y = centerY - (w / 5);
				c.translate(x, y);
				c.rotate(90f - (float) Math.toDegrees(currentRoom.player
						.relativeOrientationFor(e).getAlpha()));
				c.drawPath(arrow, yellow);
				c.restore();
				c.save();
				c.drawText(
						String.format("%.1f", currentRoom.player.distanceTo(e)),
						x - textsize, y - textsize, yellow);
			}

			currentRoom.onRender(c);
		}
	}

	private Paint createPaint(int color, float textsize) {
		Paint p = new Paint();
		p.setColor(color);
		p.setTextSize(textsize);
		p.setStyle(Style.FILL);
		// p.setAntiAlias(true);
		p.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		return p;
	}
}
