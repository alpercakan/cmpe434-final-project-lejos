package com.alpercakan.cmpe434;

public class MapUtils {
	public static final int ARR_SIZE = 13;
	public static final int START_COOR = 6;

	public static final int HEADING_NORTH = 0,
				            HEADING_EAST = 1,
				            HEADING_SOUTH = 2,
				            HEADING_WEST = 3;
	
	static IntPoint findWithColor(Cell [][]map, int color) {
		for (int x = 0; x < ARR_SIZE; ++x) {
			for (int y = 0; y < ARR_SIZE; ++y) {
				if (map[x][y] != null && map[x][y].color == color)
					return new IntPoint(x, y);
			}
		}
		
		return null;
	}
	
	static int turnCount(int heading, int count) {
		return (heading + count + 16) % 4;
	}
	
	public static int discreteDist(float dist) {
		if (dist < Pilot.MARGINAL_STEP)
			return 0;
		
		if (dist < Pilot.MARGINAL_STEP * 2)
			return 1;

		return 2;
	}

	public static int[] discreteDistArr(float dist[]) {
		int []discreteArr = new int[4];
		
		for (int i = 0; i < 4; ++i) {
			discreteArr[i] = discreteDist(dist[i]);
		}
		
		return discreteArr;
	}
	
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
}
