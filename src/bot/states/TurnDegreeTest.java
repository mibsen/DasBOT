package bot.states;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import bot.Bot;
import bot.Connection;
import bot.actions.ActionList;
import bot.actions.StartCollectionAction;
import bot.actions.StopCollectionAction;
import bot.actions.WayPointAction;
import bot.messages.Messages;
import models.Ball;
import services.BallService;
import services.CarService;
import services.WallService;

public class TurnDegreeTest extends State {

	private Point target;

	private Point start;
	private Point xAxis;
	
	public TurnDegreeTest(CarService carService, BallService ballService, WallService wallService) {
		super(carService, ballService, wallService);
	}

	@Override
	public void calculate(Mat originalFrame, Mat correctedFrame) {

		System.out.println("PLANNING TurnDegreeTest!");

		start = car.center;
		xAxis = map.car.center;
		xAxis.x = 2000;
		xAxis = map.getOriginalPoint(xAxis);
		target = wall.center;
		
		Point t = map.correctPoint(wall.center);

		t = new Point(t.x - map.center.x, t.y - map.center.y);

		ActionList list = new ActionList();

		Point targetCM = getPointInCM(t);

		System.out.println("Driving to: " + targetCM.x + " : " + targetCM.y);
		list.add(new WayPointAction(targetCM.x, targetCM.y, 0.60F));

		if (!Bot.test)
			Connection.SendActions(list);

	}

	@Override
	public void drawFrame(Mat originalFrame, Mat correctedFrame) {

		map.drawWall(new Scalar(250, 250, 250), 10);

		map.drawCar(new Scalar(100, 100, 100), 1);

		if (target != null) {

			correctedFrame = map.getFrame();
			Imgproc.line(correctedFrame, map.center, map.correctPoint(target), new Scalar(88, 214, 141));			
			Imgproc.line(correctedFrame, map.center, map.correctPoint(start), new Scalar(88, 214, 141));			
			
			
			Imgproc.circle(correctedFrame, map.correctPoint(target), 10, new Scalar(200, 200, 200), -1);
			
			carService.drawCar(originalFrame, car);
			Imgproc.line(originalFrame, car.center, target, new Scalar(88, 214, 141));
			Imgproc.line(originalFrame, start, xAxis, new Scalar(88, 214, 141));
			Imgproc.line(originalFrame, start, target, new Scalar(88, 214, 141));

			
		}

	}

	@Override
	public void handle(String message) {

		// We are done and we are ready for new work!
		if (message.equals(Messages.DONE)) {
			running = null;
			target = null;
		}
	}
}
