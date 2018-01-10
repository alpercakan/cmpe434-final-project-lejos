package com.alpercakan.cmpe434;

import lejos.hardware.Button;
import lejos.hardware.Sound;

public class Utils {
	public static void sleep(long x) {
		try { 
			Thread.sleep(x); 
		} catch (Exception e) {}
	}
	
	public static void errorExit() {
		// TODO FileKeeper.dumpMap();
		System.out.println("«˝kmak iÁin bir tu˛a bas˝n.");
		Button.waitForAnyPress();
		System.exit(0);
	}
	
	/**
	 * [1, 2, 3, 4, 5]; shift = 1 ==>>> [5, 1, 2, 3, 4]
	 * @param arr
	 * @param shiftAmount
	 * @return
	 */
	public static int[] shift(int []arr, int shiftAmount) {
	    	int []ret = new int[arr.length];
	    	
	    	for (int i = 0; i < arr.length; ++i) {
	    		ret[(i + arr.length + shiftAmount) % arr.length] = arr[i];
	    	}
	    	
	    	return ret;
    }
	
	public static float[] shift(float []arr, int shiftAmount) {
		float []ret = new float[arr.length];
	    	
    	for (int i = 0; i < arr.length; ++i) {
    		ret[(i + arr.length + shiftAmount) % arr.length] = arr[i];
    	}
    	
    	return ret;
	}
	
	public static void tripleBeep() {
		Sound.beep();
		Utils.sleep(500);
		Sound.beep();
		Utils.sleep(500);
		Sound.beep();
	}
}
