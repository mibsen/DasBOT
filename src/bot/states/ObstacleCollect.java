package bot.states;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import bot.Bot;
import bot.Connection;
import bot.actions.ActionList;
import bot.actions.GrepCollectionAction;
import bot.actions.StartCollectionAction;
import bot.actions.StopCollectionAction;
import bot.actions.TravelAction;
import bot.actions.TurnAction;
import bot.actions.WaitAction;
import bot.actions.WayPointAction;
import bot.messages.Messages;
import models.Ball;
import services.BallService;
import services.CarService;
import services.WallService;

public class ObstacleCollect extends State {

	private Ball target;
	private boolean done = false;

	public ObstacleCollect(CarService carService, BallService ballService, WallService wallService) {
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
			nextState(new CheckState(carService, ballService, wallService));
			return;
		}

		System.out.println("PLANNING OBSTACLE COLLECT!");

		Point t = map.correctPoint(target.point);
		
		t = new Point(t.x - map.center.x, t.y - map.center.y);

		// Verify VINKEL!
		double deg = -Math.toDegrees(Math.atan2(t.y, t.x));
		
		if(Math.abs(deg) > 4) {
			
			//System.out.println("correcting moving " + deg +" Deg");
			
			ActionList list = new ActionList();
			list.add(new TurnAction((long) deg));

			if (!Bot.test)
				Connection.SendActions(list);		
		
			return;
		}
		
		
		// Drive TO
		double d = Math.sqrt(Math.pow(t.x, 2) + Math.pow(t.y, 2));
		
		
		// Distance to ball
		double val = map.car.pickCenter.x;
		
		double r = (d - val) / d;
		t = new Point(t.x * r, t.y * r);

		ActionList list = new ActionList();
	
		Point targetCM = getPointInCM(t);

		//System.out.println("Driving to: " + targetCM.x + " : " + targetCM.y);
		list.add(new WayPointAction(targetCM.x, targetCM.y, 0.7F,0.5F));

		double backDistance = (Math.sqrt(Math.pow(targetCM.x,2) + Math.pow(targetCM.y,2)) * 1.2);
		
		//System.out.println("driving " + backDistance + " CM back");

		list.add(new GrepCollectionAction());
		list.add(new TravelAction(-backDistance));
		list.add(new StartCollectionAction());
		list.add(new WaitAction(2000));
		list.add(new StopCollectionAction());
		if (!Bot.test)
			Connection.SendActions(list);
		
		done = true;

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
			
			if(done) {
				target = null;
			}
			
		}
	}
}
