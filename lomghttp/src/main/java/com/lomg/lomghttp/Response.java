package com.lomg.lomghttp;

/**
 * Created by lomg on 2016/8/20.
 */
public class Response {

    public byte[] body;

    public int httpCode;

    public Request request;

    public Response(byte[] body, int httpCode, Request request) {
        this.body = body;
        this.httpCode = httpCode;
        this.request = request;
    }


}
