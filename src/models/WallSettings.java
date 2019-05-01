package models;

import java.util.Properties;

public class WallSettings {

	public final String key = "wall";

	public ImageSettings image;

	public double threshold1, threshold2;

	public WallSettings(Properties props) {
		this.image = new ImageSettings(key, props);

		this.threshold1 = Double.parseDouble(props.getProperty(key + ".canny.threshold1", "0"));
		this.threshold2 = Double.parseDouble(props.getProperty(key + ".canny.threshold2", "0"));

	}

	public void save(Properties props) {
		
		props.setProperty(key + ".canny.threshold1", ""+threshold1);
		props.setProperty(key + ".canny.threshold2", ""+threshold2);
		
		image.save(props);
		
	}

}
