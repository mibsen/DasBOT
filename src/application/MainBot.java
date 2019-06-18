package application;

import java.util.concurrent.Callable;

import boot.Boot;
import bot.Bot;

public class MainBot {

	public static void main(String[] args) {
		
		Bot.RUNTIME_IN_MS = System.currentTimeMillis();
		
		System.out.println("LOADING ROBOT...");
		
		
		Bot bot = new Bot();
		
		bot.load();
		
		System.out.println("ROBOT FINNISHED");


	}
}
