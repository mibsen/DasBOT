package bot.states;

import org.opencv.core.Mat;

public abstract class State {

	public abstract void handle(String message);

	public abstract State process(Mat frame);
	
	public abstract Mat getFrame();
}
