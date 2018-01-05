package com.alpercakan.cmpe434;

public class Cell {
	// north, east, south, west
	public Cell adj[] = new Cell[4];

	// north, east, south, west
	public float dists[] = new float[4];
	
	int x, y;

	public int color;
	
	boolean isVisited = false;
}