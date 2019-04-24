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

import models.Ball;
import models.ImageSettings;

public class BallService {

	private ImageSettings settings;

	private int minArea;
	private int maxArea;

	public BallService(ImageSettings settings, int minArea, int maxArea) {
		this.settings = settings;
		this.minArea = minArea;
		this.maxArea = maxArea;
	}

	public List<Ball> getBalls(Mat f) {
		Mat frame = getBallFrame(f);
		return getBallsFromFrame(frame);
	}

	public Mat getBallFrame(Mat f) {

		/*
		 * double hueStart = 0 ; double hueStop = 180; double saturationStart = 0 ;
		 * double saturationStop = 63 ; double valueStart = 205; double valueStop = 255;
		 */

		double hueStart = settings.hueStart;
		double hueStop = settings.hueStop;
		double saturationStart = settings.saturationStart;
		double saturationStop = settings.saturationStop;
		double valueStart = settings.valueStart;
		double valueStop = settings.valueStop;
		double blur = settings.blur;

		Mat frame = f.clone();

		// remove some noise
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

	public List<Ball> getBallsFromFrame(Mat ballFrame) {

		// init
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		ArrayList<Ball> balls = new ArrayList<Ball>();

		// find contours
		Imgproc.findContours(ballFrame, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

		// if any contour exist...
		if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {

			for (MatOfPoint c : contours) {

				double area = Imgproc.contourArea(c);
				if (area > maxArea || area < minArea) {
					// if( area > 120 || area < 30) {
					// if(area > 650 || area < 450 ) {
					continue;
				}

				RotatedRect box = Imgproc.minAreaRect(new MatOfPoint2f(c.toArray()));
				// Imgproc.boxPoints(box, c);

				balls.add(new Ball(box.center));

				// Imgproc.drawMarker(frame, new Point(box.center.x, box.center.y), new
				// Scalar(250, 0, 0));

			}

			// updateImageView(morphImage, hierarchy);

			// for each contour, display it in blue
			for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {

				// Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0));
			}
		}

		return balls;

	}

	public void drawBalls(Mat frame, List<Ball> balls) {

		for (Ball ball : balls) {
			Imgproc.drawMarker(frame,ball.point, new Scalar(250, 0, 0));
		}

	}
	
	public static void drawBalls(Mat frame, List<Ball> balls, Point center) {

		for (Ball ball : balls) {
			Imgproc.drawMarker(frame, new Point(ball.point.x + center.x, ball.point.y + center.y), new Scalar(255, 0, 255));
		}

	}

}
