package com.lomg.lomghttp;

import java.io.IOException;

/**
 * Created by lomg on 2016/8/20.
 */
public interface LomgCallBack {
    void onFail(Request request, IOException exception);
    void onSuccess(Response response) throws IOException;
}
