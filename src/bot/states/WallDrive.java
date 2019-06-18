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

public class WallDrive extends State {

	private Ball targetBall;
	private Point target;

	public WallDrive(CarService carService, BallService ballService, WallService wallService) {
		super(carService, ballService, wallService);
	}

	@Override
	public void calculate(Mat originalFrame, Mat correctedFrame) {

		System.out.println("WallDrive has begun!");

		if (map.balls.size() == 0) {
			System.out.println("THERE IS NO BALLS IN MAP TO COLLECT!!!");
			nextState(new ObstacleDrive(carService, ballService, wallService));
			return;
		}

		double minDistance = car.width;

		Mat m = map.getFrame().clone();

		WallService.drawWall(m, map.getWall(), map.center, new Scalar(250, 250, 250), (int) (car.width * 4));

		// filter balls
		ArrayList<Ball> tb = new ArrayList<Ball>();
		// Remove close balls and balls not close to border
		for (Ball b : map.balls) {

			double d = Math.sqrt(Math.pow(b.point.x, 2) + Math.pow(b.point.y, 2));

			if (d <= minDistance) {
				System.out.println("Removed Ball - to close to robot");
			} else if (!new Scalar(m.get((int) (b.point.y + map.center.y), (int) (b.point.x + map.center.x)))
					.equals(new Scalar(250, 250, 250))) {

				System.out
						.println(new Scalar(m.get((int) (b.point.y + map.center.y), (int) (b.point.x + map.center.x))));
				System.out.println("Removed Ball - to close to border");
			} else if (isBehindObstacle(b)) {
				System.out.println("Removed Ball - hiding behind obstacle");
			} else {

				boolean valid = true;
				// Remove stuff to close to border
				for (Point p : map.getWall().corners) {

					d = Math.sqrt(Math.pow(p.x - b.point.x, 2) + Math.pow(p.x - b.point.y, 2));

					if (d < car.width * 3) {
						valid = false;
						break;
					}
				}

				if (valid) {
					tb.add(b);
				} else {
					System.out.println("Removing Ball - Not close to wall");
				}
			}
		}

		if (tb.size() == 0) {
			System.out.println("THERE IS NO BALLS IN MAP TO COLLECT2!!!");
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

		// locate the correct Point against the tanget of the Wall

		// We need to locate which wall the ball is against.

		Point[] corners = wallService.getRightOrder(originalFrame, wall.corners);

		// Top ?
		Point p1 = corners[0];
		Point p2 = corners[1];

		double dist = getDist(p1, p2, targetBall.point);
		double distance = car.width * 4;

		if (dist < car.width * 1.5) {
			target = new Point(targetBall.point.x, targetBall.point.y + distance);
		}

		// Right ?
		p1 = corners[1];
		p2 = corners[3];
		
		// Dist from wall
		dist = getDist(p1, p2, targetBall.point);

		if (dist < car.width * 1.5) {
			target = new Point(targetBall.point.x - distance, targetBall.point.y);
		}

		// But ?
		p1 = corners[2];
		p2 = corners[3];

		dist = getDist(p1, p2, targetBall.point);

		if (dist < car.width * 1.5) {
			target = new Point(targetBall.point.x, targetBall.point.y - distance);
		}

		// Left ?
		p1 = corners[2];
		p2 = corners[0];

		dist = getDist(p1, p2, targetBall.point);

		if (dist < car.width * 1.5) {
			target = new Point(targetBall.point.x + distance, targetBall.point.y);
		}

		if (target == null) {
			System.out.println("THERE IS NO BALL AT WALL ??!!!");
			nextState(new ObstacleDrive(carService, ballService, wallService));
			return;
		}

		// We are at the point!
		if (getDist(car.center, target) < car.width) {
			System.out.println("We are ready to collect the Ball");
			WallCollect nextState = new WallCollect(carService, ballService, wallService);
			nextState.setTarget(targetBall);
			nextState(nextState);
			return;

		} else {

			Point p = map.correctPoint(target);
			p.x -= map.center.x;
			p.y -= map.center.y;

			System.out.println("Driving to  point: " + target.toString());

			Point targetCM = getPointInCM(p);

			ActionList list = new ActionList();

			System.out.println("Driving to: " + targetCM.x + " : " + targetCM.y);
			list.add(new WayPointAction(targetCM.x, targetCM.y, 0.70F, 0.4F));

			if (!Bot.test)
				Connection.SendActions(list);

		}

		/*
		 * 
		 * double d = Math.sqrt(Math.pow(ball.point.x, 2) + Math.pow(ball.point.y, 2));
		 * double dratio = (d - (car.width * 3)) / d;
		 * 
		 * Point destination = new Point(ball.point.x * dratio, ball.point.y * dratio);
		 * 
		 * target = map.getOriginalPoint(destination);
		 * 
		 * // We need to verify if we are inside the Circle
		 * 
		 * Mat tmp = correctedFrame.clone(); Imgproc.circle(tmp, new Point(ball.point.x
		 * + map.center.x, ball.point.y + map.center.y), (int) (car.width * 4), new
		 * Scalar(200, 200, 200), -1);
		 * 
		 * if (new Scalar(tmp.get((int) (map.center.y), (int)
		 * (map.center.x))).equals(new Scalar(200, 200, 200))) {
		 * 
		 * // THE car is inside the circle
		 * 
		 * System.out.println("What up bitches");
		 * 
		 * EasyCollect nextState = new EasyCollect(carService, ballService,
		 * wallService); nextState.setTarget(targetBall); nextState(nextState); return;
		 * }
		 * 
		 * System.out.println("Driving to  point: " + target.toString());
		 * 
		 * Point targetCM = getPointInCM(destination);
		 * 
		 * ActionList list = new ActionList();
		 * 
		 * System.out.println("Driving to: " + targetCM.x + " : " + targetCM.y);
		 * list.add(new WayPointAction(targetCM.x, targetCM.y, 0.70F, 0.4F));
		 * 
		 * if (!Bot.test) Connection.SendActions(list);
		 * 
		 * 
		 */

	}

	private double getDist(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
	}

	private double getDist(Point p1, Point p2, Point p3) {
		double a = (p2.y - p1.y) / (p2.x - p1.x);
		double b = p1.y - a * p1.x;

		double dist = Math.abs(a * p3.x + b - p3.y) / Math.sqrt(Math.pow(a, 2) + 1);

		return dist;
	}

	@Override
	public void drawFrame(Mat originalFrame, Mat correctedFrame) {

		map.drawWall(new Scalar(250, 250, 250), (int) (10));

		if (target != null) {
			Imgproc.line(correctedFrame, map.center, map.correctPoint(target), new Scalar(88, 214, 141));
			Imgproc.line(originalFrame, car.center, target, new Scalar(88, 214, 141));

		}

		if (targetBall != null) {

			Imgproc.circle(correctedFrame, map.correctPoint(targetBall.point), 10, new Scalar(200, 200, 200), -1);
			Imgproc.circle(correctedFrame, map.correctPoint(targetBall.point), (int) (car.width * 2.5),
					new Scalar(200, 200, 200), 10);

			Imgproc.circle(originalFrame, targetBall.point, 10, new Scalar(200, 200, 200), -1);
			Imgproc.circle(originalFrame, targetBall.point, (int) (car.width * 2.5), new Scalar(200, 200, 200), 10);

		}
	}
}