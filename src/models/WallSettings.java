package models;

import java.util.Properties;

public class WallSettings {

	public final String key = "wall";

	public ImageSettings image;

	public double threshold1, threshold2;
	
	public double camHeight;
	
	public double minArea;

	public WallSettings(Properties props) {
		this.image = new ImageSettings(key, props);

		this.threshold1 = Double.parseDouble(props.getProperty(key + ".canny.threshold1", "0"));
		this.threshold2 = Double.parseDouble(props.getProperty(key + ".canny.threshold2", "0"));
		this.camHeight = Double.parseDouble(props.getProperty(key + ".camheight", "150"));

		this.minArea = Double.parseDouble(props.getProperty(key + ".minArea", "0"));

	}

	public void save(Properties props) {
		
		props.setProperty(key + ".canny.threshold1", ""+threshold1);
		props.setProperty(key + ".canny.threshold2", ""+threshold2);
		props.setProperty(key + ".camheight", ""+camHeight);
		props.setProperty(key + ".minArea", ""+minArea);
		
		image.save(props);
	}

}
