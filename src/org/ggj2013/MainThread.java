package org.ggj2013;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class MainThread extends Thread {

	private static final String TAG = MainThread.class.getSimpleName();
	private final SurfaceHolder surfaceHolder;
	private final GameView view;
	private boolean running;
	public final Game game;

	public MainThread(FullscreenActivity activity, SurfaceHolder surfaceHolder,
			GameView view) {
		super();
		this.surfaceHolder = surfaceHolder;
		this.view = view;
		this.game = new Game(activity);
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void run() {
		while (running) {
			long tickCount = 0L;
			Log.d(TAG, "Starting game loop");

			while (running) {
				tickCount++;
				game.onUpdate();
				Canvas canvas = view.getHolder().lockCanvas();

				if (canvas != null) {
					game.onRender(canvas);
					view.getHolder().unlockCanvasAndPost(canvas);
				} else {
					Log.w("MainThread", "Canvas couldn't be locked.");
					running = false;
				}
			}

			Log.d(TAG, "Game loop executed " + tickCount + " times");
		}
	}
}
