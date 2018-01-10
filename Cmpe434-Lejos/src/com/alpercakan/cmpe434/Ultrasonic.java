package com.alpercakan.cmpe434;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.NXTUltrasonicSensor;
import lejos.robotics.SampleProvider;

public class Ultrasonic {
	public static EV3UltrasonicSensor ultrasonicSensor;
	public static NXTUltrasonicSensor nxtUltrasonicSensor;
	public static SampleProvider usSampleProvider, nxtSampleProvider;
	
	public static float getDist() {
		while (usSampleProvider.sampleSize() <= 0)
			Thread.yield();
		
	    float [] sample = new float[usSampleProvider.sampleSize()];
	    usSampleProvider.fetchSample(sample, 0);
	    
	    return 100 * sample[0];    
	}
	
	public static float getNxtDist() {
		while (nxtSampleProvider.sampleSize() <= 0)
			Thread.yield();
		
	    float [] sample = new float[nxtSampleProvider.sampleSize()];
	    nxtSampleProvider.fetchSample(sample, 0);
	    
	    return 100 * sample[0];    
	}
	
	public static void init() {
		ultrasonicSensor = new EV3UltrasonicSensor(SensorPort.S3);
		usSampleProvider = ultrasonicSensor.getDistanceMode();
		
		nxtUltrasonicSensor = new NXTUltrasonicSensor(SensorPort.S4);
		nxtSampleProvider = nxtUltrasonicSensor.getDistanceMode();
	}
	
	public static void taskReset() {
		
	}
 }
