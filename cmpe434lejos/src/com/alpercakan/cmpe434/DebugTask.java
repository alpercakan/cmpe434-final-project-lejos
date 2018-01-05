package com.alpercakan.cmpe434;

import lejos.hardware.Button;

public class DebugTask {

	public static void run() {
		while(true) {
			Pilot.safeStep();
			Pilot.safeStep();
		}
	}
}
