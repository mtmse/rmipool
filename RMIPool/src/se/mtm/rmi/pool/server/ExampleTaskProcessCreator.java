package se.mtm.rmi.pool.server;

import se.mtm.rmi.pool.api.TaskProcess;
import se.mtm.rmi.pool.api.TaskProcessCreator;

public class ExampleTaskProcessCreator implements TaskProcessCreator {

	@Override
	public TaskProcess newInstance() {
		return new ExampleTaskProcess();
	}

}
