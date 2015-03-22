package se.mtm.rmi.pool.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import se.mtm.rmi.pool.example.ExamplePoolServerProcess;

public class PoolServrer {
	public final static String PROP_IMPLEMENTATION = "implementation";
	public final static String PROP_MAX_INSTANCES = "max-instances";
	public final static String PROP_AGE_LIMIT = "age-limit";
	public final static String PROP_BACKOFF_TIME = "backoff-time";
	
	public static void main(String[] args) throws RemoteException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (args.length>=2 && args[0].equals("stop")) {
			Properties p = getProperties(new File(args[1]));
			Class<?> c = Class.forName(p.getProperty(PROP_IMPLEMENTATION));
			Object impl = c.newInstance();
			if (impl instanceof PoolServerProcess) {
				PoolServerProcess<?> task = (PoolServerProcess<?>)impl;
				PoolServerManager.stopService(task.getServiceName());
			}
		} else if (args.length>=2 && args[0].equals("start")) {
			try {
				Properties p = getProperties(new File(args[1]));
				System.out.println("Starting server... ");
				Class<?> c = Class.forName(p.getProperty(PROP_IMPLEMENTATION));
				Object impl = c.newInstance();
				if (impl instanceof PoolServerProcess) {
					int count = Integer.parseInt(p.getProperty(PROP_MAX_INSTANCES));
					PoolServerProcess<?> task = (PoolServerProcess<?>)impl;
					task.setMaxProcesses(count);
					task.setAgeLimit(Integer.parseInt(p.getProperty(PROP_AGE_LIMIT)));
					task.setBackoffTime(Integer.parseInt(p.getProperty(PROP_BACKOFF_TIME)));
					PoolServerManager manager = new PoolServerManager(task);
					manager.startServer(task.getServiceName());
					manager.waitFor();
					System.out.println("Stopping server...");
					manager.stopServer(task.getServiceName());
				} else {
					System.out.println("Instance is not an implementation of PoolServerProcess: " + impl.getClass().getCanonicalName());
				}
			} finally {
				//System.exit(0);
			}
		} else {
			System.out.println("Usage: <command> [args]");
			System.out.println("\tstart path_to_settings");
			System.out.println("\tstop path_to_settings");
		}
	}
	
	private static Properties getProperties(File f) {
		Properties p = new Properties();
		if (!f.exists()) {
			try {
				p.put(PROP_IMPLEMENTATION, ExamplePoolServerProcess.class.getCanonicalName());
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
