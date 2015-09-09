package com.skfairy;

public enum Switch {

	WIFI(0), AIRPLANE(2), GPRS(4), LOCK(6), MODEL(8), WEATHER(9), SWTICH_CITY(10), REBOOT(11);

	private int value;

	Switch(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
