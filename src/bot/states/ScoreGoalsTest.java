package bot.states;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import services.BallService;
import services.CarService;
import services.WallService;

public class ScoreGoalsTest {

	@Test
	public void test() {
		
		Point goalPoint = new Point(766, 259);
		Point carPoint = new Point(661, 268);

		Point diff = new Point(goalPoint.x - carPoint.x, goalPoint.y - carPoint.y);
		//diff.x = -105;
		//diff.y = 9;
		
		long angle = Math.round(Math.toDegrees(Math.atan2(-diff.y,diff.x)));
		System.out.println(angle);
		
		if(angle < 0) {
			angle += 360;
		}
		
		System.out.println(angle);
		System.out.println("------");
		
	}

}
