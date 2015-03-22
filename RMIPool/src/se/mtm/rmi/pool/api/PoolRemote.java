package se.mtm.rmi.pool.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PoolRemote extends Remote {
	public static String SERVICE_NAME = "rmi-process-pool";
	public static int SERVICE_PORT = 1099;

	public void stop() throws RemoteException;
	public String process(String args) throws RemoteException;

}
