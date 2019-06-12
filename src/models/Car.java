package models;

import org.opencv.core.Point;

import services.WallService;

public class Car {

	public Point front;
	public Point back;
	public Point center;
	public Double  width;
	public static float widthInCM = 14F;
	public static float carHeightInCM = 25F;
	
	public Car(Point front, Point back) {
		this.front = front;
		this.back = back;
		this.center = new Point((front.x + back.x)/2, (front.y+back.y)/2);
		this.width = Math.abs(Math.sqrt(Math.pow(front.x-back.x, 2) + Math.pow(front.y-back.y, 2)));
	}

	
	private Point correctPoint(Point point) {

		double factor = carHeightInCM / WallService.camHeight;

		Point wp = point;

		double x = wp.x - WallService.imageCenter.x;
		double y = wp.y - WallService.imageCenter.y;

		// x = wp.x - center.x;
		// y = wp.y - center.y;

		double nx = x * factor;
		double ny = y * factor;

		return new Point((wp.x) - nx, (wp.y) - ny);

	}
	
	public Car substractHeight(Point point) {
		return new Car(correctPoint(front),correctPoint(back));
	}

}
