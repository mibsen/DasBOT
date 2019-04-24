package models;

public class ImageSettings {

	public double hueStart;
	public double hueStop;
	public double saturationStart;
	public double saturationStop;
	public double valueStart;
	public double valueStop;
	public double blur;
		

	public ImageSettings(
			double hueStart,
			double hueStop,
			double saturationStart,
			double saturationStop,
			double valueStart,
			double valueStop,
			double blur) {
		
		this.hueStart = hueStart;
		this.hueStop = hueStop;
		this.saturationStart = saturationStart;
		this.saturationStop = saturationStop;
		this.valueStart = valueStart;
		this.valueStop = valueStop;
		this.blur = blur;
		
	}
	
	
	
}
