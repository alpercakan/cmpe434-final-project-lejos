package com.alpercakan.cmpe434;

import java.util.Arrays;

import lejos.robotics.Color;

public class Cell {
	// north, east, south, west
	public Cell adj[] = new Cell[4];

	// north, east, south, west
	public float dists[] = new float[4];

	int x = -1, y = -1;

	public int color = Color.NONE;

	boolean isVisited = false;

	IntPoint getCoor() {
		return new IntPoint(x, y);
	}

	public String toString() {
		return "(" + Arrays.toString(dists) + ", (" + x + ", " + y + "), " + color + ", " + isVisited + ")";
	}

	public void copyFrom(Cell c) {
		dists = Arrays.copyOf(c.dists, 4);
		x = c.x;
		y = c.y;
		color = c.color;
		isVisited = c.isVisited;
	}
}