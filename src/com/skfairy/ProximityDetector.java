package com.skfairy;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ProximityDetector implements SensorEventListener {

	// private static long DELAY = 50;

	// private long lastTime = System.currentTimeMillis();

	private SensorManager sensorManager;
	private ProximityListener proximityListener;
	private Sensor sensor;

	public ProximityDetector(Context context) {
		SkLog.d("get SensorManager Service...");
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		SkLog.d("got SensorManager Service");
	}

	public boolean registerListener() {
		if (sensorManager != null) {
			sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			if (sensor != null) {
				this.sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
				SkLog.d("Register Proximity SensorManager Listener: succeed!!");
				return true;
			}
		}
		return false;
	}

	public void unRegisterListener() {
		if (sensorManager != null) {
			sensorManager.unregisterListener(this);
			SkLog.d("SensorManager unregistered.");
		}
	}

	public void setOnProximityChanged(ProximityListener listener) {
		proximityListener = listener;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_PROXIMITY) {
			return;
		}
		proximityListener.onChanged(event.values[0], sensor.getMaximumRange());
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		SkLog.d("Sensor,Type=" + sensor.getType() + ",accuracy=" + accuracy + " onAccuracyChanged!!");
	}

	/**
	 * 
	 * @author Nono
	 * 
	 */
	public interface ProximityListener {
		public void onChanged(float x, float maxRange);
	}

}