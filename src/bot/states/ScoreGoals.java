package bot.states;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
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

public class ScoreGoals extends State {

	private CarService carService;
	private BallService ballService;
	private WallService wallService;
	private Map map;
	private Point[] waypoints;
	private Point nearestWaypoint;
	private Point finnishPoint;
	private long timeout = 120;
	private Point goalPoint;

	public ScoreGoals(CarService carService, BallService ballService, WallService wallService) {

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

		map.drawCar(new Scalar(0, 250, 250), 1);
		map.drawBalls(new Scalar(0, 250, 250), 1, 5);
		map.drawWall(new Scalar(250, 250, 250), (int) (car.width * 3));

		Mat m = map.getFrame();

		Point corner1 = null;
		Point corner2 = null;
		Point corner3 = null;
		Point corner4 = null;

		for (Point corner : wall.corners) {
			if (corner.x < car.center.x && corner.y < car.center.y) {
				corner1 = corner.clone();
			} else if (corner.x > car.center.x && corner.y < car.center.y) {
				corner2 = corner.clone();
			} else if (corner.x < car.center.x && corner.y > car.center.y) {
				corner3 = corner.clone();
			} else if (corner.x > car.center.x && corner.y > car.center.y) {
				corner4 = corner.clone();
			}
		}

		double width = ((corner2.x - corner1.x) + (corner4.x - corner3.x)) / 2;
		double height = ((corner3.y - corner1.y) + (corner4.y - corner2.y)) / 2;

		waypoints = new Point[4];
		waypoints[0] = new Point(width / 4 + corner1.x, height / 4 + corner1.y);
		waypoints[1] = new Point(width * 3 / 4 + corner1.x, height / 4 + corner1.y);
		waypoints[2] = new Point(width * 3 / 4 + corner1.x, height * 3 / 4 + corner1.y);
		waypoints[3] = new Point(width / 4 + corner1.x, height * 3 / 4 + corner1.y);
		
		finnishPoint = new Point(width * 4 / 5 + corner1.x, height / 2 + corner1.y);
		goalPoint = new Point(width + corner1.x, height / 2 + corner1.y);
		
		Imgproc.drawMarker(frame, goalPoint, new Scalar(150, 200, 250));
		Imgproc.drawMarker(frame, finnishPoint, new Scalar(150, 200, 250));
		
		double distance = Math.sqrt(Math.pow((finnishPoint.x - car.center.x), 2) + Math.pow((finnishPoint.y - car.center.y), 2));
		double minDistance = car.width;
		nearestWaypoint = finnishPoint;
		
		//double z = Math.sqrt(Math.pow(finnishPoint.x - car.center.x, 2) + Math.pow(finnishPoint.y - car.center.x, 2));
		
		//double angle = Math.acos((Math.pow(finnishPoint.x - car.center.x, 2) + Math.pow(z, 2) - Math.pow(finnishPoint.y - car.center.x, 2))/(2 * (finnishPoint.x  - car.center.x) * z));
		
		//System.out.println("Angle: " + angle);
		

		for (int i = 0; i < waypoints.length; i++) {

			Imgproc.drawMarker(frame, waypoints[i], new Scalar(0, 250, 250));

			double currentDistance = Math
					.sqrt(Math.pow((waypoints[i].x - car.center.x), 2) + Math.pow((waypoints[i].y - car.center.y), 2));

			

			
			if (currentDistance <= minDistance) {
				nearestWaypoint = waypoints[(i + 1) % waypoints.length];
				break;
			} else if (distance > currentDistance) {
				distance = currentDistance;
				nearestWaypoint = waypoints[i];
			}
		}
		

		
		if(nearestWaypoint.equals(finnishPoint)) {
			Imgproc.circle(frame, nearestWaypoint, (int) (car.width * 0.5), new Scalar(100, 100, 200), -1);		
		}
		else {
			Imgproc.drawMarker(frame, nearestWaypoint, new Scalar(0, 250, 0), Imgproc.MARKER_TILTED_CROSS);
		}		
		Imgproc.line(frame, car.center, nearestWaypoint, new Scalar(0, 0, 250));


		if (running == null) {

			if (isDone) {
				return this;
			}
			
			

			
			Point p = map.rotatePoint(new Point(nearestWaypoint.x - car.center.x, nearestWaypoint.y - car.center.y));

			Imgproc.line(m, map.center, new Point(map.center.x + p.x, map.center.y + p.y), new Scalar(0, 0, 250));

			running = LocalTime.now();

			double ratio = Car.widthInCM / car.width;

			float nx = (float) (p.x * ratio);
			float ny = (float) (-1 * p.y * ratio);
			// Actions
			ActionList list = new ActionList();
			list.add(new StartCollectionAction());
			list.add(new WayPointAction(nx, ny, 1.00F)); // go to waypoint
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

		return this;
	}

	@Override
	public Mat getFrame() {

		if (map == null) {
			return null;
		}
		return map.getFrame();
	}

	public void handle(String message) {

		//nextState = Bot.easyDriveState;
		//isDone = true;

		// We are done and we are ready for new work!
		if (message.equals(Messages.DONE)) {

			running = null;
		}
	}

}
