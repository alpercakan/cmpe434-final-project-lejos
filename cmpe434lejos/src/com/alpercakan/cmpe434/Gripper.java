package com.alpercakan.cmpe434;

import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;

public class Gripper {
	public static EV3MediumRegulatedMotor gripperMotor;
	
	public static final int GRIP_ROTATION_DEGREES = 650,
						    GRIP_MOTOR_SPEED = 720;
	
	public static void grip() {
		gripperMotor.rotate(GRIP_ROTATION_DEGREES);
	}
			
	public static void letGo() {
		gripperMotor.rotate(-GRIP_ROTATION_DEGREES);
	}
	
	public static void init() {
		gripperMotor = new EV3MediumRegulatedMotor(MotorPort.C);
	}
	
	public static void taskReset() {
		gripperMotor.setSpeed(GRIP_MOTOR_SPEED);
	}
}
