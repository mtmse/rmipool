package se.mtm.rmi.pool.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import se.mtm.rmi.pool.api.PoolRemote;
import se.mtm.rmi.pool.api.TaskProcessCreator;

public class PoolServrer {
	public final static String PROP_CREATOR = "creator";
	public final static String PROP_MAX_INSTANCES = "max-instances";
	public final static String PROP_AGE_LIMIT = "age-limit";
	public final static String PROP_BACKOFF_TIME = "backoff-time";
	
	public static void main(String[] args) throws RemoteException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (args.length>0 && args[0].equals("stop")) {
			stopInstance(PoolRemote.SERVICE_NAME);
			System.exit(0);
		} else if (args.length>=2 && args[0].equals("start")) {
			try {
				Properties p = getProperties(new File(args[1]));
				System.out.println("Starting server... ");
				Class<?> c = Class.forName(p.getProperty(PROP_CREATOR));
				Object processCreator = c.newInstance();
				if (processCreator instanceof TaskProcessCreator) {
					int count = Integer.parseInt(p.getProperty(PROP_MAX_INSTANCES));
					PoolServerProcess task = new PoolServerProcess.
							Builder(new ExampleTaskProcessCreator(), count).
							ageLimit(Integer.parseInt(p.getProperty(PROP_AGE_LIMIT))).
							backoffTime(Integer.parseInt(p.getProperty(PROP_BACKOFF_TIME))).
							build();
					startServer(task, PoolRemote.SERVICE_NAME);
					while (task.alive()) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					System.out.println("Stopping server...");
				} else {
					System.out.println("Instance is not an implementation of TaskProcessCreator: " + processCreator.getClass().getCanonicalName());
				}
			} finally {
				System.exit(0);
			}
		} else {
			System.out.println("Usage: <command> [args]");
			System.out.println("\tstart path_to_settings");
			System.out.println("\tstop");
		}
	}
	
	private static void startServer(PoolRemote remote, String name) throws RemoteException {
		PoolRemote stub = (PoolRemote)UnicastRemoteObject.exportObject(remote, 0);
		Registry registry = LocateRegistry.createRegistry(PoolRemote.SERVICE_PORT);
		registry.rebind(name, stub);
	}
	
	private static void stopInstance(String name) {
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
	
	private static Properties getProperties(File f) {
		Properties p = new Properties();
		if (!f.exists()) {
			try {
				p.put(PROP_CREATOR, TaskProcessCreator.class.getCanonicalName());
				p.put(PROP_MAX_INSTANCES, "2");
				p.put(PROP_AGE_LIMIT, ""+(1000*60*60));
				p.put(PROP_BACKOFF_TIME, "1000");
				p.storeToXML(new FileOutputStream(f), "Default template");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(-1);
		} else {
			try {
				p.loadFromXML(new FileInputStream(f));
			} catch (InvalidPropertiesFormatException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return p;
	}

}
