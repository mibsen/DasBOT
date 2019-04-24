package models;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.imgproc.Imgproc;

public class Wall {

	public Point[] corners;
	public MatOfPoint contour;
	public Point[] points;

	public Wall(MatOfPoint c) {
		
		this.contour = c;

		RotatedRect box = Imgproc.minAreaRect(new MatOfPoint2f(c.toArray()));
		Point[] points = new Point[4];
		box.points(points);
		this.corners = points;
		this.points = c.toArray();

	}
	
	public static Wall copyWithOrigo(Wall wall,Point origo) {
		
		
		Point[] ca = new Point[wall.points.length];
		
		for (int i = 0; i < wall.points.length; i++) {
			Point pp = wall.points[i];
			ca[i] = new Point(pp.x-origo.x,pp.y-origo.y);
		}

		Wall w = new Wall(new MatOfPoint(ca));
		
		return w;
	}

}
