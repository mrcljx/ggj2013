package org.ggj2013;

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
import android.view.Window;

public class FullscreenActivity extends Activity implements SensorEventListener {

	private float mLastZ;

	public Movement lastActivity;

	private long lastActivityTimestamp;
	private long lastInactivityTimestamp;

	/**
	 * 0 = North, 180 = South
	 */
	float lastOrientation = -1;

	private SensorManager sensorManager;

	private float[] gravity;

	private float[] magneticField;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(new GameView(this));
	}

	@Override
	protected void onResume() {
		super.onResume();

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause() {
		super.onPause();

		sensorManager.unregisterListener(this,
				sensorManager.getDefaultSensor((Sensor.TYPE_ACCELEROMETER)));
		sensorManager.unregisterListener(this,
				sensorManager.getDefaultSensor((Sensor.TYPE_MAGNETIC_FIELD)));
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

	private void onCompassEvent(SensorEvent event) {
		this.magneticField = event.values.clone();

		if (gravity != null && magneticField != null) {
			float[] inR = new float[16];
			float[] I = new float[16];
			float[] orientVals = new float[3];
			final float pi = (float) Math.PI;
			final float rad2deg = 180 / pi;

			boolean success = SensorManager.getRotationMatrix(inR, I, gravity,
					magneticField);

			if (success) {
				SensorManager.getOrientation(inR, orientVals);
				float azimuth = orientVals[0] * rad2deg;
				float orientation = azimuth;
				lastOrientation = lowPass(orientation, lastOrientation);
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
	static final float ALPHA = 0.08f;

	protected float lowPass(float input, float output) {

		float diff = input - output;
		diff = (diff + 180f + 360f) % 360f - 180f;
		diff = Math.abs(diff);

		if (input * output < 0f && diff < 180f
				&& Math.abs(input - output) > 180f) {

			if (output < 0f) {
				output += 360f;
			} else {
				output -= 360f;
			}
		}

		output = output + ALPHA * (input - output);
		return output;
	}

	enum Movement {
		NONE, MOVING
	}

	private void onAcceleratorEvent(SensorEvent event) {
		float LEG_THRSHOLD_AMPLITUDE = 2;
		long activityLag = 30;
		long inactivityLag = 300;
		long now = System.currentTimeMillis();

		final float z = event.values[2];
		final float zDiff = Math.abs(z - mLastZ);
		mLastZ = z;

		if (zDiff > LEG_THRSHOLD_AMPLITUDE) {
			lastActivityTimestamp = now;

			if (lastInactivityTimestamp + activityLag < now) {
				if (lastActivity != Movement.MOVING) {
					lastActivity = Movement.MOVING;
				}
			}
		} else {
			lastInactivityTimestamp = now;
			if (lastActivityTimestamp + inactivityLag < now) {
				if (lastActivity != Movement.NONE) {
					lastActivity = Movement.NONE;
				}
			}
		}
	}

	public void vibrate() {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		int dot = 300;
		int gap = 2000;
		long[] pattern = { 0, dot, gap, dot, gap };

		v.vibrate(pattern, -1);
	}
}
