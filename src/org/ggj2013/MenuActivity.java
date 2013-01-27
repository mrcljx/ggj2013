package org.ggj2013;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MenuActivity extends Activity {
	private final SimpleDateFormat df = new SimpleDateFormat("mm:ss:SSS");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_menu);

		findViewById(R.id.btn_lvl1).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (MotionEvent.ACTION_UP == event.getAction()) {
					return runGame(1);
				}
				return false;
			}
		});
		findViewById(R.id.btn_lvl2).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (MotionEvent.ACTION_UP == event.getAction()) {
					return runGame(2);
				}
				return false;
			}
		});
		findViewById(R.id.btn_lvl3).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (MotionEvent.ACTION_UP == event.getAction()) {
					return runGame(3);
				}
				return false;
			}
		});
		findViewById(R.id.btn_lvl4).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (MotionEvent.ACTION_UP == event.getAction()) {
					return runGame(4);
				}
				return false;
			}
		});
		findViewById(R.id.btn_lvl5).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (MotionEvent.ACTION_UP == event.getAction()) {
					return runGame(5);
				}
				return false;
			}
		});
	}

	private boolean runGame(int level) {
		Intent intent = new Intent(MenuActivity.this, FullscreenActivity.class);
		intent.putExtra("level", level);
		startActivity(intent);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		setButtonText(R.id.btn_lvl1, 1);
		setButtonText(R.id.btn_lvl2, 2);
		setButtonText(R.id.btn_lvl3, 3);
		setButtonText(R.id.btn_lvl4, 4);
		setButtonText(R.id.btn_lvl5, 5);
	}

	public void setButtonText(int id, int level) {
		Button b = (Button) findViewById(id);
		b.setText("LEVEL " + level + "\n\r ("
				+ df.format(new Date(getHighscore(level))) + ")");
	}

	public long getHighscore(int level) {
		SharedPreferences prefs = this.getSharedPreferences("DarkPulse",
				Context.MODE_PRIVATE);
		return prefs
				.getLong("level-" + level, 999 + 59 * 1000 + 59 * 1000 * 60);
	}

	public void setHighscore(int level, long time) {
		SharedPreferences prefs = this.getSharedPreferences("DarkPulse",
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putLong("level-" + level, time);
		editor.commit();
	}
}
