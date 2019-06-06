package models;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import camera.CameraFake;
import config.Config;
import services.CarService;

class MapTest {
	/*
	@BeforeAll
	static void beforeAll() {
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );		
	}
	
	@Test
	void derotateTest() {
		
		CameraFake cam = new CameraFake();
		cam.init();
		
		Mat f = cam.grabFrame();
		
		Config c = new Config();
		CarService carService = new CarService(c.loadCar());

		Car car = carService.getCar(f);
		
		Point front = car.front;
		
		System.out.println((int)(front.x) + " : " + (int)(front.y));
		
		Map m = new Map(car, f);
		
		m.corrected();
		
		System.out.println((int)(m.car.front.x) + " : " + (int)(m.car.front.y));
		
		
		front = m.getOriginalPoint(m.car.front);
		
		System.out.println((int)(front.x) + " : " + (int)(front.y));
		
		
		
	}
	*/

}
