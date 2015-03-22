package se.mtm.rmi.pool.example;

import java.rmi.RemoteException;

import se.mtm.rmi.pool.server.PoolServerProcess;

public class ExamplePoolServerProcess extends PoolServerProcess<ExampleTaskProcess> implements StringRemote {
	
	@Override
	public String process(String args) throws RemoteException {
		System.err.println("Process: " + args);
		ExampleTaskProcess t = getProcess();
		String ret = t.process(args);
		releaseProcess(t);
		return ret;
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
