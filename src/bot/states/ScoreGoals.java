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
import bot.messages.Messages;
import models.Map;
import services.BallService;
import services.CarService;
import services.WallService;

public class ScoreGoals extends State {

	private Point[] waypoints;
	private Point nearestWaypoint;
	private Point finnishPoint;
	private Point goalPoint;
	private int correctedCount = 0;
	private Point almostTherePoint;
	private boolean hasScored;

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

		// finnishPoint = new Point(almostTherePoint.x - (car.backToCenter / 3),
		// almostTherePoint.y);

		if (waypoints == null) {
			waypoints = new Point[7];

			if (Bot.GOAL_POSITION == 0) {

				goalPoint = new Point(corner1.x, height / 2 + corner1.y);
				almostTherePoint = new Point(goalPoint.x + car.backToCenter, goalPoint.y);

				waypoints[0] = new Point(width * 3 / 4 + corner1.x, height / 4 + corner1.y);
				waypoints[1] = new Point(width / 2 + corner1.x, height / 4 + corner1.y);
				waypoints[2] = new Point(width / 4 + corner1.x, height / 4 + corner1.y);
				waypoints[3] = almostTherePoint;
				waypoints[4] = new Point(width / 4 + corner1.x, height * 3 / 4 + corner1.y);
				waypoints[5] = new Point(width / 2 + corner1.x, height * 3 / 4 + corner1.y);
				waypoints[6] = new Point(width * 3 / 4 + corner1.x, height * 3 / 4 + corner1.y);
			}
			if (Bot.GOAL_POSITION == 1) {

				goalPoint = new Point(width + corner1.x, height / 2 + corner1.y);
				almostTherePoint = new Point(goalPoint.x - car.backToCenter, goalPoint.y);

				waypoints[0] = new Point(width / 4 + corner1.x, height / 4 + corner1.y);
				waypoints[1] = new Point(width / 2 + corner1.x, height / 4 + corner1.y);
				waypoints[2] = new Point(width * 3 / 4 + corner1.x, height / 4 + corner1.y);
				waypoints[3] = almostTherePoint;
				waypoints[4] = new Point(width * 3 / 4 + corner1.x, height * 3 / 4 + corner1.y);
				waypoints[5] = new Point(width / 2 + corner1.x, height * 3 / 4 + corner1.y);
				waypoints[6] = new Point(width / 4 + corner1.x, height * 3 / 4 + corner1.y);

			}
		}

		double minDistance = car.width / 2;

		// Locate IF i am in FinishPoint

		if (!hasScored) {

			double distance = Math.sqrt(Math.pow((almostTherePoint.x - car.center.x), 2)
					+ Math.pow((almostTherePoint.y - car.center.y), 2));

			System.out.println("CorrectedCount: " + correctedCount);

			System.out.println("Nearest waypoint: " + nearestWaypoint);
			// start tree of operations
			if (distance < minDistance) {

				// Calculates angle towards goal

				Point diff = map.correctPoint(goalPoint);
				diff = new Point(diff.x - map.center.x, diff.y - map.center.y);

				long angleToGoal = Math.round(Math.toDegrees(Math.atan2(diff.y, diff.x)));
				//// System.out.println("Before: Angle to goal: " + angleToGoal);
				System.out.println("Abs: Angle to goal: " + angleToGoal);

				// If angle to goal is too big we correct the angle
				if (180 - Math.abs(angleToGoal) > 2) {

					System.out.println("correcting moving " + angleToGoal + " Deg");
					long deg = 0;
					if (angleToGoal >= 0) {
						deg = 180 - Math.abs(angleToGoal);
					}
					if (angleToGoal < 0) {
						deg = -(180 - Math.abs(angleToGoal));
					}

					System.out.println("Moving degree is: " + deg);

					ActionList list = new ActionList();
					list.add(new TurnAction((long) deg));

					if (!Bot.test)
						Connection.SendActions(list);

				}
				// Otherwise we'll start to score goals
				else {

					System.out.println("Opening port!");

					// Actions
					ActionList list = new ActionList();
					list.add(new OpenPortAction());
					list.add(new ShakeAction());
					list.add(new ShakeAction());
					list.add(new ShakeAction());
					list.add(new WaitAction(3000));
					list.add(new ClosePortAction());

					if (!Bot.test)
						Connection.SendActions(list);

					System.out.println("DONE!");

					hasScored = true;

				}
			} else if (nearestWaypoint == null) {

				System.out.println("Waypoint was null");
				distance = Double.MAX_VALUE;

				for (int i = 0; i < waypoints.length; i++) {

					double currentDistance = Math.sqrt(Math.pow((waypoints[i].x - car.center.x), 2)
							+ Math.pow((waypoints[i].y - car.center.y), 2));

					if (distance > currentDistance) {
						distance = currentDistance;
						nearestWaypoint = waypoints[i];
					}
				}

				// Drive to nearestWaypoint
				Point p = map
						.rotatePoint(new Point(nearestWaypoint.x - car.center.x, nearestWaypoint.y - car.center.y));

				p = getPointInCM(p);

				// Actions
				ActionList list = new ActionList();
				list.add(new WayPointAction(p.x, p.y, 0.90F, 0.40F)); // go to waypoint

				if (!Bot.test)
					Connection.SendActions(list);

			} else {

				System.out.println("Waypoint was not null");

				if (nearestWaypoint == waypoints[0]) {
					nearestWaypoint = waypoints[1];
				} else if (nearestWaypoint == waypoints[1]) {
					nearestWaypoint = waypoints[2];
				} else if (nearestWaypoint == waypoints[2] || nearestWaypoint == waypoints[4]) {
					nearestWaypoint = waypoints[3];
				} else if (nearestWaypoint == waypoints[5]) {
					nearestWaypoint = waypoints[4];
				} else if (nearestWaypoint == waypoints[6]) {
					nearestWaypoint = waypoints[5];
				}

				// Drive to nearestWaypoint
				Point p = map
						.rotatePoint(new Point(nearestWaypoint.x - car.center.x, nearestWaypoint.y - car.center.y));

				p = getPointInCM(p);

				// Actions
				ActionList list = new ActionList();
				list.add(new WayPointAction(p.x, p.y, 0.90F, 0.40F)); // go to waypoint

				if (!Bot.test)
					Connection.SendActions(list);
			}
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
		Imgproc.drawMarker(originalFrame, goalPoint, new Scalar(200, 50, 200), Imgproc.MARKER_CROSS);
		Imgproc.line(originalFrame, car.center, goalPoint, new Scalar(200, 50, 200));
	}

	@Override
	public void handle(String message) {
		if (message.equals(Messages.DONE)) {
			running = null;

			if (hasScored) {
				System.out.println("Should change state to FinishState now!");
				nextState(new FinishState(carService, ballService, wallService));
			}
		}
	}

}
