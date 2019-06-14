package bot.states;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import bot.Bot;
import bot.Connection;
import bot.actions.ActionList;
import bot.actions.WayPointAction;
import models.Ball;
import services.BallService;
import services.CarService;
import services.WallService;

public class EasyDrive extends State {

	private Ball targetBall;
	private Point target;

	public EasyDrive(CarService carService, BallService ballService, WallService wallService) {
		super(carService, ballService, wallService);
	}

	@Override
	public void calculate(Mat originalFrame, Mat correctedFrame) {

		System.out.println("EasyDrive has begun!");

		if (map.balls.size() == 0) {
			System.out.println("THERE IS NO BALLS IN MAP TO COLLECT!!!");
			nextState(new ObstacleDrive(carService, ballService, wallService));
			return;
		}

		double minDistance = car.width * 1.5;

		map.drawWall(new Scalar(250, 250, 250), (int) (car.width * 2));
		WallService.drawWall(map.getFrame(), map.getObstacle(), map.center, new Scalar(250, 250, 250), (int) (car.width * 3));
		
		Mat m = map.getFrame();

		// filter balls
		ArrayList<Ball> tb = new ArrayList<Ball>();
		// Remove close balls and balls close to border
		for (Ball b : map.balls) {

			double d = Math.sqrt(Math.pow(b.point.x, 2) + Math.pow(b.point.y, 2));

			if (d <= minDistance) {
				// System.out.println("Removed Ball - to close to robot");
			} else if (new Scalar(m.get((int) (b.point.y + map.center.y), (int) (b.point.x + map.center.x)))
					.equals(new Scalar(250, 250, 250))) {
				// System.out.println("Removed Ball - to close to border");
			} else if (isBehindObstacle(b, m)) {
				// System.out.println("Removed Ball - hiding behind obstacle");
			} else {
				tb.add(b);
			}
		}

		if (tb.size() == 0) {
			System.out.println("THERE IS NO BALLS IN TB TO COLLECT!!!");
			nextState(new ObstacleDrive(carService, ballService, wallService));
			return;
		}

		Collections.sort(tb, new Comparator<Ball>() {

			@Override
			public int compare(Ball o1, Ball o2) {

				double d1 = Math.sqrt(Math.pow(o1.point.x, 2) + Math.pow(o1.point.y, 2));
				double d2 = Math.sqrt(Math.pow(o2.point.x, 2) + Math.pow(o2.point.y, 2));

				if (d1 == d2) {
					return 0;
				}
				return d1 > d2 ? 1 : -1;
			}
		});

		Ball ball = tb.get(0);

		targetBall = new Ball(map.getOriginalPoint(ball.point), ball.area);

		// Calculate target

		double d = Math.sqrt(Math.pow(ball.point.x, 2) + Math.pow(ball.point.y, 2));
		double dratio = (d - (car.width * 2)) / d;

		Point destination = new Point(ball.point.x * dratio, ball.point.y * dratio);

		target = map.getOriginalPoint(destination);

		// We need to verify if we are inside the Circle

		Mat tmp = correctedFrame.clone();
		Imgproc.circle(tmp, new Point(ball.point.x + map.center.x, ball.point.y + map.center.y),
				(int) (car.width * 2.5), new Scalar(200, 200, 200), -1);

		if (new Scalar(tmp.get((int) (map.center.y), (int) (map.center.x))).equals(new Scalar(200, 200, 200))) {

			// THE car is inside the circle

			System.out.println("What up bitches");

			EasyCollect nextState = new EasyCollect(carService, ballService, wallService);
			nextState.setTarget(targetBall);
			nextState(nextState);
			return;
		}

		System.out.println("Driving to  point: " + target.toString());

		Point targetCM = getPointInCM(destination);

		ActionList list = new ActionList();

		System.out.println("Driving to: " + targetCM.x + " : " + targetCM.y);
		list.add(new WayPointAction(targetCM.x, targetCM.y, 0.70F,0.4F));

		if (!Bot.test)
			Connection.SendActions(list);

	}

	private boolean isBehindObstacle(Ball ball, Mat m) {
		Point ballPoint = map.correctPoint(map.getOriginalPoint(ball.point));
		Point carPoint = map.center;

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

	@Override
	public void drawFrame(Mat originalFrame, Mat correctedFrame) {

		map.drawWall(new Scalar(250, 250, 250), (int) (car.width * 2));

		correctedFrame = map.getFrame();

		if (targetBall != null) {

			Imgproc.line(correctedFrame, map.center, map.correctPoint(targetBall.point), new Scalar(88, 214, 141));
			Imgproc.circle(correctedFrame, map.correctPoint(targetBall.point), (int) (car.width * 2.5),
					new Scalar(200, 200, 200), -1);
			Imgproc.line(originalFrame, car.center, targetBall.point, new Scalar(88, 214, 141));
		}
	}
}