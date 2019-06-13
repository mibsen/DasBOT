package bot.states;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import bot.Bot;
import bot.messages.Messages;
import models.Ball;
import models.Car;
import models.Map;
import models.Wall;
import services.BallService;
import services.CarService;
import services.WallService;

public abstract class State {

	public LocalTime running;
	protected int timeout = 120;

	protected CarService carService;
	protected BallService ballService;
	protected WallService wallService;

	protected Wall wall = null;
	protected Wall obstacle = null;
	protected Car car = null;
	protected List<Ball> balls = null;
	protected Map map;

	public State(CarService carService, BallService ballService, WallService wallService) {
		this.carService = carService;
		this.ballService = ballService;
		this.wallService = wallService;
	}

	public void handle(String message) {

		// We are done and we are ready for new work!
		if (message.equals(Messages.DONE)) {
			running = null;
		}
	}

	@Override
	public String toString() {
		return this.getClass().getName();
	}

	public State process(Mat frame) {

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

		if (running == null) {

			calculate(frame, m);
			
			running = LocalTime.now();
		}

		// Verify the Timeout
		if (Duration.between(running, LocalTime.now()).getSeconds() > timeout) {
			System.out.println("DRIVING TIMEOUT");
			running = null;
		}

		drawFrame(frame, m);
		
		return this;
	}

	protected void nextState(State state) {
	
		System.out.println("Changing state: " + state.getClass().toString());
		Bot.state = state;
		
	}
	
	protected Point getPointInCM(Point p) {

		double ratio = (Car.widthInCM / car.width);
		float nx = (float) ((p.x * ratio));
		float ny = (float) (-1 * ((p.y * ratio)));

		return new Point(nx,ny);
		
	}
	
	public abstract void calculate(Mat originalFrame, Mat correctedFrame);

	public abstract void drawFrame(Mat originalFrame, Mat correctedFrame);

	public Mat getFrame() {
		if (map == null) {
			return null;
		}
		return map.getFrame();
	};

}
