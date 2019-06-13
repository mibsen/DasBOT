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
import models.BallSettings;

public class BallService {

	private BallSettings settings;

	public BallService(BallSettings settings) {
		this.settings = settings;

	}

	public List<Ball> getBalls(Mat f) {
		Mat frame = getBallFrame(f);
		return getBallsFromFrame(frame);
	}

	public Mat getBallFrame(Mat f) {

		double hueStart = settings.image.hue.start;
		double hueStop = settings.image.hue.stop;
		double saturationStart = settings.image.saturation.start;
		double saturationStop = settings.image.saturation.stop;
		double valueStart = settings.image.value.start;
		double valueStop = settings.image.value.stop;
		double blur = settings.image.blur;

		Mat frame = f.clone();

		// remove some noise
		if (blur > 0) {
			Imgproc.blur(frame, frame, new Size(blur, blur));
		}
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
				if (area > settings.max || area < settings.min) {
					continue;
				}

				RotatedRect box = Imgproc.minAreaRect(new MatOfPoint2f(c.toArray()));

				balls.add(new Ball(box.center, area));
			}
		}

		return balls;

	}

	public void drawBalls(Mat frame, List<Ball> balls) {

		for (Ball ball : balls) {
			Imgproc.drawMarker(frame, ball.point, new Scalar(250, 0, 0));
		}

	}

	public static void drawBalls(Mat frame, List<Ball> balls, Point center) {

		for (Ball ball : balls) {
			Imgproc.drawMarker(frame, new Point(ball.point.x + center.x, ball.point.y + center.y),
					new Scalar(255, 0, 255));
			Imgproc.circle(frame, ball.point, 50, new Scalar(250, 250, 250));
		}

	}

}
