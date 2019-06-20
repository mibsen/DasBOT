package bot.states;

import org.opencv.core.Mat;

import bot.Bot;
import services.BallService;
import services.CarService;
import services.WallService;

public class CheckState extends State {

	public CheckState(CarService carService, BallService ballService, WallService wallService) {
		super(carService, ballService, wallService);
	}

	@Override
	public void calculate(Mat originalFrame, Mat correctedFrame) {

		if (System.currentTimeMillis() - Bot.RUNTIME_IN_MS > Bot.SEVEN_MINUTES_RUNTIME) {
			System.out.println("TIME IS UP! Scoring goals...");
			nextState(new ScoreGoals(carService, ballService, wallService));
			return;
		}
		if (Bot.ALL_BALLS_COLLECTED) {
			System.out.println("All balls are collected! Lets Finish this thing :*");
			nextState(new ScoreGoals(carService, ballService, wallService));
			return;
		}
		if (!Bot.DONE) {
			nextState(new EasyDrive(carService, ballService, wallService));
			return;
		}

	}

	@Override
	public void drawFrame(Mat originalFrame, Mat correctedFrame) {
		// TODO Auto-generated method stub
	}

}
