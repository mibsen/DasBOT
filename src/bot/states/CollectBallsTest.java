package bot.states;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.opencv.core.Point;

import models.Ball;

class CollectBallsTest {

	//@Test
	void test() {
		
		ArrayList<Ball> balls = new ArrayList<Ball>();

		balls.add(new Ball(new Point(1,1),1));

		
		balls.add(new Ball(new Point(0,0),1));
		
		balls.add(new Ball(new Point(0,1),1));

		
		Collections.sort(balls, new Comparator<Ball>() {

			@Override
			public int compare(Ball o1, Ball o2) {
				
				double d1 = Math.sqrt(Math.pow(o1.point.x, 2) + Math.pow(o1.point.y, 2));
				double d2 = Math.sqrt(Math.pow(o2.point.x, 2) + Math.pow(o2.point.y, 2));
		
				if (d1 == d2) {
					return 0;
				}
				System.out.println("HERE!");
				return d1 > d2? 1 : -1;
				
			}
		});
		
		
		for (Ball ball : balls) {
			System.out.println(ball.point.toString());
		}
		
	}

}
