package bot.states;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.ws.handler.MessageContext;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import bot.Bot;
import bot.Connection;
import bot.actions.ActionList;
import bot.actions.StartCollectionAction;
import bot.actions.StopCollectionAction;
import bot.actions.TestAction;
import bot.actions.TurnAction;
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

public class RandomDrive extends State {

	private LocalTime running;
	int timeout = 5;
	private CarService carService;
	private BallService ballService;
	private WallService wallService;
	private Point target;
	private Map map;

	public RandomDrive(CarService carService, BallService ballService, WallService wallService) {

		this.carService = carService;
		this.ballService = ballService;
		this.wallService = wallService;
	}

	@Override
	public State process(Mat oframe) {

		final Mat f = oframe.clone();
		
		//Point ballPoint = ballService.getBalls(f).get(0).point;
		
		//System.out.println("Ballpoint: " + ballPoint);

		// We need car Position and Wall
		ExecutorService executor = Executors.newFixedThreadPool(3);
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

		Wall wall = null;
		Car car = null;

		try {
			wall = wFuture.get();
			car = cFuture.get();
			
			if(car == null) {
				return this;
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		map = new Map(car, f);
		map.addWall(wall);
		map.corrected();

		map.drawCar(new Scalar(0, 250, 250), 1);

		map.drawWall(new Scalar(255, 250, 0), car.width.intValue()*3);
		map.drawWall(new Scalar(0, 250, 0), car.width.intValue() / 2);
		map.drawWall(new Scalar(250, 250, 250), 1);

		Mat frame = map.getFrame();

		// We are not yet driving lets schedule some new stuff
		if (running == null) {

			System.out.println("PLANNING NEW PATH");
			running = LocalTime.now();

			// Center
			Point center = map.center;
			Mat m = map.getFrame();

			System.out.println(center);
			System.out.println("rows:" + m.rows()); // ROW = y
			System.out.println("cols:" + m.cols()); // CCOLS = x

			Scalar c = new Scalar(m.get((int) center.y, (int) center.x));

			// We could be in danger ZONE ! drive back into safety
			if (c.equals(new Scalar(0, 250, 0))) {
				System.out.println("We are in the danger zone");
				// Create wall points
				MatOfPoint2f p2f = new MatOfPoint2f(map.getWall().contour);

				// Locate new point not inside danger zone and max 2 times car width away
				Random random = new Random();
				while (true) {
					// Trying
					System.out.println("trying to loate safe spot");

					int x = (int) (random.nextInt((int) (car.width + 1 + car.width)) - car.width);
					int y = (int) (random.nextInt((int) (car.width + 1 + car.width)) - car.width);

					Point p = new Point(x, y);
					// Verify point
					if (!(new Scalar(m.get((int) center.y, (int) center.x)).equals(new Scalar(0, 250, 0)))
							&& Imgproc.pointPolygonTest(p2f, p, false) > 0) {

						// This is Save
						target = p;
						break;

					} else {
						// Tried point
						Imgproc.drawMarker(m, p, new Scalar(250, 215, 160));
						System.out.println("tried: " + p);
					}

				}

			} else {
				System.out.println("LETS find a PAth");
				// Lets find somewhere to drive!
				int max = (int) (car.width * 20);
				int steps = 1;

				// We start By going UP !! (which is following the X axes
				Point p = center.clone();

				int x = (int) (p.x + (car.width / 2) + 5);
				int y = (int) (p.y);

				// We should go up ! M
				if (!new Scalar(m.get(y, x)).equals(new Scalar(255, 250, 0))) {

					System.out.println("UP!");

					while (Math.abs(center.x - x) < max) {

						x += steps;

						c = new Scalar(m.get(y, x + 5));

						// We have reached our destinations
						if (c.equals(new Scalar(255, 250, 0))) {
							break;
						}

					}

					System.out.println("THIS! " + x + ": " + y);
					target = map.getOriginalPoint( new Point(x - center.x, y - center.y));
					System.out.println("TARGET: " + target.toString());

			} else {
				
				System.out.println("TURN ME ROUND ROUND");
				
				ActionList list = new ActionList();
				list.add(new TurnAction(180));
				
				if(!Bot.test)
					Connection.SendActions(list);
				
				return this;
			}

				Point correctedTarget = new Point(x - center.x, y - center.y);
				
				// We now have a target!
				System.out.println("PLANNED NEW PATH: " + correctedTarget);
				// Send Action to robot!
				
				// Recalculate into the correct length
				ActionList list = new ActionList();
				
				double ratio = car.widthInCM / car.width;
				float nx = (float) (correctedTarget.x*ratio);
				float ny = (float) (correctedTarget.y*ratio);
				System.out.println(car.width);
				System.out.println("Driving to: " +  nx + " : " + ny);
				
	//			list.add(new StartCollectionAction());
				list.add(new WayPointAction(nx, ny));
	//			list.add(new StopCollectionAction());
				list.add(new TestAction("-DONE-"));
				
				if(!Bot.test)
				Connection.SendActions(list);
			}
		}

		// Verify the Timeout
		if (Duration.between(running, LocalTime.now()).getSeconds() > timeout) {

			// We have timed out
			System.out.println("DRIVING TIMEOUT");

			// TODO: Send HARDBREAK ACTION

			running = null;

			return this;
		}

		// We have a planned PATH Lets draw verify and return that visual

		Imgproc.line(frame, map.center, map.correctPoint(target), new Scalar(88, 214, 141));
		
		Imgproc.line(oframe, car.center, target, new Scalar(88, 214, 141));

		
		return this;
	}

	@Override
	public void handle(String message) {

		// We are done and we are ready for new work!
		if (message.equals(Messages.DONE)) {
			running = null;
		}
	}

	@Override
	public Mat getFrame() {
		
		if(map == null) {
			return null;
		}
		return map.getFrame();
	}
}
