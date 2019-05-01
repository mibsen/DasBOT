package models;

import java.util.Properties;

public class ImageSettings {

	public double blur;

	public ImageValue hue;
	public ImageValue saturation;
	public ImageValue value;
	private String key;

	public ImageSettings(String key, Properties props) {

		this.key = key;
		this.hue = new ImageValue(key + ".image.hue", props);
		this.saturation = new ImageValue(key + ".image.saturation", props);
		this.value = new ImageValue(key + ".image.value", props);
		this.blur = Double.parseDouble(props.getProperty(key + ".image.blur", "0"));
	}

	public void save(Properties props) {
		this.hue.save(props);
		this.saturation.save(props);
		this.value.save(props);
		props.setProperty(key + ".image.blur", "" + blur);
	}
}
