package com.lomg.lomghttp;

import android.os.Process;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

/**
 * Created by lomg on 2016/8/20.
 */
public class LomgExecutorService extends ThreadPoolExecutor {

    public LomgExecutorService() {
        super(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
    }

    @Override
    public Future<?> submit(Runnable task) {
        LomgFutureTask lomgFutureTask = new LomgFutureTask((AsyncRequest) task);
        execute(lomgFutureTask);
        return lomgFutureTask;
    }

    private static final class LomgFutureTask extends FutureTask implements Comparable<LomgFutureTask> {


        private final AsyncRequest mAsyncRequest;

        public LomgFutureTask(AsyncRequest runnable) {
            super(runnable, null);
            this.mAsyncRequest = runnable;
        }

        @Override
        public int compareTo(LomgFutureTask another) {
            int p1 = mAsyncRequest.getPriority();
            int p2 = another.mAsyncRequest.getPriority();
            return p1 - p2;
        }
    }
}
