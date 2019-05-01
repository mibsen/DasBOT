package models;

import java.util.Properties;

public class ImageValue {

	public double start, stop;
	private String key;

	public ImageValue(String key, double start, double stop) {
		this.key = key;
		this.start = start;
		this.stop = stop;
	}

	public ImageValue(String key, Properties props) {
		this.key = key;
		this.start = Double.parseDouble(props.getProperty(key + ".start", "0"));
		this.stop = Double.parseDouble(props.getProperty(key + ".stop", "0"));
	}

	public void save(Properties props) {
		props.setProperty(key + ".start", "" + start);
		props.setProperty(key + ".stop", "" + stop);
	}
}