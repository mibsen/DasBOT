package bot.states;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import bot.Bot;
import bot.Connection;
import bot.Controls;
import bot.actions.ActionList;
import bot.actions.StartCollectionAction;
import bot.actions.StopCollectionAction;
import bot.actions.WayPointAction;
import bot.messages.Messages;
import models.Ball;
import services.BallService;
import services.CarService;
import services.WallService;

public class EasyCollect extends State {

	private Ball target;

	public EasyCollect(CarService carService, BallService ballService, WallService wallService) {
		super(carService, ballService, wallService);
		// TODO Auto-generated constructor stub
	}

	public void setTarget(Ball targetBall) {
		target = targetBall;

	}

	@Override
	public void calculate(Mat originalFrame, Mat correctedFrame) {

		// Do we have a target?
		if (target == null) {

			System.out.println("THERE IS NO CURRENT BALL TO COLLECT!!!");
			nextState(new EasyDrive(carService, ballService, wallService));
			return;
		}

		System.out.println("PLANNING EASY COLLECT!");

		Point t = map.correctPoint(target.point);
		t = new Point(t.x - map.center.x, t.y - map.center.y);

		// We only want to Ball to be into the center of the pick
		// TODO:

		// center -> target

		// Width imellem center -> pick

		double d = Math.sqrt(Math.pow(map.center.x, 2) + Math.pow(map.center.y, 2));
		double r = (d - map.car.pickCenter.x) / d;

		t = new Point(t.x * r, t.y * r);

		ActionList list = new ActionList();
		list.add(new StartCollectionAction());

		Point targetCM = getPointInCM(t);

		System.out.println("Driving to: " + targetCM.x + " : " + targetCM.y);
		list.add(new WayPointAction(targetCM.x, targetCM.y, 0.50F,0.4F));

		// list.add(new WaitAction(1000));
		list.add(new StopCollectionAction());

		if (!Bot.test)
			Connection.SendActions(list);

	}

	@Override
	public void drawFrame(Mat originalFrame, Mat correctedFrame) {

		map.drawWall(new Scalar(250, 250, 250), 10);

		map.drawCar(new Scalar(100, 100, 100), 1);

		if (target != null) {

			correctedFrame = map.getFrame();

			Imgproc.line(correctedFrame, map.center, map.correctPoint(target.point), new Scalar(88, 214, 141));
			Imgproc.circle(correctedFrame, map.correctPoint(target.point), 10, new Scalar(200, 200, 200), -1);

			Imgproc.circle(correctedFrame, map.correctPoint(target.point), (int) (car.width * 2.5),
					new Scalar(200, 200, 200), 10);

			Imgproc.line(originalFrame, car.center, target.point, new Scalar(88, 214, 141));

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
