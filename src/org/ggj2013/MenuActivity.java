package org.ggj2013;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MenuActivity extends Activity {
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
				Toast.makeText(MenuActivity.this, "LVL1 TBD",
						Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		findViewById(R.id.btn_lvl2).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Toast.makeText(MenuActivity.this, "LVL2 TBD",
						Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		findViewById(R.id.btn_lvl3).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Intent myIntent = new Intent(MenuActivity.this,
						FullscreenActivity.class);
				startActivity(myIntent);
				return true;
			}
		});
		findViewById(R.id.btn_lvl4).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Toast.makeText(MenuActivity.this, "LVL4 TBD",
						Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		findViewById(R.id.btn_lvl5).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				Toast.makeText(MenuActivity.this, "LVL5 TBD",
						Toast.LENGTH_SHORT).show();
				return true;
			}
		});
	}
}
