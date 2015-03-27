package se.mtm.rmi.pool.server;

import java.rmi.RemoteException;
import java.util.Vector;
import java.util.logging.Logger;

import se.mtm.rmi.pool.api.PoolRemote;
import se.mtm.rmi.pool.api.TaskProcess;

public abstract class PoolServerProcess<T extends TaskProcess> implements PoolRemote {
	private int count = 0;
	private boolean stop = false;
	private int maxProcesses = 1;
	private int ageLimit = -1;
	private int serverAgeLimit = -1;
	private Vector<T> threads;
	private Vector<Object> waiting;
	
	public PoolServerProcess() {
		this.threads = new Vector<T>();
		this.waiting = new Vector<Object>();
	}
	
	public int getMaxProcesses() {
		return maxProcesses;
	}

	public void setMaxProcesses(int maxProcesses) {
		this.maxProcesses = maxProcesses;
	}

	public int getAgeLimit() {
		return ageLimit;
	}

	public void setAgeLimit(int ageLimit) {
		this.ageLimit = ageLimit;
	}

	public int getServerAgeLimit() {
		return serverAgeLimit;
	}

	public void setServerAgeLimit(int serverAgeLimit) {
		this.serverAgeLimit = serverAgeLimit;
	}

	protected T getProcess() {
		while (true) {
			if (threads.size()>0 || count < maxProcesses) {
				synchronized (threads) {
					if (threads.size()>0) {
						count ++;
						T ret = threads.remove(0);
						if (ageLimit>=0 && System.currentTimeMillis()-ret.getCreationTime()>ageLimit) {
							Logger.getLogger(this.getClass().getCanonicalName()).fine("Discarding instance due to old age.");
							ret = newInstance();
						}
						return ret;
					} else if (count < maxProcesses) {
						count ++;
						return newInstance();
					}
				}
			} else {
				try {
					waiting.add(this);
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	protected abstract T newInstance();
	
	public abstract String getServiceName();

	protected void releaseProcess(T t) {
		synchronized (threads) {
			threads.add(t);
		}
		count--;
		synchronized (waiting) {
			if (waiting.size()>0) {
				Object o = waiting.remove(0);
				synchronized (o) {
					o.notify();
				}
			}
		}
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
					Logger.getLogger(this.getClass().getCanonicalName()).fine("Discarding: " + threads.size());
				}
			}
		}
	}

	@Override
	public void stop() throws RemoteException {
		stop = true;
	}

}
