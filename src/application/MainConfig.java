package application;

import java.util.concurrent.Callable;

import boot.Boot;

public class MainConfig {
	
	public static void main(String[] args) {
		
		System.out.println("BOOTING.. BOOTING.. BOOTING..");

		Boot b = new Boot();
		
		b.after(new Callable() {

			@Override
			public Object call() throws Exception {
				System.out.println("EXIT");
				System.exit(1);
				return null;
			}
		});
		
		b.load();
		
		System.out.println("BOOTING IS COMPLETE!");
		
		
	}
	
	

}
