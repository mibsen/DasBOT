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

import models.Car;
import models.ImageSettings;
import models.Wall;
import models.WallSettings;

public class WallService {

	private WallSettings settings;

	public WallService(WallSettings settings) {
		this.settings = settings;
	}

	public Wall getWall(Mat f) {

		Mat frame = getWallFrame(f);

		return getWallsFromFrame(frame);

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
	public Wall getWallsFromFrame(Mat wallFrame) {
		// TODO Auto-generated method stub

		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();

		// Imgproc.Canny(wallFrame, wallFrame, 300, 1000);
		Imgproc.Canny(wallFrame, wallFrame, settings.threshold1, settings.threshold2);

		// find contours
		Imgproc.findContours(wallFrame, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

		if (contours.size() == 0) {
			System.out.println("COULD NOT LOCATE WALL");
			return null;
		}

		MatOfPoint c = contours.get(0);

		// Potentielt noget filter!
		// double area = Imgproc.contourArea(c);

		Wall w = new Wall(c);

		return w;

	}

	public void drawWall(Mat frame, Wall wall) {

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
