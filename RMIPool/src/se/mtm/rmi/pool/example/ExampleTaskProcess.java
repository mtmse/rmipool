package se.mtm.rmi.pool.example;

import se.mtm.rmi.pool.api.TaskProcess;

class ExampleTaskProcess implements TaskProcess {
	
	private long creationTime = System.currentTimeMillis();

	public String process(String input) {
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		char[] c = new char[input.length()];
		for (int i=0; i<input.length(); i++) {
			c[input.length()-(i+1)] = input.charAt(i);
		}
		return new String(c);
	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public void close() {
	}
	
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
