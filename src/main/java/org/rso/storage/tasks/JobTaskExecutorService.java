package org.rso.storage.tasks;

import java.util.concurrent.*;

/**
 * Created by Radosław on 23.05.2016.
 */
public class JobTaskExecutorService {
    private int corePoolSize;
    private int maxPoolSize;
    private long keepAliveTime;
    private BlockingQueue<Runnable> queue;
    private ExecutorService executorService;


    public JobTaskExecutorService(int corePoolSize, int maxPoolSize, long keepAliveTime) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.queue = new LinkedBlockingDeque<>();

        this.executorService = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.MINUTES,
                queue
        );
    }

    public void addNewTasks(Runnable...runnables){
        for(Runnable runnable: runnables){
            this.executorService.execute(runnable);
        }
    }

    public void  addNewTask(Runnable task){
        this.executorService.execute(task);
    }
}
