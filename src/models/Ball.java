package models;

import org.opencv.core.Point;

public class Ball {

	public Point point;
	
	
	public Ball(Point p) {
		
		this.point = p.clone();
	}

}
