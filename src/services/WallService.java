package services;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
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
	public static double camHeight;
	public static Point imageCenter;
	public boolean cache = true;

	public WallService(WallSettings settings, ObstacleSettings obstacleSettings) {
		this.settings = settings;
		camHeight = settings.camHeight;
		this.obstacleSettings = obstacleSettings;
	}

	public Wall getWall() {
		return wall;
	}

	public Wall getObstacle() {
		return obstacle;
	}

	public Mat locateWallsAndCorrectFrame(Mat f) {

		Mat frame = getWallFrame(f);
		f = getWallsFromFrame(f, frame);

		return f;

	}

	public Mat getWallFrame(Mat f) {

		imageCenter = new Point(f.width() / 2, f.height() / 2);

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
	public Mat getWallsFromFrame(Mat f, Mat wallFrame) {
		// TODO Auto-generated method stub

		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();

		// Imgproc.Canny(wallFrame, wallFrame, 300, 1000);
		Imgproc.Canny(wallFrame, wallFrame, settings.threshold1, settings.threshold2);

		// find contours
		Imgproc.findContours(wallFrame, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

		if (contours.size() == 0) {
			System.out.println("COULD NOT LOCATE WALL OR OBSTACLE");
			return f;
		}

		MatOfPoint[] c = new MatOfPoint[2];

		for (MatOfPoint temp : contours) {
			if (Imgproc.contourArea(temp) > settings.minArea) {

				if (c[0] != null) {
					if (Imgproc.contourArea(c[0]) > Imgproc.contourArea(temp)) {
						wall = new Wall(temp);
						f = correctWall(f, wall);
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

	//	System.out.println("Wall size: " + (c[0] != null ? Imgproc.contourArea(c[0]) : ""));
	//	System.out.println("Obstacle size: " + (c[1] != null ? Imgproc.contourArea(c[1]) : ""));

		if (c[0] == null) {
		//	System.out.println("Wall is too small :-)");
			return f;
		}
		if (c[1] == null) {
		//	System.out.println("Obstacle can't be found :-)");
			return f;
		}

		return f;
	}

	private CachedPoint[] cachedPoint = new CachedPoint[] { new CachedPoint(), new CachedPoint(), new CachedPoint(),
			new CachedPoint() };

	private Mat correctWall(Mat frame, Wall w) {

		if (w == null) {
			return frame;
		}

		Point[] srcP = getRightOrder(frame, w.corners);


		// Point with Cache
		for (int i = 0; i < srcP.length; i++) {
			cachedPoint[i].add(srcP[i]);
			srcP[i] = cachedPoint[i].point;
		}


		MatOfPoint2f src = new MatOfPoint2f(srcP);

		RotatedRect box = Imgproc.minAreaRect(src);
		Point[] p = new Point[4];
		box.points(p);

		Point[] points = getRightOrder(frame, p);

		MatOfPoint2f dst = new MatOfPoint2f(points);
		Mat warp = Imgproc.getPerspectiveTransform(src, dst);

		Imgproc.warpPerspective(frame, frame, warp, frame.size());

		wall = new Wall(new MatOfPoint(p));
		wall = wall.substractBorder();

		return frame;

	}

	private Point[] getRightOrder(Mat frame, Point[] points) {

		if (points.length < 4) {
			return points;
		}

		Point[] result = new Point[4];

		Point center = new Point(frame.width() / 2, frame.height() / 2);

		for (Point point : points) {

			if (point.x < center.x && point.y < center.y) {
				result[0] = point;
			}

			if (point.x > center.x && point.y < center.y) {
				result[1] = point;
			}

			if (point.x < center.x && point.y > center.y) {
				result[2] = point;
			}

			if (point.x > center.x && point.y > center.y) {
				result[3] = point;
			}
		}

		return result;
	}

	public void drawWall(Mat frame, Wall wall) {

		if (wall == null) {
			return;
		}

		/*
		 * Point center = new Point(frame.width()/2, frame.height()/2);
		 * 
		 * Imgproc.drawMarker(frame,center, new Scalar(0,0,0));
		 * 
		 * 
		 * double factor = (6.5D) / 175D ;
		 * 
		 * Point[] cor = new Point[wall.points.length];
		 * 
		 * for (int i = 0; i < wall.points.length; i++) {
		 * 
		 * Point wp = wall.points[i];
		 * 
		 * 
		 * double x = wp.x - center.x; double y = wp.y - center.y;
		 * 
		 * double nx = x * factor; double ny = y * factor;
		 * 
		 * 
		 * 
		 * Point corrected = new Point((wp.x) - nx, ( wp.y) - ny);
		 * 
		 * // Imgproc.drawMarker(frame, corrected, new Scalar(0,0,0));
		 * 
		 * cor[i] = corrected;
		 * 
		 * }
		 * 
		 * 
		 * Point p1 = cor[0];
		 * 
		 * for (int i = 1; i < cor.length; i++) { Imgproc.line(frame, new Point(p1.x,
		 * p1.y), new Point(cor[i].x, cor[i].y ), new Scalar(0, 250, 0), 1); p1 =
		 * cor[i]; }
		 * 
		 * Imgproc.line(frame, p1, cor[0], new Scalar(0, 250, 0));
		 * 
		 * 
		 * for (Point point : wall.corners) {
		 * 
		 * Imgproc.drawMarker(frame, point, new Scalar(0,250,250),Imgproc.MARKER_STAR);
		 * 
		 * }
		 * 
		 * 
		 * p1 = wall.points[0];
		 * 
		 * for (int i = 1; i < wall.points.length; i++) { Imgproc.line(frame, p1,
		 * wall.points[i], new Scalar(0, 250, 0)); p1 = wall.points[i]; }
		 * 
		 * Imgproc.line(frame, p1, wall.points[0], new Scalar(0, 250, 0));
		 * 
		 * 
		 * Imgproc.drawMarker(frame, wall.center, new
		 * Scalar(0,100,100),Imgproc.MARKER_STAR);
		 * 
		 * 
		 * 
		 */
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
			Imgproc.drawMarker(frame, new Point(point.x + center.x, point.y + center.y), color, 3, size);
		}

	}

}
