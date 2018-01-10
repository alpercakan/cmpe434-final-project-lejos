package com.alpercakan.cmpe434;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;

public class Gripper {
	public static EV3MediumRegulatedMotor gripperMotor;
	
	public static boolean isGripped = false;
	
	public static final int GRIP_ROTATION_DEGREES = 1150, //650,
						    GRIP_MOTOR_SPEED = 720;
	
	public static void grip() {
		if (isGripped) {
			return;
		}
		
		gripperMotor.rotate(-GRIP_ROTATION_DEGREES);
		isGripped = true;
	}
			
	public static void letGo() {
		if (!isGripped) {
			return;
		}
		
		gripperMotor.rotate(GRIP_ROTATION_DEGREES);
		isGripped = false;
	}
	
	public static void init() {
		gripperMotor = new EV3MediumRegulatedMotor(MotorPort.C);
		
		taskReset();
	}
	
	public static void taskReset() {
		letGo();
		gripperMotor.setSpeed(GRIP_MOTOR_SPEED);
		gripperMotor.resetTachoCount();
	}
}
