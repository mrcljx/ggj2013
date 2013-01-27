package org.ggj2013;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.ggj2013.Enemy.Size;
import org.ggj2013.Room.RoomConfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.MotionEvent;

public class Game {

	public static final String TAG = Game.class.getSimpleName();

	private final boolean debug = true;

	public boolean isCalibrated = false;
	public boolean isWon = false;
	public boolean isLost = false;

	public long startTime;
	public long endTime = Long.MAX_VALUE;

	private long timeDiff = 2000000; // 2ms
	private long lastUpdate = -1;
	private Room currentRoom;
	private final int currentLevel;
	public final FullscreenActivity activity;
	public SoundManager soundManager;

	public Rect settingsBounds;
	public Rect backBounds;
	public Rect resetBounds;

	private Path arrow;
	private final SimpleDateFormat df = new SimpleDateFormat("mm:ss:SSS");

	private final long startedAt;

	private double runningForSeconds;

	private final Bitmap glowBitmap;

	private int centerY;

	private int centerX;

	private int screenWidth;

	private int screenHeight;

	public Game(FullscreenActivity activity, int level) {
		this.activity = activity;
		currentLevel = level;
		soundManager = new SoundManager(activity.getApplicationContext());
		soundManager.loadSoundPack(new SoundPackStandard());

		startedAt = System.currentTimeMillis();

		createLevels();

		restart();

		glowBitmap = BitmapFactory.decodeResource(activity.getResources(),
				R.drawable.glow);
	}

	public void restart() {
		isWon = false;
		isLost = false;
		isCalibrated = false;
		resetBounds = null;
		backBounds = null;
		settingsBounds = null;
		soundManager.stopAll();
		Log.i(TAG, String.format("(Re-)started Room %d", currentLevel));
		currentRoom = new Room(this, levels.get(currentLevel - 1));
	}

	public void onPause() {
		soundManager.autoPause();
	}

	public void onResume() {
		soundManager.autoResume();
	}

