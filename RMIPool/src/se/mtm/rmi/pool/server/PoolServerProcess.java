package se.mtm.rmi.pool.server;

import java.rmi.RemoteException;
import java.util.Vector;

import se.mtm.rmi.pool.api.PoolRemote;
import se.mtm.rmi.pool.api.TaskProcess;
import se.mtm.rmi.pool.api.TaskProcessCreator;

public class PoolServerProcess implements PoolRemote {
	private int count = 0;
	private boolean stop = false;
	private final TaskProcessCreator creator;
	private final int maxProcesses;
	private final int ageLimit;
	private final int backoffTime;
	private Vector<TaskProcess> threads;
	
	public static class Builder {
		private final TaskProcessCreator creator;
		private final int processes;
		private int ageLimit = -1;
		private int backoffTime = 1000;
		public Builder(TaskProcessCreator creator, int processes) {
			this.creator = creator;
			this.processes = processes;
		}
		
		/**
		 * Sets the age limit for an instance
		 * @param value the value in milliseconds
		 * @return returns the builder
		 */
		public Builder ageLimit(int value) {
			this.ageLimit = value;
			return this;
		}
		
		public Builder backoffTime(int value) {
			this.backoffTime = value;
			return this;
		}
		
		public PoolServerProcess build() {
			return new PoolServerProcess(this);
		}
	}
	
	private PoolServerProcess(Builder builder) {
		this.threads = new Vector<TaskProcess>(builder.processes);
		this.maxProcesses = builder.processes;
		this.creator = builder.creator;
		this.ageLimit = builder.ageLimit;
		this.backoffTime = builder.backoffTime;
	}

	@Override
	public String process(String args) throws RemoteException {
		System.err.println("Process: " + args);
		TaskProcess t = getProcess();
		String ret = t.process(args);
		releaseProcess(t);
		return ret;
	}
	
	private TaskProcess getProcess() {
		while (true) {
			if (threads.size()>0 || count < maxProcesses) {
				synchronized (threads) {
					if (threads.size()>0) {
						count ++;
						TaskProcess ret = threads.remove(0);
						if (ageLimit>=0 && System.currentTimeMillis()-ret.getCreationTime()>ageLimit) {
							System.out.println("Discarding instance due to old age.");
							ret = creator.newInstance();
						}
						return ret;
					} else if (count < maxProcesses) {
						count ++;
						return creator.newInstance();
					}
				}
			} else {
				try {
					//Sleep for backoffTime-backoffTime*2 ms
					int sleep = (int)(Math.random()*backoffTime)+backoffTime;
					System.err.println("Waiting for thread: " + sleep);
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void releaseProcess(TaskProcess t) {
		synchronized (threads) {
			threads.add(t);
		}
		count--;
	}
	
	public boolean alive() {
		//about every 20 time, check age
		if (ageLimit>=0 && Math.random()<0.05) {
			discardOldThreads();
		}
		return !stop || count>0;
	}
	
	private void discardOldThreads() {
		synchronized (threads) {
			for (int i=threads.size()-1;i>=0; i--) {
				TaskProcess t = threads.elementAt(i);
				if (ageLimit>=0 && System.currentTimeMillis()-t.getCreationTime()>ageLimit) {
					threads.remove(0);
					System.out.println("Discarding: " + threads.size());
				}
			}
		}
	}

	@Override
	public void stop() throws RemoteException {
		stop = true;
	}
}
