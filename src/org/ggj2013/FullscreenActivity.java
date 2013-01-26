package org.ggj2013;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements SensorEventListener {

	private float mLastZ;

	private Movement mLastActivity;

	private int mInactivityCount;

	private SensorManager sensorManager;

	private float[] gravity;

	private float[] magneticField;

	private Game game;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		setContentView(new GameView(this));

		game = new Game();
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
				int orientation = Math.round(azimuth / 10f) * 10;

				if (orientation == -180) {
					orientation = 180;
				}

				Log.e("COMPASS", Integer.toString(orientation));
			}
		}
	}

	enum Movement {
		NONE, MOVING
	}

	private void onAcceleratorEvent(SensorEvent event) {
		// TODO: Thresholds should be based on time diff (not on samples)
		float LEG_THRSHOLD_AMPLITUDE = 5;
		int LEG_THRSHOLD_INACTIVITY = 5;

		final float z = event.values[2];

		if (Math.abs(z - mLastZ) > LEG_THRSHOLD_AMPLITUDE) {
			mInactivityCount = 0;

			if (mLastActivity != Movement.MOVING) {
				mLastActivity = Movement.MOVING;
				Log.e("MOVING", "WALKING");
			}
		} else {
			if (mInactivityCount > LEG_THRSHOLD_INACTIVITY) {
				if (mLastActivity != Movement.NONE) {
					mLastActivity = Movement.NONE;
					Log.e("MOVEMENT", "STOPPED");
					mInactivityCount = 0;
				}
			} else if (mLastActivity != Movement.NONE) {
				mInactivityCount++;
			}
		}
		mLastZ = z;
	}
}
