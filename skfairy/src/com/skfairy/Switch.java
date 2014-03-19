package com.skfairy;

public enum Switch {

	WIFI(0), AIRPLANE(2), GPRS(4), LOCK(6), MODEL(8);

	private int value;

	private Switch(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
