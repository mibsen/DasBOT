package models;

import org.opencv.core.Point;

public class Car {

	public Point front;
	public Point back;
	public Point center;

	public Car(Point front, Point back) {
		this.front = front;
		this.back = back;
		
		this.center = new Point((front.x + back.x)/2, (front.y+back.y)/2);
	}

}
