package com.alpercakan.cmpe434;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.ev3.EV3;
import lejos.robotics.Color;

public class FinalProjectLejos {
	
	static EV3 ev3 = (EV3) BrickFinder.getDefault();

	public static void init() {
		Uplink.init();
		
		debugInit();
	}
	
	public static void debugInit() {
		Ultrasonic.init();
		ColorReader.init();
		Gyro.init();
		Gripper.init();
		Pilot.init();
		Radar.init();
	}
	
	public static void taskReset() {
		Gripper.taskReset();
		Gyro.taskReset();
		Ultrasonic.taskReset();
		ColorReader.taskReset();
		Pilot.taskReset();
		Radar.taskReset();
	}
	
	public static void logicLoop() {
		int buttonId;
		
		while (true) {
			buttonId = Button.waitForAnyPress();
			
			if (buttonId == Button.ID_UP) {
				init();
				MappingTask.run();
				break;
			} else if (buttonId == Button.ID_DOWN) {
				TaskExecution.run();
				break;
			} else if (buttonId == Button.ID_ENTER) {
				debugInit();
				DebugTask.run();
			}
		}
	}
	
	public static void main(String[] args) {
		logicLoop();
		
		Uplink.cleanup();
	}
	
	
	
	// OLD CODES
	 
	/*

	static EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S2);


	public static void main(String[] args) {

		EV3 ev3 = (EV3) BrickFinder.getDefault();
		GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();

		graphicsLCD.clear();
		graphicsLCD.drawString("Ultrasonic Sensor", graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);

		SampleProvider sampleProvider2 = gyroSensor.getAngleAndRateMode();

	    SampleProvider sampleProvider = ultrasonicSensor.getDistanceMode();

		while (Button.readButtons() != Button.ID_ESCAPE) {
			if(sampleProvider.sampleSize() > 0) {
		    	float [] sample = new float[sampleProvider.sampleSize()];
		    	sampleProvider.fetchSample(sample, 0);

		    	float [] sample2 = new float[sampleProvider2.sampleSize()];
		    	sampleProvider.fetchSample(sample2, 0);
		    	float angle = sample2[0];


		    	float distance = sample[0];

		    	graphicsLCD.clear();
				graphicsLCD.drawString("u" + distance, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);
				graphicsLCD.drawString("g"+ angle, graphicsLCD.getWidth()/2, graphicsLCD.getHeight()/2 + 20, GraphicsLCD.VCENTER|GraphicsLCD.HCENTER);

				try {
					Thread.sleep(10);
				} catch (Exception e) {}
			}

	    	Thread.yield();
		}
	}
	 */
	
	
	/* COLOR NAMES
	 * 
		
		String colorNames[] = {
				"UNKNOWN",
				"RED",
				"GREEN",
				"BLUE",
				"MAGENTA",
				"YELLOW",
				"ORANGE",
				"WHITE",
				"BLACK",
				"PINK",
				"GRAY",
				"LIGHT_GRAY",
				"DARK_GRAY",
				"CYAN",
				"BROWN"
		};
	
	 */
	/* COLOR	
	EV3ColorSensor
	colorSensor2 = new EV3ColorSensor(SensorPort.S1);
	SensorMode sampleProvider22 = colorSensor2.getColorIDMode();
	while (true) {
		Button.waitForAnyPress();
		String color = "";
		if (sampleProvider22.sampleSize() > 0)
		{
			float[] x = new float[sampleProvider22.sampleSize()];
			sampleProvider22.fetchSample(x, 0);
	
			System.out.println(colorNames[ (int) (x[0] + 1)]); 
		}
	}
*/
	/*
	while(true)
	{
		int x = Button.waitForAnyPress();
		if (x == Button.ID_UP) {
			grip();
		} else if (x==Button.ID_LEFT) {
			letGo();
		}
	}*/
	
	/*// ----- GYRO TEST---
	EV3LargeRegulatedMotor sensorMotor = new EV3LargeRegulatedMotor(MotorPort.C);
	sensorMotor.setSpeed(90);
	GraphicsLCD graphicsLCD = ev3.getGraphicsLCD();
	graphicsLCD.clear();
	
	while(true) {
		gyroSensor.reset();
		switch (Button.waitForAnyPress()) {
		case Button.ID_DOWN:
			sensorMotor.rotate(-92, true);
break;
		case Button.ID_UP:
			sensorMotor.rotate(92, true);
		}

		while (true) {
			System.out.println(getAngle());
			if (Button.waitForAnyPress(50) != 0) break;
		}
	}
	*/// ----- END OF GYRO TEST
	
}
