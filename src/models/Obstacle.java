package models;


import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

public class Obstacle {
	
	public Point[] corners;
	public MatOfPoint contour;
	public Point[] points;

	public Obstacle(MatOfPoint c) {
		
		this.contour = c;

		RotatedRect box = Imgproc.minAreaRect(new MatOfPoint2f(c.toArray()));
		Point[] points = new Point[4];
		box.points(points);
		this.corners = points;
		this.points = c.toArray();
	}

}
