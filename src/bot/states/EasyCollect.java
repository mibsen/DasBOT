package bot.states;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import bot.Bot;
import bot.Connection;
import bot.actions.ActionList;
import bot.actions.StartCollectionAction;
import bot.actions.StopCollectionAction;
import bot.actions.TestAction;
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

public class EasyCollect extends State {

	private CarService carService;
	private BallService ballService;
	private WallService wallService;
	private Map map;
	private boolean waitForNextFrame = false;
	private ArrayList<Ball> activeBalls = new ArrayList<Ball>();
	private Ball activeBall = null;
	private long timeout = 120;
	public Ball currentBall = null;

	public EasyCollect(CarService carService, BallService ballService, WallService wallService) {

		this.carService = carService;
		this.ballService = ballService;
		this.wallService = wallService;
	}

	@Override
	public State process(Mat frame) {



		Wall wall = null;
		Wall obstacle = null;
		Car car = null;

		frame = wallService.locateWallsAndCorrectFrame(frame);
		wall = wallService.getWall();
		obstacle = wallService.getObstacle();

		car = carService.getCar(frame);


		if (wall == null || car == null || obstacle == null) {
			return this;
		}

		map = new Map(car, frame);
		map.addWall(wall, obstacle);

		map.corrected();

		map.drawCar(new Scalar(0, 250, 250), 1);
		map.drawWall(new Scalar(250, 250, 250), (int) (car.width * 2 ));

		Mat m = map.getFrame();


		if (running == null) {

			System.out.println("PLANNING EASY COLLECT!");

			if (currentBall == null) {
				isDone = true;
				nextState = Bot.easyDriveState;
				System.out.println("THERE IS NO CURRENT BALL TO COLLECT!!!");
				return this;
			}
			

			running = LocalTime.now();

			// Sort Balls by distance to car


			Point activePoint = map.correctPoint(currentBall.point);

			System.out.println("Driving to  ball: " + activePoint.toString());

			
			activePoint = new Point(activePoint.x - map.center.x , activePoint.y - map.center.y);
			
			double ratio = Car.widthInCM / car.width;

			ActionList list = new ActionList();
			list.add(new StartCollectionAction());

			float nx = (float) (activePoint.x * ratio);
			float ny = (float) (-1 * activePoint.y * ratio);
			System.out.println("Driving to: " + nx + " : " + ny);
			list.add(new WayPointAction(nx, ny, 0.80F));
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

		// Draw robot frame
		Ball first = currentBall;

		Imgproc.circle(m, map.correctPoint(first.point), (int) (car.width * 2.5), new Scalar(200,200,200),5);
		Imgproc.line(m, map.center, map.correctPoint(first.point), new Scalar(88, 214, 141));
		Imgproc.line(frame, car.center, first.point, new Scalar(88, 214, 141));

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

		// We are done and we are ready for new work!
		if (message.equals(Messages.DONE)) {
			currentBall =  null;
			running = null;
		}
	}

}
