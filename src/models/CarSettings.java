package models;

import java.util.Properties;

public class CarSettings {

	public final String key = "car";

	public ImageSettings image;

	public ImageValue front;
	public ImageValue back;

	public CarSettings(Properties props) {
		
		this.image = new ImageSettings(key, props);

		this.front = new ImageValue(key+".front", props);
		this.back = new ImageValue(key+".back", props);

	}

	public void save(Properties props) {
		
		front.save(props);
		back.save(props);
		image.save(props);
		
	}
	


}
