package se.mtm.rmi.pool.api;

import java.io.Closeable;

public interface TaskProcess extends Closeable {
	
	public long getCreationTime();

}
