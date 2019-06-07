package bot.states;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opencv.core.Mat;
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
import models.Ball;
import models.Car;
import models.Map;
import models.Wall;
import services.BallService;
import services.CarService;
import services.WallService;

public class CollectBalls extends State {

	private CarService carService;
	private BallService ballService;
	private WallService wallService;
	private Map map;
	private ExecutorService executor = Executors.newFixedThreadPool(3);
	private Ball activeBall = null;
	private boolean waitForNextFrame = false;
	
	
	public CollectBalls(CarService carService, BallService ballService, WallService wallService) {

		this.carService = carService;
		this.ballService = ballService;
		this.wallService = wallService;
	}

	@Override
	public State process(Mat frame) {

		final Mat f = frame.clone();

		// We need car Position and Wall

		Future<Wall> wFuture = executor.submit(new Callable<Wall>() {

			@Override
			public Wall call() throws Exception {
				return wallService.getWall(f);
			}

		});

		Future<Car> cFuture = executor.submit(new Callable<Car>() {

			@Override
			public Car call() throws Exception {
				return carService.getCar(f);
			}

		});

		Future<List<Ball>> bFuture = executor.submit(new Callable<List<Ball>>() {

			@Override
			public List<Ball> call() throws Exception {
				return ballService.getBalls(f);
			}

		});

		Wall wall = null;
		Car car = null;
		List<Ball> balls = null;

		try {
			wall = wFuture.get();
			car = cFuture.get();
			balls = bFuture.get();

			if (car == null || wall == null) {
				return this;
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		map = new Map(car, f);
		map.addBalls(balls);
		map.addWall(wall);

		map.corrected();

		map.drawCar(new Scalar(0, 250, 250), 1);
		map.drawWall(new Scalar(250, 250, 250), (int) (car.width * 2));
		map.drawBalls(new Scalar(0, 250, 250), 1);
		
		Mat m = map.getFrame();

		if (running == null) {

			// Wait for response
			System.out.println("Press enter to start");
			new Scanner(System.in).nextLine();
			System.out.println("Running again");
			
			if(waitForNextFrame) {
				waitForNextFrame = false;
				return this;
			}

			waitForNextFrame = true;
			
			// Locate A Ball

			if (map.balls.size() == 0) {
				System.out.println("THERE IS NO BALLS TO COLLECT!!!");
				return this;
			}

			Ball ball = null;

			// Close - Not to close
			// We do not want balls to close!
			double distance = Double.MAX_VALUE;
			double minDistance = car.width * 2;

			for (Ball b : map.balls) {

				double d = Math.sqrt(Math.pow(b.point.x, 2) + Math.pow(b.point.y, 2));

				if (d <= distance && d >= minDistance) {

					// Not against Wall
					if (new Scalar(m.get((int) (b.point.y), (int) (b.point.x))).equals(new Scalar(250, 250, 250))) {
						System.out.println("THIS BALL IS TO CLOSE TO THE BORDER!");
					} else {
						distance = d;
						ball = b;
					}
				}

			}

			if (ball == null) {
				System.out.println("COULD NOT LOCATE BALL");
				return this;
			}

			// Save the located active ball
			activeBall = new Ball(map.getOriginalPoint(ball.point), ball.area);

			// Drive through while motor is running

			System.out.println("Driving to: " + ball.point.toString());

			
			double ratio = car.widthInCM / car.width;
			float nx = (float) (ball.point.x*ratio);
			float ny = (float) (-1 * ball.point.y*ratio);
			System.out.println(car.width);
			System.out.println("Driving to: " +  nx + " : " + ny);

			
			ActionList list = new ActionList();
			list.add(new StartCollectionAction());
			list.add(new WayPointAction(nx, ny, 0.01F));
			list.add(new WaitAction(5000));
			list.add(new StopCollectionAction());
			list.add(new TestAction("-DONE-"));
			
			if(!Bot.test)
			Connection.SendActions(list);

		}

		Imgproc.line(m, map.center, map.correctPoint(activeBall.point), new Scalar(88, 214, 141));
		Imgproc.line(frame, car.center, activeBall.point, new Scalar(88, 214, 141));

		return this;
	}

	@Override
	public Mat getFrame() {

		if (map == null) {
			return null;
		}
		return map.getFrame();
	}

}
