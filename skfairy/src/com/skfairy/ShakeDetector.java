package com.skfairy;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {

	private static long DELAY = 50;

	private static double SHAKE_SHRESHOLD = 2500d;
	private long lastTime = System.currentTimeMillis();
	private float last_x;
	private float last_y;
	private float last_z;

	private SensorManager sensorManager;
	private ShakeListener shakeListener;

	public ShakeDetector(Context context) {
		SkLog.d("get SensorManager Service...");
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		SkLog.d("got SensorManager Service");
	}

	public boolean registerListener() {
		if (sensorManager != null) {
			Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			if (sensor != null) {
				this.sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
				SkLog.d("Register SensorManager Listener: succeed!!, DELAY=" + DELAY + ",SHAKE_SHRESHOLD=" + SHAKE_SHRESHOLD
						+ ",sensor=" + sensor.getResolution());
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

	public void setOnShakeListener(ShakeListener listener) {
		shakeListener = listener;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
			return;
		}
		long curTime = System.currentTimeMillis();
		if (curTime - lastTime > DELAY) {
			long diffTime = (curTime - lastTime);
			// SkLog.d("onSensorChanged:" + Arrays.toString(event.values));
			lastTime = curTime;
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
			if (speed > SHAKE_SHRESHOLD) {
				float deltaX = Math.abs(x - last_x);
				float deltaY = Math.abs(y - last_y);
				// if (count++ > 2) {
				// count = 0;
				shakeListener.onShake("Shook :\ndiffTime=" + diffTime + ",speed=" + forat(speed) + ". x=" + forat(x) + ", y="
						+ forat(y) + ",z=" + forat(z) + ",dX=" + forat(deltaX) + ",dY=" + forat(deltaY) + ",Dirc="
						+ getDirection(deltaX, deltaY) + ",accuracy" + event.accuracy + "\n");
				// }

			}
			last_x = x;
			last_y = y;
			last_z = z;
		}
	}

	private String forat(float f) {
		NumberFormat nf = new DecimalFormat("#.##");
		return nf.format(f);
	}

	private String getDirection(float deltaX, float deltaY) {
		String r = "Unknown";
		if (deltaX > deltaY) {
			r = "Left to Right";
		} else if (deltaY > deltaX) {
			r = "Up to Down";
		}
		return r;
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
	public interface ShakeListener {
		public void onShake(String value);
	}

	public static void setDelay(long delay) {
		ShakeDetector.DELAY = delay;
	}

	public static long getDelay() {
		return ShakeDetector.DELAY;
	}

	public static void setShakeThreshold(double threshold) {
		ShakeDetector.SHAKE_SHRESHOLD = threshold * 100;
	}

	public static int getShakeThreshold() {
		return (int) ShakeDetector.SHAKE_SHRESHOLD / 100;
	}

}