package se.mtm.rmi.pool.example;

import java.rmi.RemoteException;
import java.util.logging.Logger;

import se.mtm.rmi.pool.server.PoolServerProcess;

public class ExamplePoolServerProcess extends PoolServerProcess<ExampleTaskProcess> implements StringRemote {
	
	@Override
	public String process(String args) throws RemoteException {
		Logger.getLogger(this.getClass().getCanonicalName()).fine("Process: " + args);
		ExampleTaskProcess t = null;
		try {
			t = getProcess();
			String ret = t.process(args);
			Logger.getLogger(this.getClass().getCanonicalName()).fine("Return: " + ret);
			return ret;
		} finally {
			if (t!=null) {
				releaseProcess(t);
			}
		}
	}
	
	@Override
	protected ExampleTaskProcess newInstance() {
		return new ExampleTaskProcess();
	}

	@Override
	public String getServiceName() {
		return StringRemote.class.getCanonicalName();
	}
}
