package com.lomg.lomghttp;

import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Created by lomg on 2016/8/20.
 */
public class AsyncRequest extends Request implements Runnable {

    LomgCallBack lomgCallBack;
    private RequestBuilder request;
    private int priority;
    private boolean canceled = false;
    public Future<?> future;

    public AsyncRequest() {
    }

    @Override
    public void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(String.format("LomgHttp request thread  %s", mUrl));
        try {
            execute();
        } finally {
            Thread.currentThread().setName(oldName);
        }
    }

    private void execute() {
        if (canceled) {
            return;
        }
        HttpURLConnection conn = null;
        try {
            conn = getHttpURLConnection();

            generateHeader(conn);

            conn.connect();

            sendRequest(conn);

            ByteArrayOutputStream byteArrayOutputStream = getResponse(conn);

            lomgCallBack.onSuccess(new Response(byteArrayOutputStream.toByteArray(), conn.getResponseCode(), this));
        } catch (Exception e) {
            e.printStackTrace();
            lomgCallBack.onFail(this, new IOException(e.getMessage()));
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            mLomg.finishRequest(this);
        }


    }

    @NonNull
    private ByteArrayOutputStream getResponse(HttpURLConnection conn) throws IOException {
        InputStream inputStream = conn.getInputStream();
        int length;
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while ((length = inputStream.read(buffer)) > 0) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream;
    }

    private void sendRequest(HttpURLConnection conn) throws IOException {
        if (mContent != null) {
            OutputStream outputStream = conn.getOutputStream();
            DataOutputStream dos = new DataOutputStream(outputStream);
            dos.write(mContent.getBytes("utf-8"));
            dos.flush();
        }
    }

    private void generateHeader(HttpURLConnection conn) {
        Set<Map.Entry<String, String>> entrySet = mHeaderMap.entrySet();
        for (Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
             iterator.hasNext(); ) {
            Map.Entry<String, String> temp = iterator.next();
            conn.setRequestProperty(temp.getKey(), temp.getValue());
        }
    }

    @NonNull
    private HttpURLConnection getHttpURLConnection() throws IOException {
        URL url;
        HttpURLConnection conn;
        url = new URL(mUrl);
        conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(mLomg.readTimeOut);
        conn.setConnectTimeout(mLomg.connectTimeOut);
        conn.setRequestMethod("POST"); //请求方式
        conn.setRequestProperty("Charset", "utf-8");//设置编码
        conn.setRequestProperty("connection", "keep-alive");
        conn.setDoInput(true); //允许输入流
        conn.setDoOutput(true); //允许输出流
        conn.setUseCaches(false); //不允许使用缓存
        return conn;
    }

    public Request requestCancel() {
        getLomg().cancel(getTag());
        return this;
    }

    public int getPriority() {
        return priority;
    }

    void cancel() {
        canceled = true;
        priority = Process.THREAD_PRIORITY_LOWEST;
        lomgCallBack = null;
        request = null;
        future.cancel(false);
    }
}
