package bot.states;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import bot.Bot;
import bot.Connection;
import bot.actions.Action;
import bot.actions.ActionList;
import bot.actions.FinishAction;
import bot.actions.SoundAction;
import bot.actions.StartCollectionAction;
import bot.actions.StopCollectionAction;
import bot.actions.WaitAction;
import bot.messages.Messages;
import models.Ball;
import services.BallService;
import services.CarService;
import services.WallService;

public class FinishState extends State {

	public FinishState(CarService carService, BallService ballService, WallService wallService) {
		super(carService, ballService, wallService);
	}

	@Override
	public void calculate(Mat originalFrame, Mat correctedFrame) {

		// check if times is up
		if (!Bot.DONE) {
			List<Ball> balls = new ArrayList<Ball>();
			for (Ball ball : map.balls) {
				if (map.car.isOutside(ball)) {
					balls.add(ball);
				}
			}

			System.out.println("Current time: " + System.currentTimeMillis());
			System.out.println("Runtime time: " + Bot.RUNTIME_IN_MS);
			System.out.println("Balls: " + ballService.removeBallsOutOfBounds(map.getWall(), balls).size());
			if (ballService.removeBallsOutOfBounds(map.getWall(), balls).size() == 0 || (System.currentTimeMillis() - Bot.RUNTIME_IN_MS) > Bot.SEVEN_MINUTES_RUNTIME) {
				System.out.println("All balls are gone!");
				

				ActionList list = new ActionList();
				list.add(new FinishAction());

				if (!Bot.test)
					Connection.SendActions(list);

				Bot.DONE = true;

				System.out.println();
				System.out.println("----- DONE -----");
				System.out.println();
				System.out.println("Time spent: " + (new Timestamp(System.currentTimeMillis() - Bot.RUNTIME_IN_MS)
						.toLocaleString().substring(14)));
				System.out.println();
				System.out.println("----- DONE -----");

			} else {
				System.out.println("WE'RE NOT DONE!!!!!!");
				Bot.ALL_BALLS_COLLECTED = false;
				nextState(new CheckState(carService, ballService, wallService));
			}
		}

	}

	@Override
	public void drawFrame(Mat originalFrame, Mat correctedFrame) {
		// TODO Auto-generated method stub
		for (Ball ball : map.balls) {
			Imgproc.drawMarker(originalFrame, map.getOriginalPoint(ball.point), new Scalar(0, 250, 150));
		}
	}

	@Override
	public void handle(String message) {
		if (message.equals(Messages.DONE)) {
			if (!Bot.DONE) {
				nextState(new CheckState(carService, ballService, wallService));
			}
		}
	}

}
