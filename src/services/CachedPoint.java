package services;

import java.util.ArrayList;

import org.opencv.core.Point;

public class CachedPoint {

	public Point point;

	private Point[] points;

	int i = 0;
	int size = 40;

	public CachedPoint() {

		points = new Point[size];

	}

	public void add(Point point) {

		points[i] = point;

		i++;
		i = i % size;

		calculateAVG();
	}

	private void calculateAVG() {

		Point temp = new Point(0, 0);

		System.out.println("DEAD");

		int l = 0;
		for (Point point : points) {

			if (point == null) {
				break;
			}
			temp.x += point.x;
			temp.y += point.y;
			l++;

		}
		temp.x = temp.x / l;
		temp.y = temp.y / l;

		point = temp;
		System.out.println(l);
		System.out.println(point);
	}

}
