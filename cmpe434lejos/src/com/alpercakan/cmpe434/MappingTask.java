package com.alpercakan.cmpe434;

import java.io.DataOutputStream;

import lejos.hardware.Sound;
import lejos.robotics.Color;

public class MappingTask {
	
	public static final int ARR_SIZE = 13;
	public static final int START_COOR = 6;
	
	static boolean [][]isVisitedArr = new boolean[ARR_SIZE][ARR_SIZE];
	static boolean [][]isGoable = new boolean[ARR_SIZE][ARR_SIZE];
	static int visitedCount = 0, goableCount = 0;
	static int [][]colorArr = new int[ARR_SIZE][ARR_SIZE];
	static Cell [][]cells = new Cell[ARR_SIZE][ARR_SIZE];
	
	static boolean isNotVisited(int x, int y) {
		return !isVisitedArr[x][y];
	}
	
	public static final int HEADING_NORTH = 0,
                            HEADING_EAST = 1,
                            HEADING_SOUTH = 2,
                            HEADING_WEST = 3;
	
	// Turn right = +1
	
	public static final int DIRECTION_RIGHT = 0,
							DIRECTION_LEFT = 1,
							DIRECTION_BACK = 2,
							DIRECTION_FRONT = 3;
	
	public static int getAdvanceX(int x, int heading) {
		switch (heading) {
		case HEADING_EAST:
			return x + 1;
		case HEADING_WEST:
			return x - 1;
		case HEADING_SOUTH:
			return x;
		case HEADING_NORTH:
			return x;
		}
		
		return -1;
	}
	
	public static int getAdvanceY(int y, int heading) {
		switch (heading) {
		case HEADING_EAST:
			return y;
		case HEADING_WEST:
			return y;
		case HEADING_SOUTH:
			return y - 1;
		case HEADING_NORTH:
			return y + 1;
		}
		
		return -1;
	}
	
	
	static int turnCount(int heading, int count) {
		return (heading + count + 16) % 4;
	}
	
	public static int leftCount = 0;
	
	public static boolean checkAndFinish() {
		return goableCount == visitedCount;
	}
	
