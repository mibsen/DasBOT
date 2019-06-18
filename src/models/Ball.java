
package models;

import org.opencv.core.Point;

public class Ball {

	public Point point;
	public double area;
	public static double ballHeightInCM = 2.0D;
	
	public Ball(Point p, double area) {
		
		this.point = p.clone();
		this.area = area;
	}

}
