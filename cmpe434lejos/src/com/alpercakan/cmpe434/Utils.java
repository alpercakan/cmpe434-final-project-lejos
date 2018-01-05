package com.alpercakan.cmpe434;

import lejos.hardware.Button;

public class Utils {
	public static void sleep(long x) {
		try { 
			Thread.sleep(x); 
		} catch (Exception e) {}
	}
	
	public static void errorExit() {
		System.out.println("Çıkmak için bir tuşa basın.");
		Button.waitForAnyPress();
		System.exit(0);
	}
}
