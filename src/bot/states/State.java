package bot.states;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import bot.Bot;
import bot.messages.Messages;
import models.Ball;
import models.Car;
import models.Map;
import models.Wall;
import services.BallService;
import services.CarService;
import services.WallService;

public abstract class State {

	public LocalTime running;
	protected int timeout = 120;

	protected CarService carService;
	protected BallService ballService;
	protected WallService wallService;

	protected Wall wall = null;
	protected Wall obstacle = null;
	protected Car car = null;
	protected List<Ball> balls = null;
	protected Map map;

	public State(CarService carService, BallService ballService, WallService wallService) {
		this.carService = carService;
		this.ballService = ballService;
		this.wallService = wallService;
	}

	public void handle(String message) {

		// We are done and we are ready for new work!
		if (message.equals(Messages.DONE)) {
			running = null;
		}

	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}

	public State process(Mat frame) {

		frame = wallService.locateWallsAndCorrectFrame(frame);

		wall = wallService.getWall();

		obstacle = wallService.getObstacle();

		car = carService.getCar(frame);

		balls = ballService.getBalls(frame);

		if (wall == null || car == null || obstacle == null) {
			System.out.println("SOMETHING IS NULL");
			return this;
		}

		map = new Map(car, frame);
		map.addBalls(balls);
		map.addWall(wall, obstacle);
		map.corrected();
		Mat m = map.getFrame();

		if (running == null) {

			System.out.println("CALCULATING");
			calculate(frame, m);

			running = LocalTime.now();
		}

		// Verify the Timeout
		if (Duration.between(running, LocalTime.now()).getSeconds() > timeout) {
			System.out.println("DRIVING TIMEOUT");
			running = null;
		}

		drawFrame(frame, m);

		return this;
	}

	protected void nextState(State state) {

		System.out.println("Changing state: " + state.getClass().toString());

		Bot.state = state;
	}

	protected Point getPointInCM(Point p) {

		double ratio = (Car.widthInCM / car.width);
		double nx = ((p.x * ratio));
		double ny = ((p.y * ratio));

		return new Point(nx, -ny);

	}

	protected Point getPointFromCM(Point p) {

		double ratio = (car.width / Car.widthInCM);
		float nx = (float) ((p.x * ratio));
		float ny = (float) (((p.y * ratio)));

		return new Point(nx, -ny);

	}

	public abstract void calculate(Mat originalFrame, Mat correctedFrame);

	public abstract void drawFrame(Mat originalFrame, Mat correctedFrame);

	public Mat getFrame() {
		if (map == null) {
			return null;
		}
		return map.getFrame();
	};

	protected boolean isBehindObstacle(Point point, int size) {
		Mat m = map.getFrame().clone();

		WallService.drawWall(m, map.getObstacle(), map.center, new Scalar(250, 250, 250), size);

		Point ballPoint = map.correctPoint(map.getOriginalPoint(point));
		Point carPoint = map.car.center; // changed to map.car.center from map.center
		Point carFrontRight = map.car.frontRight;
		Point carFrontLeft = map.car.frontLeft;
		Point carBackRight = map.car.backRight;
		Point carBackLeft = map.car.backLeft;

		// System.out.println("Ball : " + ballPoint.toString());
		// System.out.println("Car : " + carPoint.toString());

		if (carPoint.x > ballPoint.x) {

			double a = (ballPoint.y - carPoint.y) / (ballPoint.x - carPoint.x);
			double b = (carPoint.y - a * carPoint.x);

			for (int x = (int) ballPoint.x; x < (int) carPoint.x - 5; x++) {
				double y = a * x + b;
				// System.out.println("1: Color of point (" + x + ", " + y + "): " + new
				// Scalar(m.get((int) y, (int) x)).toString());

				if (new Scalar(m.get((int) (y + carFrontRight.y), (int) (x + carFrontRight.x)))
						.equals(new Scalar(250, 250, 250))) {
					return true;
				}
				if (new Scalar(m.get((int) (y + carFrontLeft.y), (int) (x + carFrontLeft.x)))
						.equals(new Scalar(250, 250, 250))) {
					return true;
				}
				if (new Scalar(m.get((int) (y + carBackRight.y), (int) (x + carBackRight.x)))
						.equals(new Scalar(250, 250, 250))) {
					return true;
				}
				if (new Scalar(m.get((int) (y + carBackLeft.y), (int) (x + carBackLeft.x)))
						.equals(new Scalar(250, 250, 250))) {
					return true;
				}

			}

		} else if (ballPoint.x > carPoint.x) {

			double a = (carPoint.y - ballPoint.y) / (carPoint.x - ballPoint.x);
			double b = (ballPoint.y - a * ballPoint.x);

			for (int x = (int) carPoint.x; x < (int) ballPoint.x - 5; x++) {
				double y = a * x + b;
				// System.out.println("2: Color of point (" + x + ", " + y + "): " + new
				// Scalar(m.get((int) y, (int) x)).toString());
				if (new Scalar(m.get((int) (y + carFrontRight.y), (int) (x + carFrontRight.x)))
						.equals(new Scalar(250, 250, 250))) {
					return true;
				}
				if (new Scalar(m.get((int) (y + carFrontLeft.y), (int) (x + carFrontLeft.x)))
						.equals(new Scalar(250, 250, 250))) {
					return true;
				}
				if (new Scalar(m.get((int) (y + carBackRight.y), (int) (x + carBackRight.x)))
						.equals(new Scalar(250, 250, 250))) {
					return true;
				}
				if (new Scalar(m.get((int) (y + carBackLeft.y), (int) (x + carBackLeft.x)))
						.equals(new Scalar(250, 250, 250))) {
					return true;
				}
			}

		} else {

		}

		return false;
	}

