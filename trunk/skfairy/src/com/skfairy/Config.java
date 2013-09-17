package com.skfairy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * 
 * @author Kong,LingPiao
 * 
 */
public class Config extends Activity {
	public static final String APP_CONFIG_KEY = "SK_FAIRY_CONF";
	public static final String CONFIG_AUTO_START = "CONFIG_AUTO_START";

	private SharedPreferences preferences = null;
	private Intent sks;
	private TextView speedNote;
	private Editor pEditor = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SkLog.d("Config onCreate calling...");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		// Toast.makeText(Config.this, "Showing...", Toast.LENGTH_SHORT).show();

		preferences = getSharedPreferences(APP_CONFIG_KEY, Context.MODE_PRIVATE);
		pEditor = preferences.edit();

		CheckBox autoStartBtn = (CheckBox) findViewById(R.id.isAutoStart);
		autoStartBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				pEditor.putBoolean(CONFIG_AUTO_START, isChecked);
				pEditor.commit();
			}
		});

		boolean isAutoStart = preferences.getBoolean(CONFIG_AUTO_START, false);

		autoStartBtn.setChecked(isAutoStart);

		speedNote = (TextView) findViewById(R.id.lblSpeedThresholdNote);

		setSpeedThreshold();

		sks = new Intent(this, SkService.class);

		final Button btnStartService = (Button) findViewById(R.id.btnStartService);
		final Button btnStopService = (Button) findViewById(R.id.btnStopService);

		if (startService(sks) != null) {
			SkLog.d("SkService is being started or already running");
			btnStartService.setEnabled(false);
			btnStopService.setEnabled(true);
		} else {
			btnStartService.setEnabled(true);
			SkLog.d("SkService is not started");
		}

		OnClickListener ssLisn = new OnClickListener() {
			public void onClick(View v) {
				SkLog.d("Start SkService...");
				startService(sks);
				SkLog.d("SkService started");
				btnStartService.setEnabled(false);
				btnStopService.setEnabled(true);
			}
		};

		btnStartService.setOnClickListener(ssLisn);

		OnClickListener stsLisn = new OnClickListener() {
			public void onClick(View v) {
				// info.setText("Stop Service");
				SkLog.d("Stop SkService...");
				stopService(sks);
				SkLog.d("SkService stoped");
				btnStartService.setEnabled(true);
				btnStopService.setEnabled(false);
			}
		};

		btnStopService.setOnClickListener(stsLisn);
	}

	private void setSpeedThresholdNote(int threshold) {
		if (threshold > 30) {
			speedNote.setText(this.getString(R.string.lblSpeedThresholdNote_l3));
		} else if (threshold > 10) {
			speedNote.setText(this.getString(R.string.lblSpeedThresholdNote_l2));
		} else {
			speedNote.setText(this.getString(R.string.lblSpeedThresholdNote_l1));
		}
	}

	private void setSpeedThreshold() {
		SeekBar seekbar = (SeekBar) findViewById(R.id.skbSpeedThreshold);
		seekbar.setProgress(ShakeDetector.getShakeThreshold());
		setSpeedThresholdNote(ShakeDetector.getShakeThreshold());
		final TextView txtNT = (TextView) findViewById(R.id.txtSpeedThreshold);
		txtNT.setText(String.valueOf(ShakeDetector.getShakeThreshold()));

		OnSeekBarChangeListener sbCl = new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				if (arg1 < 3) {
					arg1 = 3;
				}
				ShakeDetector.setShakeThreshold((long) arg1);
				setSpeedThresholdNote(arg1);
				txtNT.setText(String.valueOf(arg1));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		};
		seekbar.setOnSeekBarChangeListener(sbCl);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SkLog.d("Config onOptionsItemSelected,id=" + item.getItemId());
		int id = item.getItemId();
		if (id == R.id.action_about) {
			startActivity(new Intent(this, AboutActivity.class));
		} else {
			return super.onOptionsItemSelected(item);
		}

		return true;
	}

	@Override
	protected void onResume() {
		SkLog.d("Config onResume calling...");
		super.onResume();
	}

	@Override
	protected void onPause() {
		SkLog.d("Config onPause calling...");
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		SkLog.d("Config onDestroy calling...");
		// stopService(sks);
		super.onDestroy();
	}

}
