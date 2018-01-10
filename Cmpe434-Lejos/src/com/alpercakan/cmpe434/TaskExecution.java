package com.alpercakan.cmpe434;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import lejos.hardware.Sound;
import lejos.robotics.Color;

// TODO Check match coherency

public class TaskExecution {
	/**
	 * The map read from the file, with the coordinate system of the file.
	 */
	static Cell [][]readMap = new Cell[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
	
	static boolean [][]isVisitedArr = new boolean[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
	static boolean [][]isVisitedArrCpy = new boolean[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
	static boolean [][]isGoable = new boolean[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
	static boolean [][]isGoableCpy = new boolean[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
	
	static int visitedCount = 0,
			   goableCount = 0,
			   visitedCountCpy = 0,
			   goableCountCpy = 0;

	/**
	 * Map, with the current coordinate system.
	 */
	static Cell [][]cells = new Cell[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
	static Cell [][]cellsCpy = new Cell[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
	
	static boolean isNotVisited(int x, int y) {
		return !isVisitedArr[x][y];
	}
	
	/**
	 * True if the robot is localized certainly.
	 */
	static boolean localizedAlready = false;
	
	/**
	 * If true, localization will not cause termination of DFS.
	 */
	static boolean noLocalizationFinishing = false;
	
	static boolean knowGreen = false, knowBlue = false;
	
	/**
	 * 
	 * @return True if expolaration should be finished immediately.
	 */
	static boolean checkAndFinish() {
		// TODO old version : goableCount == visitedCount
		return (goableCount <= visitedCount) || (localizedAlready && !noLocalizationFinishing);
	}
	
	/**
	 * The amount to be traveled to ensure the ball is definitely in the gripper. 
	 */
	static float GRIP_TRAVEL_DIST = 3;//Pilot.MARGINAL_STEP / 7.5f;
	
	static boolean hasWeapon = false,
				   hasKilledGiant = false;
	
	static void gripBall() {
		//float traveled = Pilot.safeTravelWithDist((float) GRIP_TRAVEL_DIST);
		
		Gripper.grip();
		Gripper.isGripped = true;
		
	//	Pilot.pilot.travel(-traveled);
		
		hasWeapon = true;
	}
	
	public static void killGiant() {
		hasKilledGiant = true;
		
		Sound.playTone(440, 1000);
		
		Pilot.optimizedTurn(180);
		
		Gripper.letGo();
		Pilot.pilot.setLinearSpeed(20);
		Pilot.pilot.travel(5);
		Pilot.pilot.travel(-5);
		Pilot.pilot.setLinearSpeed(5);
		Pilot.optimizedTurn(180);
		Gripper.grip();
	}
	
	public static void savePrince() {
		Utils.tripleBeep();
		
		Gripper.letGo();
		
		// Everything is over
		System.exit(0);
	}
	
	public static void killInUnknownMap() {
		
	}
	
	public static IntPoint rotateCW(IntPoint point, int amount) {
		if (amount == 0) {
			IntPoint orig = new IntPoint(point.x, point.y);
			return orig;
		}
		
		IntPoint once = new IntPoint(point.y, -point.x);
		
		if (amount == 1)
			return once;
		
		return rotateCW(once, amount - 1);
	}
	
	/**
	 * Checks if the two maps match.
	 * 
	 * They are assumed to be using the same coordinate system.
	 */
	public static boolean checkMatch(Cell[][] map1, Cell[][] map2) {
		int matchStrength = 0;
		
		for (int x = 0; x < MapUtils.ARR_SIZE; ++x) {
			for (int y = 0; y < MapUtils.ARR_SIZE; ++y) {
				if (map1[x][y] != null && map2[x][y] != null &&
					map1[x][y].isVisited && map2[x][y].isVisited &&
					(map1[x][y].color != map2[x][y].color ||
					 !Arrays.equals(MapUtils.discreteDistArr(map1[x][y].dists), MapUtils.discreteDistArr(map2[x][y].dists)))) {
					
					/* This cell is visited in both of the maps, but
					 * color or distances do not match
					 */
					
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Match the read map with the current map (which is in the "cells" variable)
	 * @param currentX Current x coordinate of the robot
	 * @param currentY Current y coordinate of the robot
	 * @return List of possible matching poses, with the element format (x, y, shift)
	 * where x and y are the coordinates of the cell from the read map which coincides with the
	 * cell (currentX, currentY) from the current map. Note that x and y are in the coordinate
	 * system of the file. 
	 */
	public static ArrayList tryMatch(int currentX, int currentY) {
		ArrayList possiblePoses = new ArrayList();
		
		for (int readX = 0; readX < MapUtils.ARR_SIZE; ++readX) {
			for (int readY = 0; readY < MapUtils.ARR_SIZE; ++readY) {
				for (int shiftAmount = 0; shiftAmount < 4; ++shiftAmount) {
					if (readMap[readX][readY] == null || !readMap[readX][readY].isVisited) {
						continue;
					}
					
					/*
					 * Try to fit the map read from the file to the current map by coinciding (readX, readY)
					 * cell of the file map to the (currentX, currentY) of the current map, with shiftAmount
					 */
					
					IntPoint transformed = rotateCW(new IntPoint(readX, readY), shiftAmount);
					int deltaX = currentX - transformed.x, deltaY = currentY - transformed.y;
					
					// The map read from the file, but with current coordinate system
					Cell[][] readInCurrentNotation = new Cell[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
					
					for (int x = 0; x < MapUtils.ARR_SIZE; ++x) {
						for (int y = 0; y < MapUtils.ARR_SIZE; ++y) {
							if (readMap[x][y] != null && readMap[x][y].isVisited) {
								IntPoint currTr = rotateCW(new IntPoint(x, y), shiftAmount);
								int xNew = currTr.x + deltaX, yNew = currTr.y + deltaY;
								
								if (xNew < MapUtils.ARR_SIZE &&
									yNew < MapUtils.ARR_SIZE &&
									xNew >= 0 && yNew >= 0) {	
									readInCurrentNotation[xNew][yNew] = new Cell();
									readInCurrentNotation[xNew][yNew].dists = Utils.shift(readMap[x][y].dists, shiftAmount);
									readInCurrentNotation[xNew][yNew].color = readMap[x][y].color;
									readInCurrentNotation[xNew][yNew].x = xNew;
									readInCurrentNotation[xNew][yNew].y = yNew;
									readInCurrentNotation[xNew][yNew].isVisited = true;
								}
							}
						}
					}
					
					if (checkMatch(readInCurrentNotation, cells)) {
						possiblePoses.add(new Pose(readX, readY, shiftAmount));
					}
				}
			}
		}

		Uplink.sendPossibleList(possiblePoses);
		return possiblePoses;
	}
	
	/**
	 * Integrates the read map into the current map
	 */
	static void localizeCertainly(int currentX, int currentY, int readX, int readY, int shiftAmount) {
		// TODO REMOVE
		Sound.beepSequence();
			
		// Save the current state
		cellsCpy = new Cell[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
		
		for (int x = 0; x < MapUtils.ARR_SIZE; ++x) {
			for (int y = 0; y < MapUtils.ARR_SIZE; ++y) {
				if (cells[x][y] == null) {
					cellsCpy[x][y] = null;
					continue;
				}
				
				cellsCpy[x][y] = new Cell();
				cellsCpy[x][y].copyFrom(cells[x][y]);
				
				/*rollback[x][y].x = x;
				rollback[x][y].y = y;
				rollback[x][y].dists = Arrays.copyOf(cells[x][y].dists, 4);
				rollback[x][y].color = cells[x][y].color;
				rollback[x][y].isVisited = true;*/
			}
		}
		
		localizedAlready = true;
		
		System.out.println(currentX + " " + currentY + " " + readX + " " + readY + " " + shiftAmount);
		
		IntPoint transformed = rotateCW(new IntPoint(readX, readY), shiftAmount);
		int deltaX = currentX - transformed.x, deltaY = currentY - transformed.y;
		
		for (int x = 0; x < MapUtils.ARR_SIZE; ++x) {
			for (int y = 0; y < MapUtils.ARR_SIZE; ++y) {
				if (readMap[x][y] != null && readMap[x][y].isVisited) {
					IntPoint currTr = rotateCW(new IntPoint(x, y), shiftAmount);
					int xNew = currTr.x + deltaX, yNew = currTr.y + deltaY;
					
					if (xNew < MapUtils.ARR_SIZE &&
						yNew < MapUtils.ARR_SIZE &&
						xNew >= 0 && yNew >= 0) {
						if (cells[xNew][yNew] == null || !cells[xNew][yNew].isVisited) {
							cells[xNew][yNew] = new Cell();
							cells[xNew][yNew].color = readMap[x][y].color;
							cells[xNew][yNew].x = xNew;
							cells[xNew][yNew].y = yNew;
							cells[xNew][yNew].dists = Utils.shift(readMap[x][y].dists, shiftAmount);
							cells[xNew][yNew].isVisited = true;	
						}
					}
				}
			}
		}
		
		// TODO kodu kontrol et
		Uplink.sendIntegratedMap();
	}
	
	/**
	 * Since we say "current cell", isVisited of the cell is also set to true.
	 */
	static void refreshCurrentCell(int x, int y, int heading, float[] dists, int color) {
		Cell c = cells[x][y];
		
		if (c == null) {
			c = new Cell();
			c.x = x;
			c.y = y;
		}
		
		c.dists = Utils.shift(dists, heading);
		c.isVisited = isVisitedArr[c.x][c.y] = isVisitedArrCpy[c.x][c.y] = true;
		c.color = color;
		// TODO might add visitedCount increment
		
		if (!isGoable[c.x][c.y]) {
			++goableCount;
			isGoable[c.x][c.y] = isGoableCpy[c.x][c.y] = true;
		}
	
		if (color != Color.BLACK || hasWeapon) {
			if (dists[0] > Pilot.MARGINAL_STEP && !isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)]) {
				++goableCount;
				isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)] = true;
				isGoableCpy[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)] = true;
			}
			
			heading = MapUtils.turnCount(heading, 1);
			if (dists[1] > Pilot.MARGINAL_STEP && !isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)]) {
				++goableCount;
				isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)] = true;
				isGoableCpy[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)] = true;
			}
			
			heading = MapUtils.turnCount(heading, 1);
			if (dists[2] > Pilot.MARGINAL_STEP && !isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)]) {
				++goableCount;
				isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)] = true;
				isGoableCpy[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)] = true;
			}
			