	protected boolean isBehindObstacle(Point point) {
		Mat m = map.getFrame().clone();

		WallService.drawWall(m, map.getObstacle(), map.center, new Scalar(250, 250, 250), (int) (car.width * 3));

		Point ballPoint = map.correctPoint(map.getOriginalPoint(point));
		Point carPoint = map.car.center; // changed to map.car.center from map.center

		// System.out.println("Ball : " + ballPoint.toString());
		// System.out.println("Car : " + carPoint.toString());

		if (carPoint.x > ballPoint.x) {

			double a = (ballPoint.y - carPoint.y) / (ballPoint.x - carPoint.x);
			double b = (carPoint.y - a * carPoint.x);

			for (int x = (int) ballPoint.x; x < (int) carPoint.x - 5; x++) {
				double y = a * x + b;
				// System.out.println("1: Color of point (" + x + ", " + y + "): " + new
				// Scalar(m.get((int) y, (int) x)).toString());

				if (new Scalar(m.get((int) (y), (int) (x))).equals(new Scalar(250, 250, 250))) {
					return true;
				}

			}

		} else if (ballPoint.x > carPoint.x) {

			double a = (carPoint.y - ballPoint.y) / (carPoint.x - ballPoint.x);
			double b = (ballPoint.y - a * ballPoint.x);

			for (int x = (int) carPoint.x; x < (int) ballPoint.x - 5; x++) {
				double y = a * x + b;
				// System.out.println("2: Color of point (" + x + ", " + y + "): " + new
				// Scalar(m.get((int) y, (int) x)).toString());
				if (new Scalar(m.get((int) (y), (int) (x))).equals(new Scalar(250, 250, 250))) {
					return true;
				}
			}

		} else {

		}

		return false;
	}

	protected boolean isBallOutOfSector(Point point) {
		
		Point ballPoint = map.getOriginalPoint(point);
		
		// car is on the right of center
		if (car.center.x >= map.center.x) {

			// car is in the upper half
			if (car.center.y >= map.center.y) {
				// hvis bold. x <= center.x eller bold.y <= center.y returner true
				if (ballPoint.x <= map.center.x || ballPoint.x <= map.center.y) {
					return true;
				} else {
					return false;
				}
			}
			// car is in the lower half
			else {
				// hvis bold. x <= center.x eller bold.y > center.y returner true
				if (ballPoint.x <= map.center.x || ballPoint.x > map.center.y) {
					return true;
				} else {
					return false;
				}
			}

		}

		// car is on the left of center
		else {

			// car is in the upper half
			if (car.center.y >= map.center.y) {
				// hvis bold. x > center.x eller bold.y <= center.y returner true
				if (ballPoint.x > map.center.x || ballPoint.x <= map.center.y) {
					return true;
				} else {
					return false;
				}
			}
			// car is in the lower half
			else {
				// hvis bold. x > center.x eller bold.y > center.y returner true
				if (ballPoint.x > map.center.x || ballPoint.x > map.center.y) {
					return true;
				} else {
					return false;
				}
			}

		}

	}

}
