package com.alpercakan.cmpe434;

import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;

public class MainControl {
	static boolean initted = false;
	
	
	public static void init() {
		if (initted) {
			Gripper.taskReset();
			Gyro.taskReset();
			Pilot.taskReset();
			Radar.taskReset();
			Ultrasonic.taskReset();
			Uplink.cleanup();
			Uplink.init();
		} else {
			Ultrasonic.init();
			ColorReader.init();
			Gyro.init();
			Gripper.init();
			Pilot.init();
			Radar.init();	
		}
		
		initted = true;
	}
	
	/*
	public static void taskReset() {
		Ultrasonic.taskReset();
		ColorReader.taskReset();
		Gyro.taskReset();
		Gripper.taskReset();
		Pilot.taskReset();
		Radar.taskReset();
		
		Uplink.cleanup();
		Uplink.init();
	}*/
	
	public static void idle() {
		Pilot.pilot.stop();
		Gripper.gripperMotor.stop();
		Gripper.letGo();
		Radar.radarMotor.stop();
		Uplink.cleanup();
	}
	
	// Mapping: 0, Exec: 1
	public static int currentTask = -1;
	public static Thread runningThread;
	
	public static void logicLoop() {
		/*int buttonId;
		
		while (true) {
			buttonId = Button.waitForAnyPress();
			
			switch (buttonId) {
			case Button.ID_UP: // Mapping
				init();
				MappingTask.run();
				break;
				
			case Button.ID_DOWN: // Execution
				init();
				TaskExecution.run();
				break;
				
			case Button.ID_ENTER: // Debug
				init();
				DebugTask.run();
			}
		}*/
		
		final Thread keyListener = new Thread(new Runnable() {
			public void run() {
				while (true) {
					int button = Button.waitForAnyPress();
					switch (button) {
					case Button.ID_UP: // Mapping
						if (runningThread == null) {
							init();
							currentTask = 0;
							runningThread = new Thread(new Runnable() {
								public void run() {
									try {
										MappingTask.run();
									} catch (Error e) {
										idle();
									}
								}
							});	
							
							runningThread.start();
						}
						break;
						
					case Button.ID_DOWN: // Execution
						if (runningThread == null) {
							init();
							currentTask = 1;
							runningThread = new Thread(new Runnable() {
								public void run() {
									try {
										TaskExecution.run();
									} catch (Error e) {
										idle();
									}	
								}
							});	
							System.out.println("hi");
							runningThread.start();
						}
						break;
						
					case Button.ID_ESCAPE: // Reset current
						if (runningThread != null) {
							runningThread.stop();
							runningThread = null;
						}
						
						if (currentTask != -1) {
							init();
							
							if (currentTask == 0) {
								if (runningThread == null) {
									init();
									currentTask = 0;
									runningThread = new Thread(new Runnable() {
										public void run() {
											try {
												MappingTask.run();
											} catch (Error e) {
												idle();
											}
										}
									});	
									
									runningThread.start();
								}
							} else if (currentTask == 1) {
								if (runningThread == null) {
									init();
									currentTask = 1;
									runningThread = new Thread(new Runnable() {
										public void run() {
											try {
												TaskExecution.run();
											} catch (Error e) {
												idle();
											}	
										}
									});	
									
									runningThread.start();
								}
							}	
						}
						break;
						
					case Button.ID_ENTER: // Idle
						if (runningThread != null) {
							runningThread.stop();
							runningThread = null;
						}
						break;
					}	
				}
			}
		});
		
		/*Button.ENTER.addKeyListener(keyListener);
		Button.UP.addKeyListener(keyListener);
		Button.DOWN.addKeyListener(keyListener);
		Button.ESCAPE.addKeyListener(keyListener);*/
		
		keyListener.start();
		
		while (true) {
			try {
				Thread.sleep(-1);
			} catch (Exception e) { }
		}
	}
	
	public static void main(String[] args) {
		Uplink.init();
		
		logicLoop();
		
		Uplink.cleanup();
	}
}
