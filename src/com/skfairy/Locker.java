package com.skfairy;

import android.content.Context;
import android.os.PowerManager;

@SuppressWarnings("deprecation")
public class Locker {

	private static PowerManager.WakeLock sCpuWakeLock;

	public static PowerManager.WakeLock createPartialWakeLock(Context context) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		return pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SKUnlock");
	}

	public static void acquireCpuWakeLock(Context context) {
		SkLog.d("Acquiring cpu wake lock");
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		sCpuWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.ON_AFTER_RELEASE, "SKUnlock");
		SkLog.d("sCpuWakeLock: isHeld=" + sCpuWakeLock.isHeld() + " before acquiring");
		sCpuWakeLock.acquire();
		SkLog.d("sCpuWakeLock: isHeld=" + sCpuWakeLock.isHeld() + " after acquired");
		SkLog.d("WakeLock acquired done");
	}

	public static void releaseWakeLock() {
		if (sCpuWakeLock == null)
			return;
		sCpuWakeLock.release();
		sCpuWakeLock = null;
		SkLog.d("WakeLock released");
	}
}
