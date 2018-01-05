package com.alpercakan.cmpe434;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

public class ColorReader {
	static EV3ColorSensor colorSensor;
	static SampleProvider usSampleProvider, colorSampleProvider, gyroSampleProvider;
	 
	public static void init() {
		colorSensor = new EV3ColorSensor(SensorPort.S1);
		colorSampleProvider = colorSensor.getColorIDMode();
	}
	
	public static int getColor() {
		while (colorSampleProvider.sampleSize() <= 0)
			Thread.yield();
		
		float[] x = new float[colorSampleProvider.sampleSize()];
		colorSampleProvider.fetchSample(x, 0);

		return (int) x[0];
    }
	
	public static void taskReset() {
		
	}
}
