package application;

import boot.Boot;

public class MainConfig {
	
	public static void main(String[] args) {
		
		System.out.println("BOOTING.. BOOTING.. BOOTING..");

		Boot b = new Boot();
		
		b.load();
		
		System.out.println("BOOTING IS COMPLETE!");
		
		
	}
	
	

}
