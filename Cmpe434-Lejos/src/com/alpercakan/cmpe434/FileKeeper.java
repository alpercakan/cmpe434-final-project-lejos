package com.alpercakan.cmpe434;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import lejos.robotics.Color;

public class FileKeeper {
	public static final String MAP_FILE_NAME = "final_project_map.txt",
							   LOG_FILE_NAME = "final_project_log.txt";
	
	static PrintStream logStream = null;
	
	static void log(String logMsg) {
		if (logStream == null) {
			File f = new File(LOG_FILE_NAME);
		
			try {
				logStream = new PrintStream(f, "UTF-8");
			} catch (IOException e) {
				ErrorHelper.pushNewError(e);
				return;
			}
		}
		
		logStream.println(logMsg);
		logStream.flush();
	}

	/**
	 * Dumps the map created inside the MappingTask.
	 * Format is this: For every cell, first four lines are distances (north, east, south, west),
	 * the fifth line is the color and the sixth line is the isVisited variable.
	 * @return Success or failure.
	 */
	public static boolean dumpMap() {
		File f = new File(MAP_FILE_NAME);
		PrintStream out = null;
		
		try {
			out = new PrintStream(f, "UTF-8");
		} catch (IOException e) {
			ErrorHelper.pushNewError(e);
			return false;
		}
	
		for (int x = 0; x < MapUtils.ARR_SIZE; ++x)
		{
			for (int y = 0; y < MapUtils.ARR_SIZE; ++y) {
				if (MappingTask.cells[x][y] == null) {
					// Cell is null, just dump dummy data
					
					for (int i = 0; i < 4; ++i) {
						out.println(-1.0f);
					}
					
					out.println(Color.NONE);
					out.println(false);
				} else {
					// Cell is good
					
					for (int j = 0; j < 4; ++j) {
						out.println(MappingTask.cells[x][y].dists[j] + "");
					}

					out.println(MappingTask.cells[x][y].color + "");
					out.println(MappingTask.cells[x][y].isVisited + "");	
				}				
			}
		}
		
		out.flush();
		out.close();
		return true;
	}
	
	/**
	 * Reads the map from the file and writes it into the readMap variable inside the TaskExecution.
	 * @return Success or failure.
	 */
	public static boolean readMap() {
		Scanner sc = null;
		final String incorrectFormatMsg = "Incorrect map save file format";
		
		try {
			File f = new File(MAP_FILE_NAME);
			sc = new Scanner(f);
			
			for (int x = 0; x < MapUtils.ARR_SIZE; ++x)
			{
				for (int y = 0; y < MapUtils.ARR_SIZE; ++y) {
					for (int j = 0; j < 4; ++j) {
						if (TaskExecution.readMap[x][y] == null)
							TaskExecution.readMap[x][y] = new Cell();
						
						if (!sc.hasNextLine()) {
							throw new Exception(incorrectFormatMsg);
						}
						
						TaskExecution.readMap[x][y].x = x;
						TaskExecution.readMap[x][y].y = y;
						TaskExecution.readMap[x][y].dists[j] = Float.parseFloat(sc.nextLine());
					}
					
					if (!sc.hasNextLine()) {
						throw new Exception(incorrectFormatMsg);
					}
					
					TaskExecution.readMap[x][y].color = Integer.parseInt(sc.nextLine());
					
					if (!sc.hasNextLine()) {
						throw new Exception(incorrectFormatMsg);
					}
					
					TaskExecution.readMap[x][y].isVisited = Boolean.parseBoolean(sc.nextLine());
				}
			}
		} catch (Exception e) {
			if (sc != null)
				sc.close();
			
			ErrorHelper.pushNewError(e);
			return false;
		}
		
		if (sc != null)
			sc.close();
		
		return true;
	}
}
