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
import bot.actions.TurnAction;
import bot.actions.WayPointAction;
import models.Ball;
import services.BallService;
import services.CarService;
import services.WallService;

public class ObstacleDrive extends State {

	private Ball targetBall;
	private Point target;

	ArrayList<Ball> temp = new ArrayList<Ball>();

	public ObstacleDrive(CarService carService, BallService ballService, WallService wallService) {
		super(carService, ballService, wallService);
	}

	@Override
	public void calculate(Mat originalFrame, Mat correctedFrame) {

		System.out.println("ObstacleDrive has begun!");

		if (targetBall == null) {

			if (map.balls.size() == 0) {
				System.out.println("THERE IS NO BALLS IN MAP TO COLLECT!!!");
				nextState(new CirculateDrive(carService, ballService, wallService));
				return;
			}

			Mat m = map.getFrame().clone();

			WallService.drawWall(m, map.getObstacle(), map.center, new Scalar(250, 250, 250), (int) (Math.sqrt(Math.pow(map.car.backRight.x, 2) + Math.pow(map.car.backRight.y, 2))) * 2);

			// filter balls
			ArrayList<Ball> tb = new ArrayList<Ball>();
			// Remove close balls and balls not close to border
			for (Ball b : map.balls) {

				double d = Math.sqrt(Math.pow(b.point.x, 2) + Math.pow(b.point.y, 2));

				if (!new Scalar(m.get((int) (b.point.y + map.center.y), (int) (b.point.x + map.center.x)))
						.equals(new Scalar(250, 250, 250))) {
					System.out.println("Removed Ball - to far away from border");
				}  else if (isBallOutOfSector(b.point)){
					 System.out.println("Removed Ball - out of sector");
				}
				else {
					tb.add(b);
				}
			}

			if (tb.size() == 0) {
				System.out.println("THERE IS NO BALLS IN MAP TO COLLECT!!!");
				nextState(new CirculateDrive(carService, ballService, wallService));
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
			temp = tb;

			targetBall = new Ball(map.getOriginalPoint(ball.point), ball.area);


			Point obstacleCenter = obstacle.center;
			Point np = new Point(targetBall.point.x - obstacleCenter.x, targetBall.point.y - obstacleCenter.y);
			
			double distanceCenterToBall = Math.sqrt(Math.pow(np.x, 2) + Math.pow(np.y, 2));
			double distanceCenterToTarget = distanceCenterToBall + map.car.pickFront.x * 1.2;
			double ratio = distanceCenterToTarget/distanceCenterToBall;
			
			target = new Point(np.x * ratio, np.y * ratio);
			target.x += obstacle.center.x;
			target.y += obstacle.center.y;			
			
			Point dd = map.correctPoint(target);
			dd.x -= map.center.x;
			dd.y -= map.center.y;
			
			if(isBehindObstacle(dd,  car.width.intValue())) {
				System.out.println("The Target is behind the obstacle!");
				nextState(new CirculateDrive(carService, ballService, wallService));
				return;
			}
			
			//double ratio = (distToPointFromBall + distToBallFromCenter) / distToBallFromCenter;
			
			//target = new Point((obstacleCenter.x - targetBall.point.x) * ratio + map.center.x, (obstacleCenter.y - map.center.y) * ratio + map.center.y); 

		}

		// We are at the point!
		if (getDist(car.center, target) < car.width) {
			ObstacleCollect nextState = new ObstacleCollect(carService, ballService, wallService);
			nextState.setTarget(targetBall);
			nextState(nextState);
			return;

		} else {
			

			Point p = map.correctPoint(target);
			p.x -= map.center.x;
			p.y -= map.center.y;

			// Verify VINKEL!
			double deg = -Math.toDegrees(Math.atan2(p.y, p.x));
			
			if(Math.abs(deg) > 5) {
				
				//System.out.println("correcting moving " + deg +" Deg");
				
				ActionList list = new ActionList();
				list.add(new TurnAction((long) deg));

				if (!Bot.test)
					Connection.SendActions(list);		
			
				return;
			}
			
			//System.out.println("Driving to  point: " + target.toString());

			Point targetCM = getPointInCM(p);

			ActionList list = new ActionList();

			//System.out.println("Driving to: " + targetCM.x + " : " + targetCM.y);
			list.add(new WayPointAction(targetCM.x, targetCM.y, 0.90F, 0.6F));

			if (!Bot.test)
				Connection.SendActions(list);

		}

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
		Imgproc.drawMarker(originalFrame, obstacle.center, new Scalar(150, 0, 100));

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