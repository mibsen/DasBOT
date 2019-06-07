package services;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import models.Obstacle;
import models.ObstacleSettings;

public class ObstacleService {
	

	private ObstacleSettings settings;
	public Obstacle temp;
	public boolean cache = true;

	public ObstacleService(ObstacleSettings settings) {
		this.settings = settings;
	}

	public Obstacle getObstacle(Mat f) {

		Mat frame = getObstacleFrame(f);

		Obstacle w = getObstacleFromFrame(frame);
		
		if (!cache) {
			return  w;
		}
		if (w != null) {
			temp = w;
			return w;
		}
		else {
			return temp;
		}
	}

	public Mat getObstacleFrame(Mat f) {

		double hueStart = settings.image.hue.start;
		double hueStop = settings.image.hue.stop;
		double saturationStart = settings.image.saturation.start;
		double saturationStop = settings.image.saturation.stop;
		double valueStart = settings.image.value.start;
		double valueStop = settings.image.value.stop;
		double blur = settings.image.blur;

		Mat frame = f.clone();

		// remove some noise
		// Imgproc.blur(frame, frame, new Size(20, 20));
		Imgproc.blur(frame, frame, new Size(blur, blur));

		// convert the frame to HSV
		Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2HSV);

		// get thresholding values from the UI
		// remember: H ranges 0-180, S and V range 0-255
		Scalar minValues = new Scalar(hueStart, saturationStart, valueStart);
		Scalar maxValues = new Scalar(hueStop, saturationStop, valueStop);

		// threshold HSV image to select tennis balls
		Core.inRange(frame, minValues, maxValues, frame);

		return frame;
	}

	// https://www.programcreek.com/java-api-examples/?class=org.opencv.imgproc.Imgproc&method=HoughLinesP
	// https://blog.ayoungprogrammer.com/2013/04/tutorial-detecting-multiple-rectangles.html/
	public Obstacle getObstacleFromFrame(Mat obstacleFrame) {
		// TODO Auto-generated method stub

		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();

		// Imgproc.Canny(obstacleFrame, obstacleFrame, 300, 1000);
		Imgproc.Canny(obstacleFrame, obstacleFrame, settings.threshold1, settings.threshold2);

		// find contours
		Imgproc.findContours(obstacleFrame, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

		if (contours.size() == 0) {
			System.out.println("COULD NOT LOCATE OBSTACLE");
			return null;
		}
		else if(Imgproc.contourArea(contours.get(0)) < settings.minArea) {
			System.out.println("Obstacles are too small :-)");
			return null;
		}		
		else if(Imgproc.contourArea(contours.get(0)) < settings.maxArea) {
			System.out.println("Obstacles are too big :-)");
			return null;
		}
		
		

		MatOfPoint c = contours.get(0);

		// Potentielt noget filter!
		// double area = Imgproc.contourArea(c);

		Obstacle o = new Obstacle(c);

		return o;

	}

	public void drawObstacle(Mat frame, Obstacle obstacle) {

		Point p1 = obstacle.points[0];

		for (int i = 1; i < obstacle.points.length; i++) {
			Imgproc.line(frame, p1, obstacle.points[i], new Scalar(0, 250, 0));
			p1 = obstacle.points[i];
		}

		Imgproc.line(frame, p1, obstacle.points[0], new Scalar(0, 250, 0));

		for (Point point : obstacle.points) {
			Imgproc.drawMarker(frame, point, new Scalar(0, 250, 0), 3);
		}

		// Imgproc.drawContours(frame, contours, 0, new Scalar(0, 250, 0), 3);
	}

	public static void drawObstacle(Mat frame, Obstacle obstacle, Point center) {
		drawObstacle(frame, obstacle, center, new Scalar(0, 250, 0), 1);
	}

	public static void drawObstacle(Mat frame, Obstacle obstacle, Point center, Scalar color, int size) {

		Point p1 = obstacle.points[0];

		for (int i = 1; i < obstacle.points.length; i++) {
			Imgproc.line(frame, new Point(p1.x + center.x, p1.y + center.y),
					new Point(obstacle.points[i].x + center.x, obstacle.points[i].y + center.y), color, size);
			p1 = obstacle.points[i];
		}

		Imgproc.line(frame, new Point(p1.x + center.x, p1.y + center.y),
				new Point(obstacle.points[0].x + center.x, obstacle.points[0].y + center.y), color, size);

		for (Point point : obstacle.points) {
			Imgproc.drawMarker(frame, new Point(point.x + center.x, point.y + center.y), color, 3, size);
		}

	}

}