	public void onUpdate() {
		long now = System.nanoTime();
		runningForSeconds = ((System.currentTimeMillis() - startedAt) * 0.001);

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
		Paint white = createPaint(Color.WHITE, textsize * 2, alpha);
		Paint gray = createPaint(Color.DKGRAY, textsize * 2, alpha);
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

		centerX = c.getClipBounds().centerX();
		centerY = c.getClipBounds().centerY();
		screenWidth = c.getWidth();
		screenHeight = c.getHeight();

		c.save();

		// bg
		c.drawRect(c.getClipBounds(), black);

		if (!isCalibrated) {
			float yy = centerY - 5 * textsize;
			drawTextCentered(c, red, "MAKE SHURE TO", w, centerX, yy);
			yy += textsize;
			drawTextCentered(c, red, "HAVE FREE SPACE", w, centerX, yy);
			yy += textsize;
			drawTextCentered(c, red, "IN FRONT OF YOU.", w, centerX, yy);

			yy += 2 * textsize;
			int left = centerX - w / 4;
			int top = (int) (yy + -20);
			int right = centerX + w / 4;
			yy += 3 * textsize;
			int bottom = (int) yy;
			settingsBounds = new Rect(left, top, right, bottom);
			c.drawRect(settingsBounds, white);
			c.drawRect(new Rect(left + 4, top + 4, right - 4, bottom - 4),
					black);
			yy -= textsize;
			drawTextCentered(c, white, "OK", w, centerX, yy);

			return;
		}

		long now = System.currentTimeMillis();

		if (isWon) {
			long time = now - startTime;
			if (time < endTime)
				endTime = time;
			setHighscore(currentLevel, endTime);

			c.drawRect(c.getClipBounds(), white);
			Paint p = createPaint(Color.DKGRAY, textsize, alpha);

			float yy = centerY - 5 * textsize;
			drawTextCentered(c, p, "YOU DID IT,", w, centerX, yy);
			yy += textsize;
			drawTextCentered(c, p, "YOU SAVED HER!", w, centerX, yy);
			yy += textsize * 2;
			drawTextCentered(c, p,
					"YOUR TIME: " + df.format(new Date(endTime)), w, centerX,
					yy);

			yy += 2 * textsize;
			int left = centerX + 20 - w / 2;
			int top = (int) (yy + -20);
			int right = centerX - 20 + w / 2;
			yy += 3 * textsize;
			int bottom = (int) yy;
			backBounds = new Rect(left, top, right, bottom);
			p = createPaint(Color.BLACK, textsize * 2, alpha);
			c.drawRect(backBounds, p);
			c.drawRect(new Rect(left + 4, top + 4, right - 4, bottom - 4),
					white);
			yy -= textsize;
			drawTextCentered(c, p, "CONTINUE", w, centerX, yy);

			return;
		}

		if (isLost) {
			c.drawRect(c.getClipBounds(), red);
			Paint p = createPaint(Color.WHITE, textsize, alpha);

			float yy = centerY - 5 * textsize;
			drawTextCentered(c, p, "YOU ARE DEAD! ", w, centerX, yy);
			yy += textsize * 2;
			drawTextCentered(c, p, "MIND THE CREATURES,", w, centerX, yy);
			yy += textsize;
			drawTextCentered(c, p, "THEY BITE!", w, centerX, yy);

			yy += 2 * textsize;
			int left = centerX + 20 - w / 2;
			int top = (int) (yy + -20);
			int right = centerX - 20 + w / 2;
			yy += 3 * textsize;
			int bottom = (int) yy;
			backBounds = new Rect(left, top, right, bottom);
			c.drawRect(backBounds, white);
			c.drawRect(new Rect(left + 4, top + 4, right - 4, bottom - 4), red);
			yy -= textsize;
			drawTextCentered(c, white, "MAIN MENU", w, centerX, yy);

			yy += 2 * textsize;
			left = centerX + 20 - w / 2;
			top = (int) (yy + -20);
			right = centerX - 20 + w / 2;
			yy += 3 * textsize;
			bottom = (int) yy;
			resetBounds = new Rect(left, top, right, bottom);
			c.drawRect(resetBounds, white);
			c.drawRect(new Rect(left + 4, top + 4, right - 4, bottom - 4), red);
			yy -= textsize;
			drawTextCentered(c, white, "TRY AGAIN", w, centerX, yy);

			return;
		}

		if (debug) {
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

			// renderDamsel(c);

			if (debug) {
				renderDebugCompass(c, white);
				renderDebugEnemies(c, textsize, yellow);
			}

			renderEnemies(c);
			renderDirection(c, gray);

			if (debug) {
				c.drawText(String.format("x: %.2f / y: %.2f",
						currentRoom.player.position.getX(),
						currentRoom.player.position.getY()), centerX, 80, green);

				c.drawText(String.format("%.1f",
						currentRoom.player.distanceTo(currentRoom.damsel)),
						centerX - textsize, centerY - textsize, green);
			}
		}

		alpha = (int) (127f + 128f * (Math.sin(runningForSeconds * 2)));
		c.drawRect(c.getClipBounds(), createPaint(Color.BLACK, textsize, alpha));

		Paint p = createPaint(Color.WHITE, textsize - 10, alpha);
		p.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
		drawTextCentered(
				c,
				p,
				"Level " + currentLevel + " "
						+ df.format(new Date(now - startTime)), w, centerX,
				textsize);
	}

	private void renderDamsel(Canvas c) {
		drawGlow(c, currentRoom.damsel, centerX, centerY, screenWidth,
				screenHeight, 2);
	}

	private void renderDirection(Canvas c, Paint gray) {
		// direction
		c.save();
		c.translate(centerX, centerY);
		c.rotate(90f - (float) Math.toDegrees(currentRoom.player
				.relativeOrientationFor(currentRoom.damsel).getAlpha()));
		c.scale(5, 5);
		c.drawPath(arrow, gray);
		c.restore();
	}

	private void renderEnemies(Canvas c) {
		for (Enemy e : currentRoom.enemies) {
			drawGlow(c, e, centerX, centerY, screenWidth, screenHeight, 5);
		}
	}

	private void renderDebugEnemies(Canvas c, float textsize, Paint yellow) {
		int enemyIndex = 0;
		for (Enemy e : currentRoom.enemies) {
			c.save();
			float x = centerX
					+ ((screenWidth / 5) * (enemyIndex++
							- currentRoom.enemies.size() + 2));
			float y = centerY - (screenWidth / 5);
			c.translate(x, y);
			c.rotate(90f - (float) Math.toDegrees(currentRoom.player
					.relativeOrientationFor(e).getAlpha()));
			c.drawPath(arrow, yellow);
			c.setMatrix(new Matrix());
			double distance = currentRoom.player.distanceTo(e);
			c.drawText(String.format("%.1f", distance), x - textsize, y
					- textsize, yellow);
			c.restore();
		}
	}

	private void renderDebugCompass(Canvas c, Paint white) {
		// compass
		c.save();
		c.translate(centerX, centerY);
		c.rotate(-currentRoom.player.orientation);
		Paint compassPaint = new Paint(white);
		compassPaint.setStyle(Style.STROKE);
		compassPaint.setStrokeWidth(10);
		c.drawCircle(0, 0, (screenWidth / 2) - 20, compassPaint);
		// c.drawCircle(0, 0, (w / 2) - 30, black);
		c.drawCircle(0, -(screenWidth / 2) + 20, 10, white);
		c.restore();
	}

