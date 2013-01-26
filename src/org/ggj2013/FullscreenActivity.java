package org.ggj2013;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;

public class FullscreenActivity extends Activity implements
		SensorEventListener, OnTouchListener {

	private float mLastZ;

	public Movement lastActivity;

	private long lastActivityTimestamp;
	private long lastInactivityTimestamp;

	/**
	 * 0 = North, 180 = South
	 */
	float lastOrientation = -1;

	float orientationOffset = 0;

	private SensorManager sensorManager;

	private float[] gravity;

	private float[] magneticField;

	private GameView gameView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		gameView = new GameView(this);
		gameView.setOnTouchListener(this);
		setContentView(gameView);
	}

	@Override
	protected void onResume() {
		super.onResume();

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_GAME);

		gameView.game.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

		sensorManager.unregisterListener(this,
				sensorManager.getDefaultSensor((Sensor.TYPE_ACCELEROMETER)));
		sensorManager.unregisterListener(this,
				sensorManager.getDefaultSensor((Sensor.TYPE_MAGNETIC_FIELD)));

		gameView.game.onPause();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
			// return;
		}

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			this.gravity = event.values.clone();
			onAcceleratorEvent(event);
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			onCompassEvent(event);
		}
	}

	private final List<Float> orientations = new LinkedList<Float>();

	private float _lastOrientation;

	private void onCompassEvent(SensorEvent event) {
		this.magneticField = event.values.clone();

		if (gravity != null && magneticField != null) {
			float[] inR = new float[16];
			float[] I = new float[16];
			float[] orientVals = new float[3];

			boolean success = SensorManager.getRotationMatrix(inR, I, gravity,
					magneticField);

			if (success) {
				SensorManager.getOrientation(inR, orientVals);
				float orientation = (float) Math.toDegrees(orientVals[0]);

				if (orientations.size() > 25) {
					orientations.remove(0);
				}

				orientations.add(orientation);

				Vector3D sum = Vector3D.ZERO;

				for (float orient : orientations) {
					Vector3D dir = Vector3D.MINUS_J;
					Rotation rot = new Rotation(Vector3D.PLUS_K,
							Math.toRadians(orient));
					dir = rot.applyTo(dir);
					sum = sum.add(dir);
				}

				sum = sum.normalize();

				_lastOrientation = 90f + (float) Math.toDegrees(sum.getAlpha());
				lastOrientation = _lastOrientation - orientationOffset;
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	/*
	 * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
	 * value basically means more smoothing See:
	 * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
	 */

	enum Movement {
		NONE, MOVING
	}

	private void onAcceleratorEvent(SensorEvent event) {
		float LEG_THRSHOLD_AMPLITUDE = 1;
		long activityLag = 5;
		long inactivityLag = 500;
		long now = System.currentTimeMillis();

		final float z = event.values[2];
		final float zDiff = Math.abs(z - mLastZ);
		mLastZ = z;

		if (zDiff > LEG_THRSHOLD_AMPLITUDE) {
			lastActivityTimestamp = now;

			if (lastInactivityTimestamp + activityLag <= now) {
				if (lastActivity != Movement.MOVING) {
					lastActivity = Movement.MOVING;
					Log.d("SENSOR", "Move");
				}
			}
		} else {
			lastInactivityTimestamp = now;
			if (lastActivityTimestamp + inactivityLag <= now) {
				if (lastActivity != Movement.NONE) {
					lastActivity = Movement.NONE;
					Log.d("SENSOR", "Stop");
				}
			}
		}
	}

	public void vibrate() {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(50);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (gameView.game.settingsBounds != null
				&& gameView.game.settingsBounds.contains((int) event.getX(),
						(int) event.getY())) {
			orientationOffset = _lastOrientation;
		} else if (gameView.game.resetBounds != null
				&& gameView.game.resetBounds.contains((int) event.getX(),
						(int) event.getY())) {
			gameView.game.restart();
		}

		return true;
	}
}
