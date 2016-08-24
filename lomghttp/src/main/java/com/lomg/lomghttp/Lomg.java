package com.lomg.lomghttp;

import com.lomg.lomghttp.Request.RequestBuilder;

import java.io.Serializable;

/**
 * It can has more than one object, each object has its own dispatcher and thread pool.
 * Created by lomg on 2016/8/20.
 */
public class Lomg implements Serializable {
    private static final int TIME_OUT = 15 * 1000;

    private String baseUrl;

    private Dispatcher dispatcher;

    int readTimeOut = TIME_OUT;

    int connectTimeOut = TIME_OUT;

    private Lomg() {

    }

    public RequestBuilder withPath(String path) {
        return new RequestBuilder(this, baseUrl + path);
    }

    Lomg copy() {
        Lomg lomg = new Lomg();
        lomg.baseUrl = baseUrl;
        lomg.dispatcher = dispatcher;
        lomg.readTimeOut = readTimeOut;
        lomg.connectTimeOut = connectTimeOut;
        return lomg;
    }

    public void setConnnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    Request enqueue(AsyncRequest request) {
        dispatcher.dispatch(request);
        return request;
    }

    void finishRequest(AsyncRequest tag) {
        dispatcher.finish(tag);
    }

    public void cancel(Object tag) {
        dispatcher.cancel(tag);
    }

    public static class Builder {
        private String baseUrl;

        public Builder(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Lomg build() {
            if (baseUrl == null) {
                throw new IllegalStateException("Base URL required.");
            }
//            if (!baseUrl.endsWith("/")) {
//                throw new IllegalStateException("Base URL must start with '/'.");
//            }
            Lomg result = new Lomg();
            result.baseUrl = baseUrl;
            result.dispatcher = new Dispatcher();
            return result;
        }
    }
}
