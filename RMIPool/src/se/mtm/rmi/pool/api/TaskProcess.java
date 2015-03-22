package se.mtm.rmi.pool.api;

public interface TaskProcess {
	
	public long getCreationTime();
	public String process(String input);

}
