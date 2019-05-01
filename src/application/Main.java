package application;
	
import java.util.concurrent.Callable;

import boot.Boot;
import bot.Bot;

public class Main {
	
	public static void main(String[] args) {
		Boot b = new Boot();
		
		b.after(new Callable() {

			@Override
			public Object call() throws Exception {
				Bot bot = new Bot();
				bot.load();
				
				System.out.println("DONE With tha shit!");
				return null;
			}
		});
		
		b.load();
	}
}
