package bot.actions;

public class TestAction implements Action{

	public String data;
	
	
	public TestAction(String data) {
	 this.data = data;
	}
	
	@Override
	public void Perform() {
		System.out.println(data);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