	// //////////////////////////////////////////////////////////////////////////
	// draw helper

	private void drawGlow(Canvas c, Entity e, float centerX, float centerY,
			float w, float h, int minDistance) {
		float distance = currentRoom.player.distanceTo(e);

		if (distance < minDistance) {
			c.save();
			c.setMatrix(new Matrix());

			Vector3D v = currentRoom.player.relativeOrientationFor(e);
			double[] clip = MathUtils.CohenSutherlandLineClipAndDraw(centerX,
					centerY, v.getX() * 1000 + centerX, v.getY() * 1000
							+ centerY, 0, 0, w, h);
			float newX = (float) clip[2];
			float newY = h - (float) clip[3];

			float halfH = glowBitmap.getHeight() / 2;
			float halfW = glowBitmap.getWidth() / 2;

			float scale = MathUtils.lerp(distance, 0, minDistance, 0.5f, 0.1f);
			float alpha = MathUtils.lerp(distance, minDistance * 0.75f,
					minDistance, 1, 0);

			c.translate(newX, newY);
			c.scale(scale, scale);

			Paint paint = new Paint();
			paint.setAlpha(Math.round(alpha * 255f));
			paint.setDither(true);
			c.drawBitmap(glowBitmap, -halfW, -halfH, paint);

			c.restore();
		}
	}

	private Paint createPaint(int color, float textsize, int alpha) {
		Paint p = new Paint();
		p.setColor(color);
		p.setTextSize(textsize);
		p.setStyle(Style.FILL);
		p.setAlpha(alpha);
		p.setAntiAlias(true);
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
	// HighScore

	public long getHighscore(int level) {
		SharedPreferences prefs = activity.getSharedPreferences("DarkPulse",
				Context.MODE_PRIVATE);
		return prefs.getLong("level-" + level, 9999999);
	}

	public void setHighscore(int level, long time) {
		long old = getHighscore(level);
		if (old < time)
			return;

		SharedPreferences prefs = activity.getSharedPreferences("DarkPulse",
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putLong("level-" + level, time);
		editor.commit();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Levels

	LinkedList<RoomConfig> levels = new LinkedList<Room.RoomConfig>();

	private void createLevels() {
		// TODO Level 1
		RoomConfig cfg = new RoomConfig();
		cfg.playerPosition = new Vector3D(0, 0, 0);
		cfg.damselPosition = new Vector3D(0, 3, 0);
		cfg.left = -4;
		cfg.right = 4;
		cfg.top = 5;
		cfg.bottom = -2;
		levels.add(cfg);

		// TODO Level 2
		cfg = new RoomConfig();
		cfg.playerPosition = new Vector3D(0, 0, 0);
		cfg.damselPosition = new Vector3D(0, 10, 0);
		cfg.enemies = new HashMap<Vector3D, Enemy.Size>();
		cfg.enemies.put(new Vector3D(0, 5, 0), Size.SMALL);
		cfg.left = -4;
		cfg.right = 4;
		cfg.top = 10;
		cfg.bottom = -10;
		levels.add(cfg);

		// Level 3
		cfg = new RoomConfig();
		cfg.playerPosition = new Vector3D(0, 0, 0);
		cfg.damselPosition = new Vector3D(0, 10, 0);
		cfg.enemies = new HashMap<Vector3D, Enemy.Size>();
		cfg.enemies.put(new Vector3D(5, 5, 0), Size.MEDIUM);
		cfg.enemies.put(new Vector3D(-3, 8, 0), Size.BIG);
		cfg.enemies.put(new Vector3D(0, -3, 0), Size.SMALL);
		cfg.left = -5;
		cfg.right = 5;
		cfg.top = 10;
		cfg.bottom = -10;
		levels.add(cfg);

		// TODO Level 4
		cfg = new RoomConfig();
		levels.add(cfg);

		// TODO Level 5
		cfg = new RoomConfig();
		levels.add(cfg);
	}

	public void onTouch(MotionEvent event) {
		if (settingsBounds != null
				&& settingsBounds.contains((int) event.getX(),
						(int) event.getY())) {
			restart();
			activity.orientationOffset = activity._lastOrientation;
			isCalibrated = true;
			startTime = System.currentTimeMillis();
		} else if (resetBounds != null
				&& resetBounds.contains((int) event.getX(), (int) event.getY())) {
			restart();
		} else if (backBounds != null
				&& backBounds.contains((int) event.getX(), (int) event.getY())) {
			soundManager.stopAll();
			activity.finish();
		}
	}
}
