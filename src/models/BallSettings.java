package models;

import java.util.Properties;

public class BallSettings {

	public double radiusMin, radiusMax;

	public final String key = "ball";

	public ImageSettings image;

	public double min, max;

	public BallSettings(Properties props) {
		this.image = new ImageSettings(key, props);

		this.min = Double.parseDouble(props.getProperty(key + ".min", "0"));
		this.max = Double.parseDouble(props.getProperty(key + ".max", "0"));

		this.radiusMin = Double.parseDouble(props.getProperty(key + ".radiusMin", "0"));
		this.radiusMax = Double.parseDouble(props.getProperty(key + ".radiusMax", "0"));

		
	}

	public void save(Properties props) {

		props.setProperty(key + ".min", "" + min);
		props.setProperty(key + ".max", "" + max);

		props.setProperty(key + ".radiusMin", "" + radiusMin);
		props.setProperty(key + ".radiusMax", "" + radiusMax);

		image.save(props);

	}

}