	static void dfs(Cell c, int heading) {
		int originalHeading = heading;
		int angle = 0;
		
		c.isVisited = isVisitedArr[c.x][c.y] = true;
		++visitedCount;
		c.color = ColorReader.getColor();
		
		cells[c.x][c.y] = c;
		
		colorArr[c.x][c.y] = c.color;
		
		float dist = Ultrasonic.getDist();
		RadarData radarData = Radar.scanAll();
		
		Uplink.sendMapInfo(c.x, c.y, c.color, heading, radarData);
		
		if (c.color == Color.BLACK)
			return;
		
		if (dist > Pilot.MARGINAL_STEP && !isGoable[getAdvanceX(c.x, heading)][getAdvanceY(c.y, heading)]) {
			++goableCount;
			isGoable[getAdvanceX(c.x, heading)][getAdvanceY(c.y, heading)] = true;
		}
		heading = turnCount(heading, 1);
		if (radarData.dists[1] > Pilot.MARGINAL_STEP && !isGoable[getAdvanceX(c.x, heading)][getAdvanceY(c.y, heading)]) {
			++goableCount;
			isGoable[getAdvanceX(c.x, heading)][getAdvanceY(c.y, heading)] = true;
		}
		heading = turnCount(heading, 1);
		if (radarData.dists[2] > Pilot.MARGINAL_STEP && !isGoable[getAdvanceX(c.x, heading)][getAdvanceY(c.y, heading)]) {
			++goableCount;
			isGoable[getAdvanceX(c.x, heading)][getAdvanceY(c.y, heading)] = true;
		}
		heading = turnCount(heading, 1);
		if (radarData.dists[3] > Pilot.MARGINAL_STEP && !isGoable[getAdvanceX(c.x, heading)][getAdvanceY(c.y, heading)]) {
			++goableCount;
			isGoable[getAdvanceX(c.x, heading)][getAdvanceY(c.y, heading)] = true;
		}
		
		heading = originalHeading;
			
		if (dist > Pilot.MARGINAL_STEP && isNotVisited(getAdvanceX(c.x, heading), getAdvanceY(c.y, heading))) {
			float travelled = Pilot.safeStep();
			
			Cell advance = c.adj[heading] = new Cell();
	
			c.dists[heading] = dist;
			advance.adj[turnCount(heading, 2)] = c;
			advance.x = getAdvanceX(c.x, heading);
			advance.y = getAdvanceY(c.y, heading);
			
			dfs(advance, heading);
			--leftCount;
			if (checkAndFinish()) return;
			Pilot.pilot.travel(-travelled);
			Uplink.sendMapInfo(c.x,c.y, c.color, heading, radarData);
		}
		
		heading = turnCount(heading, 1);
		
		if (radarData.dists[1] > Pilot.MARGINAL_STEP && isNotVisited(getAdvanceX(c.x, heading), getAdvanceY(c.y, heading))) {
			Pilot.optimizedTurn(-90);
			Uplink.sendMapInfo(c.x,c.y, c.color, heading, radarData);
			angle = -90;
			dist = Ultrasonic.getDist();
			
			if (dist > Pilot.MARGINAL_STEP) {
				float travelled = Pilot.safeStep();
				
				Cell advance = c.adj[heading] = new Cell();
		
				c.dists[heading] = dist;
				advance.adj[turnCount(heading, 2)] = c;
				advance.x = getAdvanceX(c.x, heading);
				advance.y = getAdvanceY(c.y, heading);
				
				dfs(advance, heading);
				--leftCount;
				if (checkAndFinish()) return;
				Pilot.pilot.travel(-travelled);
			}
			Uplink.sendMapInfo(c.x, c.y, c.color, heading, radarData);
		}
		
		heading = turnCount(heading, 1);
		
		if (radarData.dists[2] > Pilot.MARGINAL_STEP && isNotVisited(getAdvanceX(c.x, heading), getAdvanceY(c.y, heading))) {
			Pilot.optimizedTurn(-180 - angle);
			Uplink.sendMapInfo(c.x,c.y, c.color, heading, radarData);
			angle = -180;
			dist = Ultrasonic.getDist();
			
			if (dist > Pilot.MARGINAL_STEP) {
				float travelled = Pilot.safeStep();
				
				Cell advance = c.adj[heading] = new Cell();
		
				c.dists[heading] = dist;
				advance.adj[turnCount(heading, 2)] = c;
				advance.x = getAdvanceX(c.x, heading);
				advance.y = getAdvanceY(c.y, heading);
				
				dfs(advance, heading);
				--leftCount;
				if (checkAndFinish()) return;
				Pilot.pilot.travel(-travelled);
				Uplink.sendMapInfo(c.x, c.y, c.color, heading, radarData);
			}
		}
		
		heading = turnCount(heading, 1);
		
		if (radarData.dists[3] > Pilot.MARGINAL_STEP && isNotVisited(getAdvanceX(c.x, heading), getAdvanceY(c.y, heading))) {
			Pilot.optimizedTurn(-270 - angle);
			Uplink.sendMapInfo(c.x,c.y, c.color, heading, radarData);
			angle = -270;
			dist = Ultrasonic.getDist();
			
			if (dist > Pilot.MARGINAL_STEP) {
				float travelled = Pilot.safeStep();
				
				Cell advance = c.adj[heading] = new Cell();
		
				c.dists[heading] = dist;
				advance.adj[turnCount(heading, 2)] = c;
				advance.x = getAdvanceX(c.x, heading);
				advance.y = getAdvanceY(c.y, heading);
				
				dfs(advance, heading);
				--leftCount;
				if (checkAndFinish()) return;
				Pilot.pilot.travel(-travelled);
				Uplink.sendMapInfo(c.x,c.y, c.color, heading, radarData);
			}
		}
		
		if (angle == -90)
			Pilot.optimizedTurn(90);
		else if (angle == -180)
			Pilot.optimizedTurn(180);
		else if (angle == -270)
			Pilot.optimizedTurn(-90);
		
		Uplink.sendMapInfo(c.x, c.y, c.color, originalHeading, radarData);
	}
	
	public static final int INITIAL_FALSE_READ_LIMIT = 5;

	public static void run() {
		Cell start = new Cell();
		start.x = start.y = START_COOR;
		
		float travelled = 0;
		int counter = 0;
		
		while (Ultrasonic.getDist() < INITIAL_FALSE_READ_LIMIT) {
			Utils.sleep(100);
			
			int multiplier = -1;
			if (counter % 2 == 0) {
				multiplier = +1;
			}
			
			Pilot.pilot.travel(5 * multiplier);
			travelled += 5 * multiplier;
		}
		
		if (travelled > 1)
			Pilot.pilot.travel(-travelled);
		
		dfs(start, HEADING_NORTH);
		
		Sound.beep();
		Utils.sleep(500);
		Sound.beep();
		Utils.sleep(500);
		Sound.beep();
	}
}
