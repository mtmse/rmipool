package se.mtm.rmi.pool.server;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import se.mtm.rmi.pool.api.PoolRemote;

public class PoolServerManager {
	private final PoolServerProcess<?> remote;
	
	public PoolServerManager(PoolServerProcess<?> remote) {
		this.remote = remote;
	}

	public void startServer(String name) throws RemoteException {
		Registry registry = LocateRegistry.createRegistry(PoolRemote.SERVICE_PORT);
		PoolRemote stub = (PoolRemote)UnicastRemoteObject.exportObject(remote, 0);
		registry.rebind(name, stub);
	}
	
	public void stopServer(String name) throws RemoteException {
		Registry registry = LocateRegistry.getRegistry(PoolRemote.SERVICE_PORT);
		try {
			registry.unbind(name);
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		UnicastRemoteObject.unexportObject(remote, true);
	}
	
	public static void stopService(String name) {
        try {
    		Registry registry = LocateRegistry.getRegistry(PoolRemote.SERVICE_PORT);
			PoolRemote server = (PoolRemote) registry.lookup(name);
			server.stop();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void waitFor() {
		while (remote.alive()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
