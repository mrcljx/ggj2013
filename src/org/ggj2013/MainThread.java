package org.ggj2013;

import android.util.Log;
import android.view.SurfaceHolder;

public class MainThread extends Thread {

	private static final String TAG = MainThread.class.getSimpleName();
	private final SurfaceHolder surfaceHolder;
	private final GameView view;
	private boolean running;

	public MainThread(SurfaceHolder surfaceHolder, GameView view) {
		super();
		this.surfaceHolder = surfaceHolder;
		this.view = view;
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
				// update game state
				// render state to the screen
			}
			Log.d(TAG, "Game loop executed " + tickCount + " times");
		}
	}
}
