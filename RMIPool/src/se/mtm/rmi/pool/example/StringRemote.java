package se.mtm.rmi.pool.example;

import java.rmi.RemoteException;

import se.mtm.rmi.pool.api.PoolRemote;

public interface StringRemote extends PoolRemote {

	public String process(String args) throws RemoteException;

}
