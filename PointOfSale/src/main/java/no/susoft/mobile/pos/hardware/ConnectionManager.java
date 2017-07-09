package no.susoft.mobile.pos.hardware;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ConnectionManager implements Executor {

	private static ConnectionManager instance;

	final Queue<Runnable> tasks = new ArrayDeque();
    final Executor executor;
    Runnable active;

    public static ConnectionManager getInstance() {
        if (instance == null)
            instance = new ConnectionManager();
        return instance;
    }

	public ConnectionManager() {
		executor = Executors.newSingleThreadExecutor();
	}

    public synchronized void execute(final Runnable r) {
        tasks.offer(new Runnable() {
            public void run() {
                try {
                    r.run();
                } finally {
                    scheduleNext();
                }
            }
        });
        if (active == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((active = tasks.poll()) != null) {
            executor.execute(active);
        }
    }
}