			heading = MapUtils.turnCount(heading, 1);
			if (dists[3] > Pilot.MARGINAL_STEP && !isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)]) {
				++goableCount;
				isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)] = true;
				isGoableCpy[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)] = true;
			}			
		}
	}

	/**
	 * Restores the C-O-W map
	 */
	static void restore() {
		isVisitedArr = isVisitedArrCpy;
		isGoable = isGoableCpy;
		visitedCount = visitedCountCpy;

		cellsCpy = cells;
	}
	
	static IntPoint globalColoredCoor;
	
	/**
	 * @param c
	 * @return Possible shifts
	 */
	static ArrayList tryMatchByColor(Cell c) {
		if (c.color == Color.GREEN || c.color == Color.BLUE) {	
			IntPoint coloredCoor = MapUtils.findWithColor(readMap, c.color);
			
			if (coloredCoor != null) {
				Cell coloredOne = readMap[coloredCoor.x][coloredCoor.y];
				
				if (coloredOne == null) {
					// TODO should never come here
					System.out.println("TODO should never come here -- 293");
				}
				
				// Discrete versions of the distances at this special cell
				int []currentInted = MapUtils.discreteDistArr(c.dists),
					  coloredInted = MapUtils.discreteDistArr(coloredOne.dists);
				
				ArrayList possibleShifts = new ArrayList();
						
				for (int shiftAmount = 0; shiftAmount < 4; ++shiftAmount) {
					if (Arrays.equals(Utils.shift(coloredInted, shiftAmount), currentInted))
						possibleShifts.add(new Integer(shiftAmount));
				}
			
				if (possibleShifts.size() == 1) {
					// TODO
					// Handle possibility of incoherent matches, i.e, the special (= colored) cell match
					 // and "running" match give different results 
					 //
					//localizeCertainly(c.x, c.y, coloredCoor.x, coloredCoor.y, ((Integer) possibleShifts.get(0)).intValue());
					//FileKeeper.log("3");
					globalColoredCoor = coloredCoor;
					return possibleShifts;
				}
			}
		}
		
		globalColoredCoor = null;
		return null;
	}
	
	static void dfs(Cell c, int heading) {
		// Heading with which we are called
		int originalHeading = heading;

		
		// Angle w.r.t the angle we had at the beginning of this call
		int angle = 0;
		
		
		// Perform the readings
		float dist = Ultrasonic.getDist();
		RadarData radarData = Radar.scanAll();
	
		

		// Update the global map with this cell
		cells[c.x][c.y] = c;
		cellsCpy[c.x][c.y] = c;

		
		// Update the cell data with the physical readings
		refreshCurrentCell(c.x, c.y, originalHeading, radarData.dists, ColorReader.getColor());

		
/*		refreshCurrentCell(x, y, originalHeading, dists, color);
		c.dists = Utils.shift(radarData.dists, originalHeading);
		c.isVisited = isVisitedArr[c.x][c.y] = true;
		c.color = ColorReader.getColor();*/
			
		
		// Increment the visited cell count
		++visitedCount;
		++visitedCountCpy;
		
		
		/*
		// Count the cells that we can go
		if (!isGoable[c.x][c.y]) {
			++goableCount;
			isGoable[c.x][c.y] = true;
		}
	
		if (dist > Pilot.MARGINAL_STEP && !isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)]) {
			++goableCount;
			isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)] = true;
		}
		
		heading = MapUtils.turnCount(heading, 1);
		if (radarData.dists[1] > Pilot.MARGINAL_STEP && !isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)]) {
			++goableCount;
			isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)] = true;
		}
		
		heading = MapUtils.turnCount(heading, 1);
		if (radarData.dists[2] > Pilot.MARGINAL_STEP && !isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)]) {
			++goableCount;
			isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)] = true;
		}
		
		heading = MapUtils.turnCount(heading, 1);
		if (radarData.dists[3] > Pilot.MARGINAL_STEP && !isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)]) {
			++goableCount;
			isGoable[MapUtils.getAdvanceX(c.x, heading)][MapUtils.getAdvanceY(c.y, heading)] = true;
		}	
		*/
		// Restore the heading variables original value
		heading = originalHeading;
		
		
		// Send the new pose info
		Uplink.sendMapInfo(c.x, c.y, c.color, heading, c.dists);
		
		
		// When at blue cell, never miss the chance to take the ball
		if (c.color == Color.BLUE) {
			 gripBall();
		}
		
		
		// When at green and monster is gone, END EVERYTHING
		if (c.color == Color.GREEN && hasKilledGiant) {
			savePrince();
		}
		
		if (c.color == Color.BLACK && hasWeapon) {
			// TODO
			killInUnknownMap();
		} 
		
		if (!localizedAlready) {
			// When at a "special" cell, try to localize with it
			/*if (c.color == Color.GREEN || c.color == Color.BLUE) {	
				IntPoint coloredCoor = MapUtils.findWithColor(readMap, c.color);
				
				if (coloredCoor != null) {
					Cell coloredOne = readMap[coloredCoor.x][coloredCoor.y];
					
					if (coloredOne == null) {
						// TODO should never come here
						System.out.println("TODO should never come here -- 293");
					}
					
					// Discrete versions of the distances at this special cell
					int []currentInted = MapUtils.discreteDistArr(c.dists),
						  coloredInted = MapUtils.discreteDistArr(coloredOne.dists);
					
					ArrayList possibleShifts = new ArrayList();
							
					for (int shiftAmount = 0; shiftAmount < 4; ++shiftAmount) {
						if (Arrays.equals(Utils.shift(coloredInted, shiftAmount), currentInted))
							possibleShifts.add(new Integer(shiftAmount));
					}
				
					if (possibleShifts.size() == 1) {
						// TODO
						// Handle possibility of incoherent matches, i.e, the special (= colored) cell match
						 // and "running" match give different results 
						 //
						localizeCertainly(c.x, c.y, coloredCoor.x, coloredCoor.y, ((Integer) possibleShifts.get(0)).intValue());
						FileKeeper.log("355");
						return;
					}
				}
			}*/
			
			ArrayList possibleShifts = tryMatchByColor(c);
			
			if (possibleShifts != null && possibleShifts.size() == 1) {
				localizeCertainly(c.x, c.y, globalColoredCoor.x, globalColoredCoor.y, ((Integer) possibleShifts.get(0)).intValue());
				return;
			}
			
			// Try generic localization
			
			ArrayList possibleLocalizations =  tryMatch(c.x, c.y);
			
			if (possibleLocalizations.size() == 1 && visitedCount >= 2) {
				Pose coincide = (Pose) possibleLocalizations.get(0);
				localizeCertainly(c.x, c.y, coincide.x, coincide.y, coincide.heading);
				
				return;
			}	
		}		
		
		// If black, just step back (we do not have a weapon bcs we checked it above)
		if (c.color == Color.BLACK && !hasWeapon /* TODO */) {
			// TODO
			FileKeeper.log("375");
			return;
		}
		
		heading = originalHeading;
			
		FileKeeper.log("alper " + dist);
		FileKeeper.log("alper " + c.dists);
		FileKeeper.log("alper " + c);
		FileKeeper.log("alper " + c.color);
		FileKeeper.log("alper " + c.x + " " + c.y);
		FileKeeper.log("alper " + isNotVisited(MapUtils.getAdvanceX(c.x, heading), MapUtils.getAdvanceY(c.y, heading)) + " ");
		// Try front
		if (dist > Pilot.MARGINAL_STEP && isNotVisited(MapUtils.getAdvanceX(c.x, heading), MapUtils.getAdvanceY(c.y, heading))) {
			float traveled = Pilot.safeStep();
			FileKeeper.log("travel");
			
			Cell advance = c.adj[heading] = new Cell();
	
			advance.adj[MapUtils.turnCount(heading, 2)] = c;
			advance.x = MapUtils.getAdvanceX(c.x, heading);
			advance.y = MapUtils.getAdvanceY(c.y, heading);
			
			dfs(advance, heading);
		
			if (checkAndFinish()) return;
			
			Pilot.stepBack(traveled);
		}
		
		
		// Try right
		heading = MapUtils.turnCount(heading, 1);
		
		if (radarData.dists[1] > Pilot.MARGINAL_STEP && isNotVisited(MapUtils.getAdvanceX(c.x, heading), MapUtils.getAdvanceY(c.y, heading))) {
			Pilot.optimizedTurn(-90);
			
			angle = -90;
			dist = Ultrasonic.getDist();
			
			if (dist > Pilot.MARGINAL_STEP) {
				float traveled = Pilot.safeStep();
				
				Cell advance = c.adj[heading] = new Cell();
		
				c.dists[heading] = dist;
				advance.adj[MapUtils.turnCount(heading, 2)] = c;
				advance.x = MapUtils.getAdvanceX(c.x, heading);
				advance.y = MapUtils.getAdvanceY(c.y, heading);
				
				dfs(advance, heading);
				
				if (checkAndFinish()) return;
				
				Pilot.stepBack(traveled);
			}
		}
		
		
		// Try back
		heading = MapUtils.turnCount(heading, 1);
		
		if (radarData.dists[2] > Pilot.MARGINAL_STEP && isNotVisited(MapUtils.getAdvanceX(c.x, heading), MapUtils.getAdvanceY(c.y, heading))) {
			Pilot.optimizedTurn(-180 - angle);
			
			angle = -180;
			dist = Ultrasonic.getDist();
			
			if (dist > Pilot.MARGINAL_STEP) {
				float traveled = Pilot.safeStep();
				
				Cell advance = c.adj[heading] = new Cell();
		
				c.dists[heading] = dist;
				advance.adj[MapUtils.turnCount(heading, 2)] = c;
				advance.x = MapUtils.getAdvanceX(c.x, heading);
				advance.y = MapUtils.getAdvanceY(c.y, heading);
				
				dfs(advance, heading);
				
				if (checkAndFinish()) return;
				
				Pilot.stepBack(traveled);
			}
		}
		
		
		// Try left
		heading = MapUtils.turnCount(heading, 1);
		
		if (radarData.dists[3] > Pilot.MARGINAL_STEP && isNotVisited(MapUtils.getAdvanceX(c.x, heading), MapUtils.getAdvanceY(c.y, heading))) {
			Pilot.optimizedTurn(-270 - angle);
			
			angle = -270;
			dist = Ultrasonic.getDist();
			
			if (dist > Pilot.MARGINAL_STEP) {
				float traveled = Pilot.safeStep();
				
				Cell advance = c.adj[heading] = new Cell();
		
				c.dists[heading] = dist;
				advance.adj[MapUtils.turnCount(heading, 2)] = c;
				advance.x = MapUtils.getAdvanceX(c.x, heading);
				advance.y = MapUtils.getAdvanceY(c.y, heading);
				
				dfs(advance, heading);
				
				if (checkAndFinish()) return;
				
				Pilot.stepBack(traveled);
			}
		}
		
		// Restore the original heading
		if (angle == -90)
			Pilot.optimizedTurn(90);
		else if (angle == -180)
			Pilot.optimizedTurn(180);
		else if (angle == -270)
			Pilot.optimizedTurn(-90);
	}
	
	/**
	 * @return Path. Does not include the current cell.
	 */
	static ArrayList bfs(IntPoint currentPoint, IntPoint target) {
		ArrayList path = new ArrayList();
		ArrayDeque queue = new ArrayDeque();
		boolean isVisited[][] = new boolean[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
		IntPoint parent[][] = new IntPoint[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
		
		queue.add(currentPoint);
		isVisited[currentPoint.x][currentPoint.y] = true;
		
		while (!queue.isEmpty()) {
			IntPoint queueFront = (IntPoint) queue.remove();
			Cell cell = cells[queueFront.x][queueFront.y];
			
			if (queueFront.x == target.x && queueFront.y == target.y) {
				break;
			}
			
			for (int i = 0; i < 4; ++i) {
				int x = MapUtils.getAdvanceX(queueFront.x, i),
					y = MapUtils.getAdvanceY(queueFront.y, i);
				
				if (!isVisited[x][y] &&
					cell != null &&
					MapUtils.discreteDist(cell.dists[i]) > 0) {
					isVisited[x][y] = true;
					queue.add(new IntPoint(x, y));
					parent[x][y] = new IntPoint(queueFront.x, queueFront.y);
				}
			}
		}
		
		int x = target.x, y = target.y;
		
		while (parent[x][y] != null) {
			path.add(0, new IntPoint(x, y));
			int newX = parent[x][y].x,
				newY = parent[x][y].y;
			
			x = newX;
			y = newY;
		}
		
		return path;
	}
	
	/**
	 * 
	 * @param path ArrayList of "IntPoint"s
	 */
	static void executePath(ArrayList path, IntPoint curr) {
		for (int i = 0; i < path.size(); ++i) {
			IntPoint coor = (IntPoint) path.get(i);
			
			if (coor.x != curr.x) {
				if (coor.x > curr.x) {
					// Go east
					Pilot.goAdj(1);
				} else {
					// Go west
					Pilot.goAdj(3);
				}
			} else {
				if (coor.y > curr.y) {
					// Go north
					Pilot.goAdj(0);
				} else {
					// Go south
					Pilot.goAdj(2);
				}
			}
			
			curr.x = coor.x;
			curr.y = coor.y;
			
			// TODO refreshCurrentCell(curr.x, curr.y, Pilot.currentHeading, Radar.scanAll().dists, ColorReader.getColor());
			// Send the new pose info
			refreshCurrentCell(curr.x, curr.y, 0, cells[curr.x][curr.y].dists, ColorReader.getColor());
			Uplink.sendMapInfo(curr.x, curr.y, cells[curr.x][curr.y].color, Pilot.currentHeading, cells[curr.x][curr.y].dists);
			
			ArrayList possibleShifts = tryMatchByColor(cells[curr.x][curr.y]);
			
			if (possibleShifts != null && possibleShifts.size() == 1) {
				restore(); // TODO check
				localizeCertainly(curr.x, curr.y, globalColoredCoor.x, globalColoredCoor.y, ((Integer) possibleShifts.get(0)).intValue());
				Uplink.debug("Rematched");
				executePath(bfs(Pilot.getCurrentCoor(), (IntPoint) path.get(path.size() - 1)), curr); // TODO rematch
				return;
			}
		}
	}
	
	static IntPoint findUnvisitedGoable() {
		for (int x = 0; x < MapUtils.ARR_SIZE; ++x) {
			for (int y = 0; y < MapUtils.ARR_SIZE; ++y) {
				if (isGoable[x][y] && !isVisitedArr[x][y]) {
					return new IntPoint(x, y);
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Assumes we are at a black cell. Finds and kills the giant.
	 * @param black The black cell at which the robot currently resides
	 */
	static void killFromBlack(Cell c) {
		float dist = c.dists[Pilot.currentHeading];
		int heading = Pilot.currentHeading;
		float angle = 0;
		
		// Try front
		if (dist > Pilot.MARGINAL_STEP && isNotVisited(MapUtils.getAdvanceX(c.x, heading), MapUtils.getAdvanceY(c.y, heading))) {
			float traveled = Pilot.safeStep();
			int color = ColorReader.getColor();
			
			refreshCurrentCell(Pilot.currentX, Pilot.currentY, Pilot.currentHeading, Radar.scanAll().dists, color);
			
			if (color == Color.RED) {
				killGiant();
				return;
			}
			
			Pilot.stepBack(traveled);
		}
		
		
		// Try right
		heading = MapUtils.turnCount(heading, 1);
		dist = c.dists[heading];
		
		if (dist > Pilot.MARGINAL_STEP && isNotVisited(MapUtils.getAdvanceX(c.x, heading), MapUtils.getAdvanceY(c.y, heading))) {
			Pilot.optimizedTurn(-90);
			
			angle = -90;
			dist = Ultrasonic.getDist();
			
			if (dist > Pilot.MARGINAL_STEP) {
				float traveled = Pilot.safeStep();
				int color = ColorReader.getColor();
				
				refreshCurrentCell(Pilot.currentX, Pilot.currentY, Pilot.currentHeading, Radar.scanAll().dists, color);
				
				if (color == Color.RED) {
					killGiant();
					return;
				}
				
				Pilot.stepBack(traveled);
			}
		}
		
		// Try back
		heading = MapUtils.turnCount(heading, 1);
		dist = c.dists[heading];
		
		if (dist > Pilot.MARGINAL_STEP && isNotVisited(MapUtils.getAdvanceX(c.x, heading), MapUtils.getAdvanceY(c.y, heading))) {
			Pilot.optimizedTurn(-180 - angle);
			
			angle = -180;
			dist = Ultrasonic.getDist();
			
			if (dist > Pilot.MARGINAL_STEP) {
				float traveled = Pilot.safeStep();
				int color = ColorReader.getColor();
				
				refreshCurrentCell(Pilot.currentX, Pilot.currentY, Pilot.currentHeading, Radar.scanAll().dists, color);
				
				if (color == Color.RED) {
					killGiant();
					return;
				}
				
				/*
				if (ColorReader.getColor() == Color.RED) {
					killGiant();
					return;
				}*/
				
				Pilot.stepBack(traveled);
			}
		}
		
		// Try left
		heading = MapUtils.turnCount(heading, 1);
		dist = c.dists[heading];
		
		if (dist > Pilot.MARGINAL_STEP && isNotVisited(MapUtils.getAdvanceX(c.x, heading), MapUtils.getAdvanceY(c.y, heading))) {
			Pilot.optimizedTurn(-270 - angle);
			
			angle = -270;
			dist = Ultrasonic.getDist();
			
			if (dist > Pilot.MARGINAL_STEP) {
				float traveled = Pilot.safeStep();
				int color = ColorReader.getColor();
				
				refreshCurrentCell(Pilot.currentX, Pilot.currentY, Pilot.currentHeading, Radar.scanAll().dists, color);
				
				if (color == Color.RED) {
					killGiant();
					return;
				}
				
				Pilot.stepBack(traveled);
			}
		}
	}
	
	public static void run() {
		Pilot.currentHeading = MapUtils.HEADING_NORTH;
		FileKeeper.readMap();
		
		Uplink.notifyMode(Uplink.BT_EXEC_MODE);
		Uplink.sendReadMap();
		
		Cell start = new Cell();
		start.x = start.y = MapUtils.START_COOR;
		
		dfs(start, MapUtils.HEADING_NORTH);
		
		if (!localizedAlready) {
			// TODO handle 
			FileKeeper.log("732");
			System.exit(0);
		}
		
		
		// Localization is done, now onto the execution
		
		IntPoint blueCoor = MapUtils.findWithColor(cells, Color.BLUE);
		// Black one should never be null.

		if (!hasWeapon) {
			noLocalizationFinishing = true; // TODO check it
			while (blueCoor == null) {
				IntPoint goable = findUnvisitedGoable();
				
				if (goable == null) {
					// TODO should never come here
				}
				
				executePath(bfs(Pilot.getCurrentCoor(), goable), Pilot.getCurrentCoor());
				Cell newDfsHead = cells[Pilot.currentX][Pilot.currentY] = cellsCpy[Pilot.currentX][Pilot.currentY] = new Cell(); // TODO check code
				newDfsHead.x = Pilot.currentX;
				newDfsHead.y = Pilot.currentY;
				
				dfs(newDfsHead, Pilot.currentHeading);
			} 
			
			Cell blueOne = cells[blueCoor.x][blueCoor.y];
				
			ArrayList path = bfs(Pilot.getCurrentCoor(), blueOne.getCoor());
			executePath(path, Pilot.getCurrentCoor());
				
			gripBall();
		}
		
		IntPoint blackCoor = MapUtils.findWithColor(cells, Color.BLACK);
		
		if (!hasKilledGiant) {
			if (blackCoor == null) {
				// Should never come here
			} 
	
			Cell blackOne = cells[blackCoor.x][blackCoor.y];
			
			ArrayList path = bfs(Pilot.getCurrentCoor(), blackOne.getCoor());
			executePath(path, Pilot.getCurrentCoor());
			
			killFromBlack(blackOne);
		}		
	
		IntPoint greenCoor = MapUtils.findWithColor(cells, Color.GREEN);
		
		while (greenCoor == null) {
			noLocalizationFinishing = true; // TODO check it
			IntPoint goable = findUnvisitedGoable();
			
			if (goable == null) {
				Uplink.debug("No goable cells");
			}
			
			executePath(bfs(Pilot.getCurrentCoor(), goable), Pilot.getCurrentCoor());
			Cell newDfsHead = cells[Pilot.currentX][Pilot.currentY] = new Cell();
			newDfsHead.x = Pilot.currentX;
			newDfsHead.y = Pilot.currentY;
			
			dfs(newDfsHead, Pilot.currentHeading);
		} 
		
		Cell greenOne = cells[greenCoor.x][greenCoor.y];
			
		ArrayList path = bfs(Pilot.getCurrentCoor(), greenOne.getCoor());
		executePath(path, Pilot.getCurrentCoor());
			
		savePrince();
	}
}
	
