package com.alpercakan.cmpe434;

import lejos.robotics.Color;

public class MappingTask {
	
	static boolean [][]isVisitedArr = new boolean[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
	static boolean [][]isGoable = new boolean[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
	static int visitedCount = 0, goableCount = 0;
	static int [][]colorArr = new int[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
	static Cell [][]cells = new Cell[MapUtils.ARR_SIZE][MapUtils.ARR_SIZE];
	
	static boolean isNotVisited(int x, int y) {
		return !isVisitedArr[x][y];
	}
	
	/*
	public static final int DIRECTION_RIGHT = 0,
							DIRECTION_LEFT = 1,
							DIRECTION_BACK = 2,
							DIRECTION_FRONT = 3;*/
	
	public static boolean checkAndFinish() {
		return goableCount == visitedCount;
	}
	
	static void dfs(Cell c, int heading) {
		// Heading with which we are called
		int originalHeading = heading;
		
		
		// Angle w.r.t the angle we had at the beginning of this call
		int angle = 0;
		
		
		// Perform the readings
		float dist = Ultrasonic.getDist();
		RadarData radarData = Radar.scanAll();
		
		
		// Update the cell data with the physical readings
		c.dists = Utils.shift(radarData.dists, originalHeading);
		c.isVisited = isVisitedArr[c.x][c.y] = true;
		c.color = ColorReader.getColor();
		
		
		// Update the global map with this cell
		cells[c.x][c.y] = c;
		
		
		// Increment the visited cell count
		++visitedCount;
		
		
		// Send the new pose info
		Uplink.sendMapInfo(c.x, c.y, c.color, heading, c.dists);
		
		if (c.color == Color.BLACK)
			return;
		
		// Count the cells that we can go
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
		
		
		// Restore the heading variables original value
		heading = originalHeading;
			
		// Try front
		if (dist > Pilot.MARGINAL_STEP && isNotVisited(MapUtils.getAdvanceX(c.x, heading), MapUtils.getAdvanceY(c.y, heading))) {
			float traveled = Pilot.safeStep();
			
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
	
	public static final int INITIAL_FALSE_READ_LIMIT = 5;

	public static void run() {
		Cell start = new Cell();
		start.x = start.y = MapUtils.START_COOR;
		
		Uplink.notifyMode(Uplink.BT_MAPPING_MODE);
		
		float traveled = 0;
		int counter = 0;
		
		while (Ultrasonic.getDist() < INITIAL_FALSE_READ_LIMIT) {
			Utils.sleep(100);
			
			int multiplier = -1;
			if (counter % 2 == 0) {
				multiplier = +1;
			}
			
			Pilot.pilot.travel(5 * multiplier);
			traveled += 5 * multiplier;
		}
		
		if (traveled > 1)
			Pilot.pilot.travel(-traveled);
		
		dfs(start, MapUtils.HEADING_NORTH);
		
		FileKeeper.dumpMap();
		
		Utils.tripleBeep();
	}
}
