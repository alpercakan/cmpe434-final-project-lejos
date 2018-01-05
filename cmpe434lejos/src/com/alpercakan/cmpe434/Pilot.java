package com.alpercakan.cmpe434;

import java.io.IOException;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.PilotProps;

public class Pilot {
	public static MovePilot pilot;
	
	public static final int MARGINAL_STEP = 29; // cm
	public static final float STOP_DIST = 16; // cm
	
	public static float lastStep;
	
	/**
	 * @return Travelled distance
	 */
	public static float safeStep() {
		pilot.travel(MARGINAL_STEP * 2, true);
		float travelled;
		
		while ((travelled = pilot.getMovement().getDistanceTraveled()) < MARGINAL_STEP && Ultrasonic.getDist() > STOP_DIST)
			Utils.sleep(20);
		
		pilot.stop();
		
		return (lastStep = travelled);
	}
	
	public static void turnTo(float targetAngle) {
		/*float angle = getAngle();
		
		while (true) {
			pilot.stop();
			if (angle > targetAngle)
				pilot.rotate(1000, true);
			else
				pilot.rotate(-1000, true);
			
			while (Math.abs(getAngle() - targetAngle) > 1) {
				ev3.getGraphicsLCD().clear();
				System.out.println(getAngle());
				sleep(20);
			}
			
		
			if (Math.abs(getAngle() - targetAngle) <= 1) {
				pilot.stop();
				return;
			}
		}*/
		
//		pilot.rotate(targetAngle);
		turn(targetAngle - Gyro.readGyro()); // TODO
	}
	
	public static void turn(float delta)
	{
		pilot.stop();
		Gyro.reset();
	
		pilot.rotate(-delta * 1.3, true);
		
		float gyro;
		while(Math.abs(delta - (gyro = Gyro.readGyro())) > 1.5f) {
			Utils.sleep(10);
		}
		
		pilot.stop();
		
		if (Math.abs(delta - (gyro = Gyro.readGyro())) > 1.5f) {
			turn(delta - Gyro.readGyro());
		}
	}
	
	public static void optimizedTurn(float delta) {
		if (delta == -270)
			turn(90);
		else if (delta == +270)
			turn(-90);
		else
			turn(delta);
	}
	
	
	public static void init() {
		PilotProps pilotProps = new PilotProps();
		pilotProps.setProperty(PilotProps.KEY_WHEELDIAMETER, "4.96");
		pilotProps.setProperty(PilotProps.KEY_TRACKWIDTH, "13.0");
		pilotProps.setProperty(PilotProps.KEY_LEFTMOTOR, "D");
		pilotProps.setProperty(PilotProps.KEY_RIGHTMOTOR, "A");
		pilotProps.setProperty(PilotProps.KEY_REVERSE, "false");
		try {
			pilotProps.storePersistentValues();
			pilotProps.loadPersistentValues();
		} catch (IOException e) {
			System.out.println("Dosya hatası oldu, lütfen tekrar deneyin");
			Utils.errorExit();
		}

		EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(MotorPort.D);
		EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(MotorPort.A);

		float wheelDiameter = Float.parseFloat(pilotProps.getProperty(PilotProps.KEY_WHEELDIAMETER, "4.96"));
		float trackWidth = Float.parseFloat(pilotProps.getProperty(PilotProps.KEY_TRACKWIDTH, "13.0"));
		boolean reverse = Boolean.parseBoolean(pilotProps.getProperty(PilotProps.KEY_REVERSE, "false"));

		Chassis chassis = new WheeledChassis(
				new Wheel[] { 
						WheeledChassis.modelWheel(leftMotor, 4.93 /* wheeldiameter TODO */).offset(-trackWidth/2).invert(reverse),
						WheeledChassis.modelWheel(rightMotor,wheelDiameter).offset(trackWidth/2).invert(reverse)},
				WheeledChassis.TYPE_DIFFERENTIAL);
		
		pilot = new MovePilot(chassis);
		taskReset();
	}
	
	public static void taskReset() {
		/*pilot.setLinearSpeed(5);
		pilot.setAngularSpeed(5);
		pilot.setLinearAcceleration(20);
		pilot.setAngularAcceleration(5);*/
		
		pilot.setLinearSpeed(5);
		pilot.setAngularSpeed(10);
		pilot.setLinearAcceleration(40);
		pilot.setAngularAcceleration(10);
		
		pilot.stop();
	}
}
