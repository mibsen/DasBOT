package models;

import java.util.Properties;

public class ObstacleSettings {
	
	public final String key = "obstacle";

	public ImageSettings image;

	public double threshold1, threshold2;
	
	public double minArea, maxArea;

	public ObstacleSettings(Properties props) {
		this.image = new ImageSettings(key, props);

		this.threshold1 = Double.parseDouble(props.getProperty(key + ".canny.threshold1", "0"));
		this.threshold2 = Double.parseDouble(props.getProperty(key + ".canny.threshold2", "0"));
		
		this.minArea = Double.parseDouble(props.getProperty(key + ".minArea", "0"));
		this.maxArea = Double.parseDouble(props.getProperty(key + ".maxArea", "0"));

	}

	public void save(Properties props) {
		
		props.setProperty(key + ".canny.threshold1", ""+threshold1);
		props.setProperty(key + ".canny.threshold2", ""+threshold2);

		props.setProperty(key + ".minArea", "" + minArea);
		props.setProperty(key + ".maxArea", "" + maxArea);
		
		image.save(props);
		
	}

}
