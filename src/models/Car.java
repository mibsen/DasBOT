package models;

import org.opencv.core.Point;

import services.WallService;

public class Car {

	public Double width;
	public Point frontMarker;
	public Point backMarker;

	// Car Points
	public Point front;
	public Point back;
	public Point backRight;
	public Point backLeft;
	public Point frontRight;
	public Point frontLeft;
	public Point center;
	public Point pickBack;
	public Point pickFront;
	public Point pickFrontRight;
	public Point pickBackRight;
	public Point pickBackLeft;
	public Point pickFrontLeft;
	public Point pickCenter;

	// Car Build variable
	
	public static float widthInCM = 10.7F;
	public static float carHeightInCM = 22F;
	public static float frontToMakerCM = 7F;
	public static float maxPicktoMarkerCM = 14F;
	public static float pickWithCM = 3F;
	public static float backToMakerCM = 9F;
	
	public static float radiusLeftCM = 9F;
	public static float radiusRightCM = 14F;
	
	public Car(Point front, Point back) {

		this.frontMarker = front;
		this.backMarker = back;
		this.width = Math.abs(Math.sqrt(Math.pow(frontMarker.x - backMarker.x, 2) + Math.pow(frontMarker.y - backMarker.y, 2)));

		// ## Build car points ##
		this.center = new Point((front.x + back.x) / 2, (front.y + back.y) / 2);

		calculateParts();
	}
	
	private void calculateParts() {
		
		getBack();
		getFront();
		
		getRightSide();
		getLeftSide();
		
		getPick();
		
	}

	
	private void getPick() {
	
		pickBack = front;
		getPickFront();
		getPickRight();
		
		
		pickCenter = new Point(((pickBack.x-pickFront.x)/2)+pickFront.x,((pickBack.y-pickFront.y)/2)+pickFront.y); 
				
				
	}
	
	private void getPickFront() {
		
		float factor = maxPicktoMarkerCM/ widthInCM;

		double x = frontMarker.x - backMarker.x;
		double y = frontMarker.y - backMarker.y;

		double nx = x * factor;
		double ny = y * factor;

		pickFront = new Point(frontMarker.x + nx, frontMarker.y + ny);	
	}
	
	private void getPickRight() {
		
		float factor = (pickWithCM/2)/ widthInCM;

		double x = backMarker.x - frontMarker.x ;
		double y = backMarker.y - frontMarker.y ;

		double nx = x * factor;
		double ny = y * factor;
		
		double radian = Math.toRadians(90);

		double s = Math.sin(radian);
		double c = Math.cos(radian);

		double nnx = nx * c + ny * s;
		double nny = -1 * nx * s + ny * c;
		
		pickBackRight = new Point(nnx + front.x, nny + front.y);
		pickFrontRight = new Point(nnx + pickFront.x, nny + pickFront.y);
		
		 radian = Math.toRadians(-90);

		 s = Math.sin(radian);
		 c = Math.cos(radian);

		 nnx = nx * c + ny * s;
		 nny = -1 * nx * s + ny * c;

		pickBackLeft = new Point(nnx + front.x, nny + front.y);
		pickFrontLeft = new Point(nnx + pickFront.x, nny + pickFront.y);
		
		
	}
	
	
	private void getBack() {
		
		float factor =   (widthInCM ) / (backToMakerCM + widthInCM);

		double x = backMarker.x - frontMarker.x;
		double y = backMarker.y - frontMarker.y;

		double nx = x * factor;
		double ny = y * factor;

		back = new Point((backMarker.x) + nx, backMarker.y + +ny);
		
	}
	
	private void getRightSide() {
		
		float factor = radiusRightCM / widthInCM ;

		double x = backMarker.x - frontMarker.x;
		double y = backMarker.y - frontMarker.y;

		double nx = x * factor;
		double ny = y * factor;

		double radian = Math.toRadians(90);

		double s = Math.sin(radian);
		double c = Math.cos(radian);

		double nnx = nx * c + ny * s;
		double nny = -1 * nx * s + ny * c;
		
		backRight = new Point(nnx + back.x, nny + back.y);
		frontRight = new Point(nnx + front.x, nny + front.y);

		
	}
	
	private void getLeftSide() {
		
		float factor = radiusLeftCM / widthInCM ;

		double x = backMarker.x - frontMarker.x;
		double y = backMarker.y - frontMarker.y;

		double nx = x * factor;
		double ny = y * factor;

		double radian = Math.toRadians(-90);

		double s = Math.sin(radian);
		double c = Math.cos(radian);

		double nnx = nx * c + ny * s;
		double nny = -1 * nx * s + ny * c;
		
		backLeft = new Point(nnx + back.x, nny + back.y);
		frontLeft = new Point(nnx + front.x, nny + front.y);

		
	}
	
	private void getFront() {
		
		float factor = frontToMakerCM / widthInCM;

		
		double x = frontMarker.x - backMarker.x;
		double y = frontMarker.y - backMarker.y;

		double nx = x * factor;
		double ny = y * factor;

		front = new Point(frontMarker.x + nx, frontMarker.y + ny);
		
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
		return new Car(correctPoint(frontMarker), correctPoint(backMarker));
	}

}
