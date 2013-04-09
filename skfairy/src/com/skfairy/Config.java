package com.skfairy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class Config extends Activity {
	private Intent sks;
	// private ShakeDetector mShakeDetector;

	// private boolean debugEnabled = false;
	private TextView info;
	private boolean debugEnabled = false;

	private BroadcastReceiver mybcr = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Constants.SHAKE_ACTION.equalsIgnoreCase(action)) {
				if (debugEnabled) {
					SkLog.d("Got an action broadcast");
					Bundle extras = intent.getExtras();
					if (extras != null) {
						String value = extras.getString(Constants.SHAKE_EXTRA_VALUE);
						info.append(value);
					}
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SkLog.d("Config onCreate calling...");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		// Toast.makeText(Config.this, "Showing...", Toast.LENGTH_SHORT).show();
		info = (TextView) findViewById(R.id.info_text);

		setDelay();
		setNoiseThreshold();
		setDebug();

		sks = new Intent(this, SkService.class);

		final Button btnStartService = (Button) findViewById(R.id.btnStartService);
		final Button btnStopService = (Button) findViewById(R.id.btnStopService);

		OnClickListener ssLisn = new OnClickListener() {
			public void onClick(View v) {
				// info.setText("Start Service");
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
		registerReceiver(mybcr, new IntentFilter(Constants.SHAKE_ACTION));

	}

	private void setDelay() {
		SeekBar seekbar = (SeekBar) findViewById(R.id.skbDelay);
		seekbar.setProgress((int) ShakeDetector.getDelay());

		final TextView txtDelay = (TextView) findViewById(R.id.txtDelay);
		txtDelay.setText(String.valueOf(ShakeDetector.getDelay()));

		OnSeekBarChangeListener sbCl = new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				ShakeDetector.setDelay((long) arg1);
				txtDelay.setText(String.valueOf(arg1));
				// Toast.makeText(Config.this, "Value=" + arg1,
				// Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

		};

		seekbar.setOnSeekBarChangeListener(sbCl);
	}

	private void setNoiseThreshold() {
		SeekBar seekbar = (SeekBar) findViewById(R.id.skbNoiseThreshold);
		seekbar.setProgress(ShakeDetector.getShakeThreshold());

		final TextView txtNT = (TextView) findViewById(R.id.txtNoiseThreshold);
		txtNT.setText(String.valueOf(ShakeDetector.getShakeThreshold()));

		OnSeekBarChangeListener sbCl = new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				ShakeDetector.setShakeThreshold((long) arg1);
				txtNT.setText(String.valueOf(arg1));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

		};

		seekbar.setOnSeekBarChangeListener(sbCl);
	}

	private void setDebug() {
		Switch debugSwc = (Switch) findViewById(R.id.swcDebug);
		debugSwc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				info.setText("");
				if (isChecked) {
					debugEnabled = true;
					info.setVisibility(View.VISIBLE);
				} else {
					debugEnabled = false;
					info.setVisibility(View.GONE);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.config, menu);
		return true;
	}

	@Override
	protected void onResume() {
		SkLog.d("Config onResume calling...");
		registerReceiver(mybcr, new IntentFilter(Constants.SHAKE_ACTION));
		super.onResume();
	}

	@Override
	protected void onPause() {
		SkLog.d("Config onPause calling...");
		unregisterReceiver(mybcr);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		SkLog.d("Config onDestroy calling...");
		stopService(sks);
		super.onDestroy();
	}

}
