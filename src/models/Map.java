package models;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import services.BallService;
import services.CarService;
import services.WallService;

public class Map {

	private Mat frame;
	public Mat originalframe;

	public Car car;
	public List<Ball> balls = new ArrayList<Ball>();

	public Point center;
	private Point origo;
	
	private Wall wall;
	private double radian;

	public Map(Car car, Mat frame) {

		this.originalframe = frame;

		this.frame = Mat.zeros(frame.rows() * 2, frame.cols() * 2, CvType.CV_8UC3);

		this.center = new Point(frame.cols(), frame.rows());

		// Origo is now center of the car
		this.origo = car.center;

		// Lets corrigate the car

		this.car = new Car(new Point(car.front.x - origo.x, car.front.y - origo.y),
				new Point(car.back.x - origo.x, car.back.y - origo.y));

		CarService.drawCar(this.frame, this.car, this.center);
		
	}

	public void drawAxis() {

		// X
		Imgproc.line(this.frame, new Point(-1 * center.x * 2, 0 + center.y), new Point(center.x * 2, 0 + center.y),
				new Scalar(0, 0, 255));

		// X
		Imgproc.line(this.frame, new Point(0 + center.x, -3000), new Point(0 + center.x, 1000), new Scalar(0, 0, 255));

	}

	public Mat getFrame() {
		return frame;
	}

	public void addBalls(List<Ball> balls) {

		for (Ball ball : balls) {

			this.balls.add(new Ball(new Point(ball.point.x - origo.x, ball.point.y - origo.y), ball.area));
		}

		BallService.drawBalls(this.frame, this.balls, this.center);
	}

	public void addWall(Wall wall) {

		this.wall = Wall.copyWithOrigo(wall, this.origo);
		WallService.drawWall(this.frame, this.wall, this.center);
	}

	public Point rotatePoint(Point p) {

		double s = Math.sin(radian);
		double c = Math.cos(radian);

		double x = p.x * c + p.y * s;
		double y = -1 * p.x * s + p.y * c;

		Point p2 = new Point(x, y);

		return p2;

	}

	private Point derotatePoint(Point p) {

		double s = Math.sin(-radian);
		double c = Math.cos(-radian);

		double x = p.x * c + p.y * s;
		double y = -1 * p.x * s + p.y * c;

		Point p2 = new Point(x, y);

		return p2;
	}

	public Wall getWall() {
		return this.wall;
	}

	public void corrected() {

		this.radian = Math.atan2(this.car.front.y, this.car.front.x);

		this.frame = Mat.zeros(frame.rows(), frame.cols(), CvType.CV_8UC3);

		if (this.car != null) {
			this.car = new Car(rotatePoint(this.car.front), rotatePoint(this.car.back));
			// CarService.drawCar(this.frame, this.car, this.center);
		}
	
		
		ArrayList<Ball> temp = new ArrayList<Ball>();
		if (this.balls.size() > 0) {

			for (Ball ball : this.balls) {
				temp.add(new Ball(rotatePoint(ball.point), ball.area));
			}

			this.balls = temp;

			// BallService.drawBalls(frame, this.balls, center);
		}

		if (this.wall != null) {

			Point[] wtemp = new Point[this.wall.points.length];

			for (int i = 0; i < this.wall.points.length; i++) {

				wtemp[i] = rotatePoint(this.wall.points[i]);

			}

			this.wall = new Wall(new MatOfPoint(wtemp));
			// WallService.drawWall(this.frame, this.wall, this.center);

		}

	}

	public void drawCar(Scalar color, int size) {
		CarService.drawCar(this.frame, this.car, this.center, color, size);

	}

	public void drawWall(Scalar color, int size) {
		WallService.drawWall(this.frame, this.wall, this.center, color, size);

	}

	public void drawBalls(Scalar color, int size) {
		BallService.drawBalls(frame, balls, center);

	}
	
	public Point getOriginalPoint(Point front) {

		
		
		Point d = derotatePoint(front);

		System.out.println(d.toString());

		return new Point(d.x + origo.x, d.y + origo.y);
	}

	public Point correctPoint(Point p) {

		p = new Point(p.x - origo.x, p.y - origo.y);
		
		p  = rotatePoint(p);
		
		return new Point(p.x + center.x, p.y + center.y);

	}

}
