package com.alpercakan.cmpe434;

import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;

public class Gyro {
	public static EV3GyroSensor gyroSensor;
	public static SampleProvider gyroSampleProvider;
	
	public static float readGyro() {
		while (gyroSampleProvider.sampleSize() <= 0)
			Thread.yield();

		float [] sampleArr = new float[gyroSampleProvider.sampleSize()];
		gyroSampleProvider.fetchSample(sampleArr, 0);
		float deg = (sampleArr[0]);
		
		return deg;
	}
	
    public static void init() {
		gyroSensor = new EV3GyroSensor(SensorPort.S2);
		reset();
		gyroSampleProvider = gyroSensor.getAngleAndRateMode();
    }
    
    public static void taskReset() {
		reset();
    }
    
    public static void reset() {
		gyroSensor.reset();
    }
}
