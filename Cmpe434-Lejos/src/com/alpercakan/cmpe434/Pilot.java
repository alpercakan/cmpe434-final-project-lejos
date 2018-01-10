package com.alpercakan.cmpe434;

import java.io.IOException;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.PilotProps;

public class Pilot {
	public static MovePilot pilot;
	
	public static final float MARGINAL_STEP = 29; // centimeters
	
	/**
	 * The marginal step length as measured by the odometry.
	 */
	public static final float PHYSICAL_MARGINAL_STEP = 32;//31;//29;//30; // centimeters
	public static final float STOP_DIST = 16; // centimeters
	
	/**
	 * The maximum degrees which is accepted for turning errors.
	 */
	public static final float ATOMIC_TURN_DEGREE = 1.5f;
	
	static final float TURN_INTENTIONAL_OVERSHOOT_MULT = 1.3f;
	
	/**
	 * The amount traveled in the last full, safe step
	 */
	public static float lastStep;
	
	/**
	 * Current heading of the robot
	 */
	static int currentHeading = MapUtils.HEADING_NORTH;
	
	static int currentX = MapUtils.START_COOR,
			   currentY = MapUtils.START_COOR;
	
	static IntPoint getCurrentCoor() {
		return new IntPoint(currentX, currentY);
	}
	
	static void goAdj(int dir) {
		optimizedTurn((-90.0f) * ((dir - currentHeading + 8) % 4));
		safeStep();
	}
	
	/**
	 * @return Traveled distance
	 */
	public static float safeStep() {
		currentX = MapUtils.getAdvanceX(currentX, currentHeading);
		currentY = MapUtils.getAdvanceY(currentY, currentHeading);
		
		lastStep = safeTravelWithDist(PHYSICAL_MARGINAL_STEP);
		
		Uplink.sendXY(currentX, currentY);
		
		return lastStep;
	}
	
	public static void stepBack(float dist) {
		currentX = MapUtils.getAdvanceX(currentX, MapUtils.turnCount(currentHeading, 2));
		currentY = MapUtils.getAdvanceY(currentY, MapUtils.turnCount(currentHeading, 2));
		pilot.travel(-dist);
		
		Uplink.sendXY(currentX, currentY);
	}
	
	/**
	 * @return Traveled distance
	 */
	public static float safeTravelWithDist(float dist) {
		pilot.travel(dist * 2, true);
		float traveled;
		
		while ((traveled = pilot.getMovement().getDistanceTraveled()) < dist &&
			   Ultrasonic.getDist() > STOP_DIST)
			Utils.sleep(20);
		
		pilot.stop();
		
		float deltaFix = 0, minDiff = 6000, gyro = Gyro.readGyro();
		
		for (int i = -4; i <= +4; ++i) {
			if (Math.abs(i * 90 - gyro) < minDiff) {
				minDiff = Math.abs(i * 90 - gyro);
				deltaFix = i * 90 - gyro;
			}
		}

		if (Math.abs(deltaFix + 360) < Math.abs(deltaFix))
			deltaFix += 360;
		if (Math.abs(deltaFix - 360) < Math.abs(deltaFix))
			deltaFix -= 360;
		
		if (Math.abs(deltaFix) > ATOMIC_TURN_DEGREE) // 3
			turn(deltaFix);

		return traveled;
	}
	
	public static void turn(float delta)
	{
		// Stop and reset everything
		pilot.stop();
		Gyro.reset();
	
		pilot.rotate(-delta * TURN_INTENTIONAL_OVERSHOOT_MULT, true);
		
		float gyro;
		
		while(Math.abs(delta - (gyro = Gyro.readGyro())) > ATOMIC_TURN_DEGREE) {
			Utils.sleep(10);
		}
		
		pilot.stop();
		
		if (Math.abs(delta - (gyro = Gyro.readGyro())) > ATOMIC_TURN_DEGREE) {
			pilot.rotate(delta - gyro);
		}
	}
	
	public static void optimizedTurn(float delta) {
		if (Math.abs(delta) < 45) {
			// TODO Error
			return;
		}
		
		currentHeading = MapUtils.turnCount(currentHeading, (( (int) delta + 720) % 360) / (-90));
		
		if (delta == -270)
			turn(90);
		else if (delta == +270)
			turn(-90);
		else
			turn(delta);
		
		Uplink.sendHeading(currentHeading);
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
			System.out.println("Dosya hatas� oldu, l�tfen tekrar deneyin");
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
		
		///* en son
		pilot.setLinearSpeed(5);
		pilot.setAngularSpeed(10);
		pilot.setLinearAcceleration(40);
		pilot.setAngularAcceleration(10);
		pilot.stop();
		
		/*
		pilot.setLinearSpeed(10);
		pilot.setAngularSpeed(5);
		pilot.setLinearAcceleration(40);
		pilot.setAngularAcceleration(10);
		pilot.stop();*/
	}
}
