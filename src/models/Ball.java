package models;

import org.opencv.core.Point;

public class Ball {

	public Point point;
	public double area;
	
	
	public Ball(Point p, double area) {
		
		this.point = p.clone();
		this.area = area;
	}

}
