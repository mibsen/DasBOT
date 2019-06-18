package bot.states;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import bot.Bot;
import bot.Connection;
import bot.actions.ActionList;
import bot.actions.ClosePortAction;
import bot.actions.OpenPortAction;
import bot.actions.ShakeAction;
import bot.actions.StartCollectionAction;
import bot.actions.StopCollectionAction;
import bot.actions.TravelAction;
import bot.actions.TurnAction;
import bot.actions.WaitAction;
import bot.actions.WayPointAction;
import models.Map;
import services.BallService;
import services.CarService;
import services.WallService;

public class ScoreGoals extends State {

	private Point[] waypoints;
	private Point nearestWaypoint;
	private Point finnishPoint;
	private Point goalPoint;
	private boolean atFinishPoint;
	private int correctedCount = 0;
	private Point almostTherePoint;

	public ScoreGoals(CarService carService, BallService ballService, WallService wallService) {
		super(carService, ballService, wallService);
	}

	@Override
	public void calculate(Mat originalFrame, Mat correctedFrame) {

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

		// System.out.println("Car back to center: " + car.backToCenter);

		goalPoint = new Point(width + corner1.x - car.backToCenter, height / 2 + corner1.y);
		almostTherePoint = new Point(goalPoint.x - (car.backToCenter / 3), goalPoint.y);
		finnishPoint = new Point(almostTherePoint.x - (car.backToCenter / 3), almostTherePoint.y);

		waypoints = new Point[7];
		waypoints[0] = new Point(width / 4 + corner1.x, height / 4 + corner1.y);
		waypoints[1] = new Point(width / 2 + corner1.x, height / 4 + corner1.y);
		waypoints[2] = new Point(width * 3 / 4 + corner1.x, height / 4 + corner1.y);
		waypoints[3] = goalPoint;
		waypoints[4] = new Point(width * 3 / 4 + corner1.x, height * 3 / 4 + corner1.y);
		waypoints[5] = new Point(width / 2 + corner1.x, height * 3 / 4 + corner1.y);
		waypoints[6] = new Point(width / 4 + corner1.x, height * 3 / 4 + corner1.y);

		double minDistance = car.width;

		// Locate IF i am in FinishPoint

		double distance = Math
				.sqrt(Math.pow((goalPoint.x - car.center.x), 2) + Math.pow((goalPoint.y - car.center.y), 2));

		System.out.println("CorrectedCount: " + correctedCount);

		// Calculates angle towards goal
		Point diff = new Point(goalPoint.x - car.center.x, goalPoint.y - car.center.y);
		long angleToGoal = Math.round(Math.toDegrees(Math.atan2(-diff.y, diff.x)));
		System.out.println("Before: Angle to goal: " + angleToGoal);
		System.out.println("Abs: Angle to goal: " + Math.abs(angleToGoal));

		// start tree of operations
		if (distance < minDistance || atFinishPoint) {

			atFinishPoint = true;

			if (correctedCount == 0) {

				System.out.println("Almost there!");
				distance = Math.sqrt(Math.pow((almostTherePoint.x - car.center.x), 2)
						+ Math.pow((almostTherePoint.y - car.center.y), 2));

				// System.out.println(almostTherePoint);

				Point p = map
						.rotatePoint(new Point(almostTherePoint.x - car.center.x, almostTherePoint.y - car.center.y));

				// System.out.println(p);

				// Turn into the correct Degree
				nearestWaypoint = map.getOriginalPoint(p);

				p = getPointInCM(p);

				// Actions
				ActionList list = new ActionList();
				list.add(new WayPointAction(p.x, p.y, 0.40F, 0.3F)); // go to waypoint

				correctedCount++;

				if (!Bot.test)
					Connection.SendActions(list);

			}

			// We are at the finnishPoint

			// Drive a bit closer

			else if (correctedCount == 1) {

				System.out.println("Driving to finnish point");
				distance = Math.sqrt(
						Math.pow((finnishPoint.x - car.center.x), 2) + Math.pow((finnishPoint.y - car.center.y), 2));

				// System.out.println(finnishPoint);

				Point p = new Point(finnishPoint.x - car.center.x, finnishPoint.y - car.center.y);

				p = map.rotatePoint(p);

				// System.out.println(p);

				// Turn into the correct Degree
				nearestWaypoint = map.getOriginalPoint(p);

				p = getPointInCM(p);

				// Actions
				ActionList list = new ActionList();
				list.add(new WayPointAction(p.x, p.y, 0.40F)); // go to waypoint

				correctedCount++;

				if (!Bot.test)
					Connection.SendActions(list);

			} else if (Math.abs(angleToGoal) > 2) {

				System.out.println("Starting over");
				correctedCount = 0;

				Point p = map.rotatePoint(new Point(goalPoint.x - car.center.x, goalPoint.y - car.center.y));

				// Turn into the correct Degree
				nearestWaypoint = map.getOriginalPoint(p);

				p = getPointInCM(p);

				// Actions
				ActionList list = new ActionList();

				list.add(new WayPointAction(p.x, p.y, 0.40F, 0.3F)); // go to waypoint

				if (!Bot.test)
					Connection.SendActions(list);

			} else if (correctedCount == 2) {
				System.out.println("Driving closer to goal!");
				// Actions
				ActionList list = new ActionList();
				list.add(new TravelAction(-15.0));

				correctedCount++;
				if (!Bot.test)
					Connection.SendActions(list);

			} else {

				System.out.println("Opening port!");

				// Actions
				ActionList list = new ActionList();
				// list.add(new TurnAction(angleToGoal));
				list.add(new OpenPortAction());
				list.add(new ShakeAction());
				list.add(new WaitAction(2000));
				list.add(new ShakeAction());
				list.add(new WaitAction(3000));
				list.add(new ClosePortAction());

				if (!Bot.test)
					Connection.SendActions(list);

				System.out.println("DONE!");
				System.exit(0);

			}

			// open

		} else

		{
			distance = Double.MAX_VALUE;

			for (int i = 0; i < waypoints.length; i++) {

				double currentDistance = Math.sqrt(
						Math.pow((waypoints[i].x - car.center.x), 2) + Math.pow((waypoints[i].y - car.center.y), 2));

				if (currentDistance <= minDistance) {
					nearestWaypoint = waypoints[(i + 1) % waypoints.length];
					break;
				} else if (distance > currentDistance) {
					distance = currentDistance;
					nearestWaypoint = waypoints[i];
				}
			}

			// Drive to nearestWaypoint
			Point p = map.rotatePoint(new Point(nearestWaypoint.x - car.center.x, nearestWaypoint.y - car.center.y));

			p = getPointInCM(p);

			// Actions
			ActionList list = new ActionList();
			list.add(new WayPointAction(p.x, p.y, 0.70F, 0.40F)); // go to waypoint

			if (!Bot.test)
				Connection.SendActions(list);

		}
	}

	@Override
	public void drawFrame(Mat originalFrame, Mat correctedFrame) {

		map.drawWall(new Scalar(250, 250, 250), 10);

		map.drawCar(new Scalar(100, 100, 100), 1);

		if (nearestWaypoint != null) {

			correctedFrame = map.getFrame();

			Imgproc.line(correctedFrame, map.center, map.correctPoint(nearestWaypoint), new Scalar(88, 214, 141));
			Imgproc.circle(correctedFrame, map.correctPoint(nearestWaypoint), 10, new Scalar(200, 200, 200), -1);

			Imgproc.circle(correctedFrame, map.correctPoint(nearestWaypoint), (int) (car.width * 2.5),
					new Scalar(200, 200, 200), 10);

			Imgproc.line(originalFrame, car.center, nearestWaypoint, new Scalar(88, 214, 141));

		}
	}
}
