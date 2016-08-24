package com.lomg.lomghttp;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by lomg on 2016/8/20.
 */
public class Dispatcher {

    private static final int MSG_SUBMIT_REQUEST = 1;
    private static final int MSG_CANCEL_REQUEST = 2;
    private static final int MSG_FINISH_REQUEST = 3;

    private Deque<AsyncRequest> mRunningRequests;

    private HandlerThread mDishpatcherThread = new HandlerThread("LomgHttp - dispatcher",
            Process.THREAD_PRIORITY_BACKGROUND);
    /**
     * Executes calls. Created lazily.
     */
    private ThreadPoolExecutor mThreadPoolExecutor;

    private Handler handler;

    public Dispatcher() {
        mRunningRequests = new LinkedBlockingDeque<>();
        mThreadPoolExecutor = new LomgExecutorService();
        mDishpatcherThread.start();
        handler = new LomgDispatcherHandler(mDishpatcherThread.getLooper(), this);
    }

    public AsyncRequest dispatch(AsyncRequest asyncRequest) {
        handler.obtainMessage(MSG_SUBMIT_REQUEST, asyncRequest).sendToTarget();
        return asyncRequest;
    }

    public void cancel(Object tag) {
        handler.obtainMessage(MSG_CANCEL_REQUEST, tag).sendToTarget();
    }

    void performSubmit(AsyncRequest requestSubmit) {
        mRunningRequests.add(requestSubmit);
        requestSubmit.future = mThreadPoolExecutor.submit(requestSubmit);
    }

    void performCancel(Object tag) {
        for (AsyncRequest request : mRunningRequests) {
            Object requestTag = request.getTag();
            if ((tag == requestTag) || (requestTag != null && requestTag.equals(tag))) {
                request.cancel();
                mRunningRequests.remove(request);
                break;
            }
        }

    }

    void finish(AsyncRequest asyncRequest) {
        handler.obtainMessage(MSG_FINISH_REQUEST, asyncRequest).sendToTarget();
    }

    private static class LomgDispatcherHandler extends Handler {
        private Dispatcher dispatcher;

        public LomgDispatcherHandler(Looper looper, Dispatcher dispatcher) {
            super(looper);
            this.dispatcher = dispatcher;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SUBMIT_REQUEST:
                    dispatcher.performSubmit((AsyncRequest) msg.obj);
                    break;
                case MSG_CANCEL_REQUEST:
                    dispatcher.performCancel(msg.obj);
                    break;
                case MSG_FINISH_REQUEST:
                    dispatcher.performFinish((AsyncRequest) msg.obj);
                    break;
            }
        }
    }

    private void performFinish(AsyncRequest asyncRequest) {
        mRunningRequests.remove(asyncRequest);
    }
}
