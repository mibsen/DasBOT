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

import models.Car;
import models.CarSettings;

public class CarService {

	private CarSettings settings;
	public boolean debug;

	public CarService(CarSettings settings) {
		this.settings = settings;
	}

	public Car getCar(Mat f) {

		Mat frame = getCarFrame(f);

		return getCarFromFrame(frame);
	}

	public Mat getCarFrame(Mat f) {

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

	public Car getCarFromFrame(Mat carFrame) {

		// init
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();

		// find contours
		Imgproc.findContours(carFrame, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

		// if any contour exist...
		if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {

			int n = 1;

			Point front = null;
			Point back = null;

			for (MatOfPoint c : contours) {

				double area = Imgproc.contourArea(c);

				if (debug) {
					System.out.println("Car_" + n + ": " + area);
					n++;
				}

				if ((area > settings.back.start && area < settings.back.stop)) {
					RotatedRect box = Imgproc.minAreaRect(new MatOfPoint2f(c.toArray()));
					Point cordinate = new Point(box.center.x, box.center.y);
					back = cordinate;
				} else if ((area > settings.front.start && area < settings.front.stop)) {
					RotatedRect box = Imgproc.minAreaRect(new MatOfPoint2f(c.toArray()));
					Point cordinate = new Point(box.center.x, box.center.y);
					front = cordinate;
				} else {
					continue;
				}

			}

			if (front != null && back != null) {

				Car car = new Car(front, back);
				return car;
			} else {
				System.out.println("COULD NOT LOCATE CAR!");
			}
		}
		return null;
	}

	public static void drawCar(Mat frame, Car car) {

		drawCar(frame, car, new Point(0, 0));

	}

	public static void drawCar(Mat frame, Car car, Point center, Scalar color, int size) {

		if (car == null) {
			return;
		}

		Imgproc.drawMarker(frame, new Point(car.front.x + center.x, car.front.y + center.y), color,1, size);
		Imgproc.drawMarker(frame, new Point(car.back.x + center.x, car.back.y + center.y), color,1, size);
		Imgproc.drawMarker(frame, new Point(car.center.x + center.x, car.center.y + center.y), color,1, size);
		Imgproc.line(frame, new Point(car.front.x + center.x, car.front.y + center.y),
				new Point(car.back.x + center.x, car.back.y + center.y), color, size);
	}

	public static void drawCar(Mat frame, Car car, Point center) {

		drawCar(frame, car, center, new Scalar(0, 250, 250), 2);
	}

}
