package models;

import org.opencv.core.Point;

public class Car {

	public Point front;
	public Point back;
	public Point center;
	public Double  width;
	public static float widthInCM = 13.5F;
	
	public Car(Point front, Point back) {
		this.front = front;
		this.back = back;
		this.center = new Point((front.x + back.x)/2, (front.y+back.y)/2);
		this.width = Math.abs(Math.sqrt(Math.pow(front.x-back.x, 2) + Math.pow(front.y-back.y, 2)));
	}

}
