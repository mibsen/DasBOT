package services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import models.ObstacleSettings;
import models.Wall;
import models.WallSettings;

public class WallService {

	private WallSettings settings;
	private ObstacleSettings obstacleSettings;
	public Wall wall;
	public Wall obstacle;
	public boolean cache = true;

	public WallService(WallSettings settings, ObstacleSettings obstacleSettings) {
		this.settings = settings;
		this.obstacleSettings = obstacleSettings;
	}

	public Wall getWall() {
		return wall;
	}

	public Wall getObstacle() {
		return obstacle;
	}

	public void locateWalls(Mat f) {

		Mat frame = getWallFrame(f);


		getWallsFromFrame(frame);
	}

	public Mat getWallFrame(Mat f) {

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
	public void getWallsFromFrame(Mat wallFrame) {
		// TODO Auto-generated method stub

		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();

		// Imgproc.Canny(wallFrame, wallFrame, 300, 1000);
		Imgproc.Canny(wallFrame, wallFrame, settings.threshold1, settings.threshold2);

		// find contours
		Imgproc.findContours(wallFrame, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

		if (contours.size() == 0) {
			System.out.println("COULD NOT LOCATE WALL OR OBSTACLE");
			return;
		}

		MatOfPoint[] c = new MatOfPoint[2];

		for (MatOfPoint temp : contours) {
			if (Imgproc.contourArea(temp) > settings.minArea) {

				if (c[0] != null) {
					if (Imgproc.contourArea(c[0]) > Imgproc.contourArea(temp)) {
						wall = new Wall(temp);
						c[0] = temp;
					}

				} else {
					c[0] = temp;
					wall = new Wall(temp);
				}

			} else if (Imgproc.contourArea(temp) > obstacleSettings.minArea
					&& Imgproc.contourArea(temp) < obstacleSettings.maxArea) {
				c[1] = temp;
				obstacle = new Wall(temp);
			}
		}
		

		//System.out.println("Wall size: " + (c[0] != null ? Imgproc.contourArea(c[0]) : ""));
		//System.out.println("Obstacle size: " + (c[1] != null ? Imgproc.contourArea(c[1]) : ""));

		if (c[0] == null) {
			System.out.println("Wall is too small :-)");
			return;
		}
		if (c[1] == null) {
			System.out.println("Obstacle can't be found :-)");
			return;
		}

		// Potentielt noget filter!
		// double area = Imgproc.contourArea(c);
		
	}

	public void drawWall(Mat frame, Wall wall) {

		if (wall == null) {
			return;
		}

		Point p1 = wall.points[0];

		for (int i = 1; i < wall.points.length; i++) {
			Imgproc.line(frame, p1, wall.points[i], new Scalar(0, 250, 0));
			p1 = wall.points[i];
		}

		Imgproc.line(frame, p1, wall.points[0], new Scalar(0, 250, 0));

		for (Point point : wall.points) {
			Imgproc.drawMarker(frame, point, new Scalar(0, 250, 0), 3);
		}

		// Imgproc.drawContours(frame, contours, 0, new Scalar(0, 250, 0), 3);
	}

	public static void drawWall(Mat frame, Wall wall, Point center) {
		drawWall(frame, wall, center, new Scalar(0, 250, 0), 1);
	}

	public static void drawWall(Mat frame, Wall wall, Point center, Scalar color, int size) {

		Point p1 = wall.points[0];

		for (int i = 1; i < wall.points.length; i++) {
			Imgproc.line(frame, new Point(p1.x + center.x, p1.y + center.y),
					new Point(wall.points[i].x + center.x, wall.points[i].y + center.y), color, size);
			p1 = wall.points[i];
		}

		Imgproc.line(frame, new Point(p1.x + center.x, p1.y + center.y),
				new Point(wall.points[0].x + center.x, wall.points[0].y + center.y), color, size);

		for (Point point : wall.points) {
			//Imgproc.drawMarker(frame, new Point(point.x + center.x, point.y + center.y), color, 3, size);
		}

	}

}
