package bot.states;

import java.time.LocalTime;
import java.util.Scanner;

import org.opencv.core.Mat;

import bot.messages.Messages;

public abstract class State {

	protected LocalTime running;
	
	public void handle(String message) {

		// We are done and we are ready for new work!
		if (message.equals(Messages.DONE)) {
			
			
			// Wait for response
			System.out.println("Received DONE. Press enter to continue");
			new Scanner(System.in).nextLine(); 
			System.out.println("Running again");
						
			running = null;
		}
	}

	public abstract State process(Mat frame);
	
	public abstract Mat getFrame();
}
