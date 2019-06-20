package bot.states;

import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import models.Wall;
import services.BallService;
import services.CarService;
import services.WallService;

public class StartState extends State {

	public StartState(CarService carService, BallService ballService, WallService wallService) {
		super(carService, ballService, wallService);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void calculate(Mat originalFrame, Mat correctedFrame) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drawFrame(Mat originalFrame, Mat correctedFrame) {

		map.drawWall(new Scalar(250, 250, 250), (int) (car.width * 2));
		// map.drawBalls(new Scalar(250, 250, 0), 5, -1);
		map.drawCar(new Scalar(100, 100, 100), 1);

		for (Point p : obstacle.corners) {
			Imgproc.drawMarker(originalFrame, p, new Scalar(0, 255, 255));
			
		}
		
		Wall w = new Wall(new MatOfPoint(obstacle.corners));

		Imgproc.drawContours(originalFrame, Arrays.asList(w.contour), 0, new Scalar(0,255,255));
		
		
	}

}
