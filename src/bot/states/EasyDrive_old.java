package bot.states;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import bot.Bot;
import bot.Connection;
import bot.actions.ActionList;
import bot.actions.StartCollectionAction;
import bot.actions.StopCollectionAction;
import bot.actions.WaitAction;
import bot.actions.WayPointAction;
import bot.messages.Messages;
import models.Ball;
import models.Car;
import models.Map;
import models.Wall;
import services.BallService;
import services.CarService;
import services.WallService;

public class EasyDrive_old extends State {

	private CarService carService;
	private BallService ballService;
	private WallService wallService;
	private Map map;
	private boolean waitForNextFrame = false;
	private Ball activeBall = null;
	private long timeout = 120;
	private Point correctPoint;

	public EasyDrive_old(CarService carService, BallService ballService, WallService wallService) {
		this.carService = carService;
		this.ballService = ballService;
		this.wallService = wallService;
	}

	@Override
	public State process(Mat frame) {

		// We need car Position and Wall

		Wall wall = null;
		Wall obstacle = null;
		Car car = null;
		List<Ball> balls = null;
		ArrayList<Ball> drawingBalls = null;

		frame = wallService.locateWallsAndCorrectFrame(frame);
		wall = wallService.getWall();
		obstacle = wallService.getObstacle();

		car = carService.getCar(frame);

		balls = ballService.getBalls(frame);

		if (wall == null || car == null || obstacle == null) {
			return this;
		}

		map = new Map(car, frame);
		map.addBalls(balls);
		map.addWall(wall, obstacle);

		map.corrected();

		Mat m = map.getFrame();

		map.drawCar(new Scalar(0, 250, 250), 1);
		map.drawWall(new Scalar(250, 250, 250), (int) (car.width * 2));
		
		if (running == null) {
			
			System.out.println("Easy collect has begun!");
			
			isDone = false;

			// Locate A Ball

			if (map.balls.size() == 0) {
				System.out.println("THERE IS NO BALLS IN MAP TO COLLECT!!!");
				isDone = true;
				nextState = Bot.driveObstacleState;
				return this;
			}

			// Ball ball = null;

			// Close - Not to close
			// We do not want balls to close!
			double minDistance = car.width * 2;

			ArrayList<Ball> tb = new ArrayList<Ball>();

			// Remove close balls and balls close to border
			for (Ball b : map.balls) {

				double d = Math.sqrt(Math.pow(b.point.x, 2) + Math.pow(b.point.y, 2));

				if (d <= minDistance) {
					//System.out.println("Removed Ball - to close to robot");
				} else if (new Scalar(m.get((int) (b.point.y + map.center.y), (int) (b.point.x + map.center.x)))
						.equals(new Scalar(250, 250, 250))) {
					//System.out.println("Removed Ball - to close to border");
				} else if (isBehindObstacle(b, m)) {
					//System.out.println("Removed Ball - hiding behind obstacle");
				} else {
					tb.add(b);
				}
			}

			if (tb.size() == 0) {

				System.out.println("THERE IS NO BALLS IN TB TO COLLECT!!!");
				isDone = true;
				nextState = Bot.driveObstacleState;				
				return this;
			}

			running = LocalTime.now();

			// Sort Balls by distance to car

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
						
			correctPoint = ball.point.clone();
			
			double d = Math.sqrt(Math.pow(correctPoint.x, 2) + Math.pow(correctPoint.y, 2));
			double dratio = (d - (car.width * 2)) / d;
			
			
			correctPoint.x = correctPoint.x * dratio;
			correctPoint.y = correctPoint.y * dratio;
			
			activeBall = new Ball(map.getOriginalPoint(correctPoint), ball.area);

			Imgproc.circle(m, new Point(ball.point.x + map.center.x , ball.point.y + map.center.y), (int) (car.width * 2.5), new Scalar(200,200,200),-1);

			if(new Scalar(m.get((int) (map.center.y), (int) (map.center.x)))
					.equals(new Scalar(200, 200, 200))) {
				System.out.println("What up bitches");
				Bot.easyCollectState.currentBall = new Ball(map.getOriginalPoint(ball.point), ball.area);;
				isDone = true;	
				nextState = Bot.easyCollectState;
				return this;
			} else {
				Bot.easyCollectState.currentBall = null;
			}

			
			System.out.println("Driving to  point: " + correctPoint.toString());

			
			ActionList list = new ActionList();			
			list.add(new StartCollectionAction());

			Point d = getPointInCM(target);
			
			System.out.println("Driving to: " + d.x + " : " + d.y);
			list.add(new WayPointAction(d.x, d.y, 0.60F));
			list.add(new StopCollectionAction());

			if (!Bot.test)
				Connection.SendActions(list);

		}

		// Verify the Timeout
		if (Duration.between(running, LocalTime.now()).getSeconds() > timeout) {

			// We have timed out
			System.out.println("DRIVING TIMEOUT");

			// TODO: Send HARDBREAK ACTION

			running = null;

			return this;
		}
		

		
		//for (Ball b : drawingBalls) {
		//	Imgproc.line(m, map.center, map.correctPoint(map.getOriginalPoint(b.point)), new Scalar(250, 250, 250));
		//}
		
		Ball first = activeBall;
		


		Imgproc.circle(m, map.correctPoint(first.point), (int) (car.width * 2.5), new Scalar(200,200,200),-1);
		Imgproc.line(m, map.center, map.correctPoint(first.point), new Scalar(88, 214, 141));
		Imgproc.line(frame, car.center, first.point, new Scalar(88, 214, 141));

		return this;
	}

	private boolean isBehindObstacle(Ball ball, Mat m) {
		Point ballPoint = map.correctPoint(map.getOriginalPoint(ball.point));
		Point carPoint = map.center;

		//System.out.println("Ball : " + ballPoint.toString());
		//System.out.println("Car : " + carPoint.toString());
		
		if (carPoint.x > ballPoint.x) {
			
			double a = (ballPoint.y - carPoint.y) / (ballPoint.x - carPoint.x);
			double b = (carPoint.y - a * carPoint.x);

			for (int x = (int) ballPoint.x; x < (int) carPoint.x-5; x++) {
				double y = a * x + b;
				//System.out.println("1: Color of point (" + x + ", " + y + "): " + new Scalar(m.get((int) y, (int) x)).toString());
				if (new Scalar(m.get((int) (y), (int) (x)))
						.equals(new Scalar(250, 250, 250))) {
					return true;
				}
			}

		} else if (ballPoint.x > carPoint.x) {
			
			double a = (carPoint.y - ballPoint.y) / (carPoint.x - ballPoint.x);
			double b = (ballPoint.y - a * ballPoint.x);

			for (int x = (int) carPoint.x; x < (int) ballPoint.x-5; x++) {
				double y = a * x + b;
				//System.out.println("2: Color of point (" + x + ", " + y + "): " + new Scalar(m.get((int) y, (int) x)).toString());
				if (new Scalar(m.get((int) (y), (int) (x)))
						.equals(new Scalar(250, 250, 250))) {
					return true;
				}
			}

		} 
		else {

		}

		return false;
	}

	@Override
	public Mat getFrame() {

		if (map == null) {
			return null;
		}
		return map.getFrame();
	}

	public void handle(String message) {

		// We are done and we are ready for new work!
		if (message.equals(Messages.DONE)) {
			activeBall = null;
			running = null;
		}
	}

}