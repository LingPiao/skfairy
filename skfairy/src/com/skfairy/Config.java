package com.skfairy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Config extends Activity {
	private Intent sks;
	private TextView speedNote;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SkLog.d("Config onCreate calling...");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		// Toast.makeText(Config.this, "Showing...", Toast.LENGTH_SHORT).show();

		speedNote = (TextView) findViewById(R.id.lblSpeedThresholdNote);

		setSpeedThreshold();

		sks = new Intent(this, SkService.class);

		final Button btnStartService = (Button) findViewById(R.id.btnStartService);
		final Button btnStopService = (Button) findViewById(R.id.btnStopService);

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
		stopService(sks);
		super.onDestroy();
	}

}
