package org.ggj2013;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements SensorEventListener {

	private float mLastZ;

	private Movement lastActivity;

	private long mLastTimestamp;

	private long lastActivityTimestamp;

	/**
	 * 0 = North, 180 = South
	 */
	private int dLastOrientation = -1;

	private long dLastTimestamp;

	private SensorManager sensorManager;

	private float[] gravity;

	private float[] magneticField;

	private Game game;

	SoundManager soundManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(new GameView(this));

		game = new Game();

		soundManager = new SoundManager(getApplicationContext());
		soundManager.loadSoundPack(new SoundPackStandard());
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
		long now = event.timestamp;

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

				// 0-350�
				orientation = (orientation + 180);
				if (orientation == 360) {
					orientation = 0;
				}

				int diff = orientation - dLastOrientation;
				diff = (diff + 180 + 360) % 360 - 180;
				diff = Math.abs(diff);

				float factor = (now - dLastTimestamp) / 100000000f;

				if (dLastOrientation < 0 || (diff < (45 * factor)) && diff > 0) {
					dLastOrientation = orientation;
					Log.e("COMPASS", Integer.toString(dLastOrientation));
				}
			}
		}
		dLastTimestamp = now;
	}

	enum Movement {
		NONE, MOVING
	}

	private void onAcceleratorEvent(SensorEvent event) {
		long now = event.timestamp;
		float factor = (now - mLastTimestamp) / 10000000f;
		float LEG_THRSHOLD_AMPLITUDE = 5 / factor;

		final float z = event.values[2];

		if (Math.abs(z - mLastZ) > LEG_THRSHOLD_AMPLITUDE) {
			lastActivityTimestamp = System.currentTimeMillis();

			if (lastActivity != Movement.MOVING) {
				lastActivity = Movement.MOVING;

				soundManager
						.play(SoundPackStandard.CAT_MEOW,
								SoundManager.BALANCE_FULL_LEFT,
								SoundManager.VOLUME_100);

				Log.e("MOVING", "WALKING");
			}
		} else {
			if (lastActivityTimestamp + 500 < System.currentTimeMillis()) {
				if (lastActivity != Movement.NONE) {
					lastActivity = Movement.NONE;
					Log.e("MOVEMENT", "STOPPED");
				}
			}
		}
		mLastZ = z;
		mLastTimestamp = now;
	}
}
