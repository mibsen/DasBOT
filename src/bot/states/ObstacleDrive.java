package bot.states;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import bot.Bot;
import bot.Connection;
import bot.actions.ActionList;
import bot.actions.WayPointAction;
import bot.messages.Messages;
import services.BallService;
import services.CarService;
import services.WallService;

public class ObstacleDrive extends State {

	private Point nearestWaypoint;
	private Point[] waypoints;

	public ObstacleDrive(CarService carService, BallService ballService, WallService wallService) {
		super(carService, ballService, wallService);
		// TODO Auto-generated constructor stub
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

		Point[] waypoints = new Point[4];
		waypoints[0] = new Point(width / 4 + corner1.x, height / 4 + corner1.y);
		waypoints[1] = new Point(width * 3 / 4 + corner1.x, height / 4 + corner1.y);
		waypoints[2] = new Point(width * 3 / 4 + corner1.x, height * 3 / 4 + corner1.y);
		waypoints[3] = new Point(width / 4 + corner1.x, height * 3 / 4 + corner1.y);

		double distance = 0;
		double minDistance = car.width;

		for (int i = 0; i < waypoints.length; i++) {

			double currentDistance = Math
					.sqrt(Math.pow((waypoints[i].x - car.center.x), 2) + Math.pow((waypoints[i].y - car.center.y), 2));

			if (currentDistance <= minDistance) {
				nearestWaypoint = waypoints[(i + 1) % waypoints.length];
				break;
			} else if (distance == 0 || distance > currentDistance) {
				distance = currentDistance;
				nearestWaypoint = waypoints[i];
			}
		}

		Point p = map.rotatePoint(new Point(nearestWaypoint.x - car.center.x, nearestWaypoint.y - car.center.y));

		ActionList list = new ActionList();

		p = getPointInCM(p);
		list.add(new WayPointAction((float)p.x, (float)p.y, 1.00F)); // go to waypoint

		if (!Bot.test)
			Connection.SendActions(list);

	}

	@Override
	public void drawFrame(Mat originalFrame, Mat correctedFrame) {

		map.drawWall(new Scalar(255,255,255), 10);
		map.drawCar(new Scalar(200,200,200), 2);
		
		if (originalFrame != null) {

			Imgproc.drawMarker(originalFrame, nearestWaypoint, new Scalar(0, 250, 0), Imgproc.MARKER_TILTED_CROSS);
			Imgproc.line(originalFrame, car.center, nearestWaypoint, new Scalar(0, 0, 250));

		}

		if (waypoints != null) {
			for (int i = 0; i < waypoints.length; i++) {

				Imgproc.drawMarker(originalFrame, waypoints[i], new Scalar(0, 250, 250));
			}
		}

		Point p = map.correctPoint(nearestWaypoint);
		Imgproc.line(correctedFrame, map.center, p, new Scalar(0, 0, 250));

	}
	
	
	@Override
	public void handle(String message) {
		
		if (message.equals(Messages.DONE)) {
			nextState(new EasyDrive(carService, ballService, wallService));
		}
		
		
		
	}
}
