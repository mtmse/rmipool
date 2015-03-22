package se.mtm.rmi.pool.example;

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
public class ExamplePoolClient {

	public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry(PoolRemote.SERVICE_PORT);
            StringRemote server = (StringRemote) registry.lookup(StringRemote.class.getCanonicalName());
            LineNumberReader ln = new LineNumberReader(new InputStreamReader(System.in));
            String line;
            while (!(line=ln.readLine()).equals("")) {
            	//TaskProcess t = server.getProcess();
            	System.out.println(server.process(line));
            	//server.releaseProcess(t);
            }
        } catch (Exception e) {
            System.err.println("RMI connection error.");
            e.printStackTrace();
        }
	}
}