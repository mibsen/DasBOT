package application;

import java.util.concurrent.Callable;

import boot.Boot;
import bot.Bot;

public class Main {

	public static void main(String[] args) {

		boolean skip = false;

		if (!skip) {
			Boot b = new Boot();

			b.after(new Callable() {

				@Override
				public Object call() throws Exception {
					Bot bot = new Bot(skip);
					bot.load();

					System.out.println("DONE With tha shit!");
					return null;
				}
			});

			b.load();
		} else {
			System.out.println("Skipping configuration");
			Bot bot = new Bot(skip);

			bot.load();
		}

	}
}
