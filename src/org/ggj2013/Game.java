package org.ggj2013;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.ggj2013.Enemy.Size;
import org.ggj2013.Room.RoomConfig;

import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
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

	boolean debug = true;
	boolean isCalibrated = false;

	private long timeDiff = 2000000; // 2ms
	private long lastUpdate = -1;
	private Room currentRoom;
	private final int currentLevel;
	public final FullscreenActivity activity;
	public SoundManager soundManager;

	public Rect settingsBounds;
	public Rect resetBounds;

	private Path arrow;

	public Game(FullscreenActivity activity, int level) {
		this.activity = activity;
		currentLevel = level;
		soundManager = new SoundManager(activity.getApplicationContext());
		soundManager.loadSoundPack(new SoundPackStandard());

		createLevels();

		restart();
	}

	public void restart() {
		soundManager.stopAll();
		Log.e("Start Room", "" + currentLevel);
		currentRoom = new Room(levels.get(currentLevel - 1));
	}

	public void onPause() {
		soundManager.autoPause();
	}

	public void onResume() {
		soundManager.autoResume();
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

		int alpha = 255;

		Paint red = createPaint(Color.RED, textsize, alpha);
		Paint redinnerglow = createPaint(Color.RED, textsize, alpha);
		redinnerglow.setMaskFilter(new BlurMaskFilter(100, Blur.INNER));
		Paint redouterglow = createPaint(Color.RED, textsize, alpha);
		redouterglow.setMaskFilter(new BlurMaskFilter(100, Blur.OUTER));
		Paint white = createPaint(Color.WHITE, textsize * 2, alpha);
		Paint grey = createPaint(Color.DKGRAY, textsize * 2, alpha);
		Paint black = createPaint(Color.BLACK, textsize, alpha);
		Paint yellow = createPaint(Color.YELLOW, textsize, alpha);
		Paint green = createPaint(Color.GREEN, textsize, alpha);
		Paint greeninnerglow = createPaint(Color.GREEN, textsize, alpha);
		greeninnerglow.setMaskFilter(new BlurMaskFilter(100, Blur.INNER));
		Paint greenouterglow = createPaint(Color.GREEN, textsize, alpha);
		greenouterglow.setMaskFilter(new BlurMaskFilter(100, Blur.OUTER));

		if (arrow == null) {
			arrow = new Path();
			arrow.moveTo(0, 10);
			arrow.lineTo(-20, 30);
			arrow.lineTo(0, -30);
			arrow.lineTo(20, 30);
			arrow.close();
		}

		int centerX = c.getClipBounds().centerX();
		int centerY = c.getClipBounds().centerY();

		c.save();

		// bg
		c.drawRect(c.getClipBounds(), black);

		if (!isCalibrated) {
			float yy = centerY - 5 * textsize;
			drawTextCentered(c, red, "MAKE SHURE TO", w, centerX, yy);
			drawTextCentered(c, red, "HAVE FREE SPACE", w, centerX, yy
					+ textsize);
			drawTextCentered(c, red, "IN FRONT OF YOU.", w, centerX, yy + 2
					* textsize);

			int left = centerX - w / 4;
			int top = (int) (yy + 4 * textsize - 20);
			int right = centerX + w / 4;
			int bottom = (int) (yy + 7 * textsize);
			settingsBounds = new Rect(left, top, right, bottom);
			c.drawRect(settingsBounds, white);
			c.drawRect(new Rect(left + 4, top + 4, right - 4, bottom - 4),
					black);
			drawTextCentered(c, white, "OK", w, centerX, yy + 6 * textsize);

			return;
		}

		if (debug) {
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
		}

		if (currentRoom != null && currentRoom.player != null) {
			// enemies
			for (int i = 0; i < currentRoom.enemies.size(); i++) {
				Enemy e = currentRoom.enemies.get(i);
				if (debug) {
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
					double distance = currentRoom.player.distanceTo(e);
					c.drawText(String.format("%.1f", distance), x - textsize, y
							- textsize, yellow);
				}

				paintGlow(c, e, centerX, centerY, w, h, 5, redinnerglow,
						redouterglow);
			}

			// damsel
			// paintGlow(c, currentRoom.damsel, centerX, centerY, w, h, 10,
			// greeninnerglow, greenouterglow);

			if (debug) {
				// compass
				c.translate(centerX, centerY);
				c.rotate(-currentRoom.player.orientation);
				c.drawCircle(0, 0, (w / 2) - 20, white);
				c.drawCircle(0, 0, (w / 2) - 30, black);
				c.drawCircle(0, -(w / 2) + 20, 10, white);
			}

			// direction
			c.restore();
			c.save();
			c.translate(centerX, centerY);
			c.rotate(90f - (float) Math.toDegrees(currentRoom.player
					.relativeOrientationFor(currentRoom.damsel).getAlpha()));
			c.scale(5, 5);
			c.drawPath(arrow, grey);

			if (debug) {
				c.restore();
				c.save();
				c.drawText(String.format("%.1f",
						currentRoom.player.distanceTo(currentRoom.damsel)),
						centerX - textsize, centerY - textsize, green);
			}

			currentRoom.onRender(c);
		}

		// TODO player pulse fade
		alpha = (int) (127f + 128f * (Math
				.sin(System.currentTimeMillis() % 1000000 * 0.001f)));
		c.drawRect(c.getClipBounds(), createPaint(Color.BLACK, textsize, alpha));
	}

	private void paintGlow(Canvas c, Entity e, float centerX, float centerY,
			float w, float h, int minDistance, Paint inner, Paint outer) {
		float distance = currentRoom.player.distanceTo(e);
		if (distance < minDistance) {
			Vector3D v = currentRoom.player.relativeOrientationFor(e);
			double[] clip = MathUtils.CohenSutherlandLineClipAndDraw(centerX,
					centerY, v.getX() * 1000 + centerX, v.getY() * 1000
							+ centerY, 0, 0, w, h);
			float newX = (float) clip[2];
			float newY = h - (float) clip[3];
			float factor = distance * minDistance;
			newX = centerX - newX > 0 ? newX - factor : newX + factor;
			newY = centerY - newY > 0 ? newY - factor : newY + factor;
			c.restore();
			c.save();
			c.drawCircle(newX, newY, 100, inner);
			c.drawCircle(newX, newY, 100, outer);
		}
	}

	private Paint createPaint(int color, float textsize, int alpha) {
		Paint p = new Paint();
		p.setColor(color);
		p.setTextSize(textsize);
		p.setStyle(Style.FILL);
		p.setAlpha(alpha);
		// p.setAntiAlias(true);
		p.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		return p;
	}

	private void drawTextCentered(Canvas c, Paint paint, String text, float w,
			float centerX, float y) {
		// int length = paint.breakText(text, true, w, null);
		paint.setTextAlign(Paint.Align.CENTER);
		c.drawText(text, centerX, y, paint);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Levels

	LinkedList<RoomConfig> levels = new LinkedList<Room.RoomConfig>();

	private void createLevels() {
		// TODO Level 1
		RoomConfig cfg = new RoomConfig();
		levels.add(cfg);

		// TODO Level 2
		cfg = new RoomConfig();
		levels.add(cfg);

		// Level 3
		cfg = new RoomConfig();
		cfg.context = this;
		cfg.playerPosition = new Vector3D(0, 0, 0);
		cfg.damselPosition = new Vector3D(0, 10, 0);
		cfg.enemies = new HashMap<Vector3D, Enemy.Size>();
		cfg.enemies.put(new Vector3D(5, 5, 0), Size.MEDIUM);
		cfg.enemies.put(new Vector3D(-3, 8, 0), Size.BIG);
		cfg.enemies.put(new Vector3D(0, -3, 0), Size.SMALL);
		cfg.roomTopLeft = new Vector3D(-10, 10, 0);
		cfg.roomTopRight = new Vector3D(10, 10, 0);
		cfg.roomBottomLeft = new Vector3D(-10, -10, 0);
		cfg.roomBottomRight = new Vector3D(10, -10, 0);
		levels.add(cfg);

		// TODO Level 4
		cfg = new RoomConfig();
		levels.add(cfg);

		// TODO Level 5
		cfg = new RoomConfig();
		levels.add(cfg);
	}
}
