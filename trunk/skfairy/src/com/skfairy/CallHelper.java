package com.skfairy;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import com.android.internal.telephony.ITelephony;

/**
 * 
 * @author Kong,LingPiao
 * 
 */
public class CallHelper {

	public static void silenceCall(Context context) {
		// Make sure the phone is still ringing
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
			return;
		}
		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		am.setRingerMode(AudioManager.RINGER_MODE_SILENT); // For Silent mode
		// am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);// For Normal mode
		// am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);// For Vibrate
		// mode
	}

	public static void setRingModeBack(Context context, int mode) {
		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		am.setRingerMode(mode);
	}

	public static int getRingMode(Context context) {
		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		return am.getRingerMode();
	}

	public static void answerCall(Context context) {
		// Make sure the phone is still ringing
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
			return;
		}

		// Answer the phone
		answerPhoneHeadsethook(context);
		// try {
		// answerPhoneAidl(context);
		// } catch (Exception e) {
		// SkLog.d("Error trying to answer using telephony service.  Falling back to headset.Exception:"
		// + e.getMessage());
		// answerPhoneHeadsethook(context);
		// }
	}

	private static void answerPhoneHeadsethook(Context context) {
		SkLog.d("CallHelper.answerPhoneHeadsethook()...");
		// Simulate a press of the headset button to pick up the call
		Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");

		// froyo and beyond trigger on buttonUp instead of buttonDown
		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
		context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
		SkLog.d("CallHelper.answerPhoneHeadsethook() end");
	}

	@SuppressWarnings("unchecked")
	public static void answerPhoneAidl(Context context) throws Exception {
		SkLog.d("CallHelper.answerPhoneAidl()...");
		// Set up communication with the telephony service
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		@SuppressWarnings("rawtypes")
		Class c = Class.forName(tm.getClass().getName());
		Method m = c.getDeclaredMethod("getITelephony");
		m.setAccessible(true);
		ITelephony telephonyService = (ITelephony) m.invoke(tm);

		// Silence the ringer and answer the call!
		telephonyService.silenceRinger();
		telephonyService.answerRingingCall();
		SkLog.d("CallHelper.answerPhoneAidl() end");
	}
}
