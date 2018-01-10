package com.alpercakan.cmpe434;

import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;

class RadarData {
	// front, right, back, left
	float dists[] = new float[4];
}

public class Radar {
	 
	static NXTRegulatedMotor radarMotor;

	public static void init() {
		radarMotor = new NXTRegulatedMotor(MotorPort.B);
		radarMotor.resetTachoCount();
	
		taskReset();
	}
	
	public static void taskReset() {
		radarMotor.rotateTo(0);
		radarMotor.resetTachoCount();
	}
	
	public static RadarData scanAll() {
		RadarData data = new RadarData();
		
		data.dists[0] = Ultrasonic.getDist();
		data.dists[2] = Ultrasonic.getNxtDist();
		
		radarMotor.rotateTo(90);
		Utils.sleep(100);
		
		data.dists[1] = Ultrasonic.getDist();
		data.dists[3] = Ultrasonic.getNxtDist();
		
		radarMotor.rotateTo(0);
		
		return data;
	}
}
