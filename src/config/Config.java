package config;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import models.BallSettings;
import models.CarSettings;
import models.ObstacleSettings;
import models.WallSettings;

public class Config {

	Properties props;
	private String file = "/app.properties";

	public Config() {
		this.loadConfig();
	}

	private void loadConfig() {

		props = new Properties();
		InputStream in = getClass().getResourceAsStream(file);
		try {
			props.load(in);
			in.close();
		} catch (IOException e) {
			System.out.println("COULD NOT LOAD CONFIG");
			e.printStackTrace();
		}
	}

	private void saveConfig() {
		try {
			props.store(new FileWriter(this.getClass().getResource(file).getPath()), null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BallSettings loadBall() {

		return new BallSettings(props);
	}

	public void saveBall(BallSettings bs) {
		bs.save(props);
		saveConfig();
	}
	
	public ObstacleSettings loadObstacle() {
		ObstacleSettings os = new ObstacleSettings(props);
		return os;
	}

	public WallSettings loadWall() {
		WallSettings ws = new WallSettings(props);
		return ws;
	}

	public void saveWall(WallSettings ws) {

		ws.save(props);
		saveConfig();

	}

	public CarSettings loadCar() {
		CarSettings cs = new CarSettings(props);
		return cs;
	}

	public void saveCar(CarSettings cs) {

		cs.save(props);
		saveConfig();

	}

	public void saveObstacle(ObstacleSettings obstacleSettings) {
		obstacleSettings.save(props);
		saveConfig();
	}
}