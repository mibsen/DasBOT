package bot.states;

import java.time.LocalTime;
import java.util.Scanner;

import org.opencv.core.Mat;

import bot.messages.Messages;

public abstract class State {

	protected LocalTime running;
	public boolean isDone = false;
	
	public void handle(String message) {

		// We are done and we are ready for new work!
		if (message.equals(Messages.DONE)) {
						
			running = null;
		}
	}
	

	@Override
	public String toString() {
		return this.getClass().getName();
	}

	public abstract State process(Mat frame);
	
	public abstract Mat getFrame();
}
