package se.mtm.rmi.pool.client;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import se.mtm.rmi.pool.api.PoolRemote;

/**
 * Provides a pool client.
 * 
 * @author Joel Håkansson
 */
public class PoolClient {

	public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(PoolRemote.SERVICE_PORT);
            PoolRemote server = (PoolRemote) registry.lookup(PoolRemote.SERVICE_NAME);
            LineNumberReader ln = new LineNumberReader(new InputStreamReader(System.in));
            String line;
            while (!(line=ln.readLine()).equals("")) {
            	System.out.println(server.process(line));
            }
        } catch (Exception e) {
            System.err.println("RMI connection error.");
            e.printStackTrace();
        }
	}
}