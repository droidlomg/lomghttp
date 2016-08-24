package com.lomg.lomghttp;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by lomg on 2016/8/20.
 */
public abstract class Request {
    public enum Method {
        POST, GET
    }

    Lomg mLomg;
    String mUrl;
    String mHeaders;
    HashMap<String, String> mHeaderMap;
    String mContent;
    Object mTag;

    Lomg getLomg() {
        return mLomg;
    }

    public String getUrl() {
        return mUrl;
    }


    Object getTag() {
        return mTag;
    }

    /**
     * to build a request
     * Created by lomg on 2016/8/20.
     */
    public static class RequestBuilder implements Serializable {
        private static final String MEDIA_TYPE_MARKDOWN = "multipart/form-data; charset=utf-8";
        private static final String COLONSPACE = ": ";
        private static final String CRLF = "\r\n";
        private static final String DASHDASH = "--";
        private static final String CHAR_SET = "utf-8";

        private final Lomg mLomg;
        private String mUrl;
        private HashMap<String, String> mHeaders;
        HashMap<String, Object> mUrlParams;
        private ArrayList<Serializable> mSerializable;
        private HashMap<String, File> mFileParams;
        private Object mTag;
        private Method mMethod;
        private boolean mIsMutilpart = false;
        private String boundary;
        private LomgCallBack callBack;

        public RequestBuilder(Lomg lomg, String url) {
            this.mUrl = url;
            this.mLomg = lomg;
            mHeaders = new HashMap<>();
            mUrlParams = new HashMap<>();
            mFileParams = new HashMap<>();
            mSerializable = new ArrayList<>();
            boundary = UUID.randomUUID().toString();
        }

        public RequestBuilder addHeader(String key, String value) {
            if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value))
                mHeaders.put(key, value);
            return this;
        }

        public RequestBuilder addHeader(Map<String, String> headers) {
            mHeaders.putAll(headers);
            return this;
        }

        public RequestBuilder addParam(String key, Object value) {
            if (!TextUtils.isEmpty(key) && value != null && !TextUtils.isEmpty(value.toString()))
                mUrlParams.put(key, value);
            return this;
        }

        public RequestBuilder addPrarm(String key, File file) {
            if (!TextUtils.isEmpty(key) && file != null && !file.isDirectory())
                mFileParams.put(key, file);
            return this;
        }

        public RequestBuilder clearHeaders() {
            mHeaders.clear();
            return this;
        }

        public RequestBuilder tag(Object tag) {
            this.mTag = tag;
            return this;
        }

        public RequestBuilder setMutipart(boolean mutipart) {
            mIsMutilpart = mutipart;
            return this;
        }

        public Request post(LomgCallBack lomgCallBack) {
            mMethod = Method.POST;
            callBack = lomgCallBack == null ? EmptyCallback.EmptyCallback : lomgCallBack;
            return mLomg.enqueue(build());
        }

        public Request get(LomgCallBack lomgCallBack) {
            mMethod = Method.GET;
            if (mIsMutilpart) {
                throw new RuntimeException("Can't use GET when set mIsMutilpart == true");
            }
            callBack = lomgCallBack == null ? EmptyCallback.EmptyCallback : lomgCallBack;
            return mLomg.enqueue(build());
        }

        synchronized AsyncRequest build() {
            AsyncRequest request = new AsyncRequest();
            request.mLomg = mLomg.copy();
            request.mTag = mTag;
            request.mUrl = mUrl;
            request.lomgCallBack = callBack;
            request.mHeaderMap = mHeaders;
            if (!mUrlParams.isEmpty() || !mFileParams.isEmpty()) {
                if (mMethod == Method.GET) {
                    processGetContent(request);
                } else if (mMethod == Method.POST) {
                    processPostContent(request);
                }
            }
            processHeader(request);
            return request;
        }

        private void processPostContent(Request request) {
            StringBuilder stringBuilder = new StringBuilder();
            if (mFileParams.isEmpty() && !mIsMutilpart) {
                Iterator<Map.Entry<String, Object>> iterator = mUrlParams.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> entry = iterator.next();
                    stringBuilder.append(entry.getKey());
                    stringBuilder.append("=");
                    stringBuilder.append(entry.getValue());
                    stringBuilder.append("&");
                }
                stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
                request.mContent = stringBuilder.toString();
                //tell header content length
                mHeaders.put("Content-Length", String.valueOf(request.mContent.length()));
            } else {
                //multipart
                processPostParam(stringBuilder);
                processPostFile(stringBuilder);
                addBehind(stringBuilder);
                //set to header
                request.mContent = stringBuilder.toString();
                //tell header content length
                mHeaders.put("Content-Length", String.valueOf(request.mContent.length()));
                mHeaders.put("Content-Type", "multipart/form-data; charset="+ CHAR_SET +"; boundary=" + boundary);
            }
        }

        private void processPostFile(StringBuilder stringBuilder) {
            Set<Map.Entry<String, File>> entrys = mFileParams.entrySet();
            for (Iterator<Map.Entry<String, File>> iterator = entrys.iterator();
                 iterator.hasNext(); ) {
                Map.Entry<String, File> temp = iterator.next();
                addPostFileFront(stringBuilder, temp);
                stringBuilder.append(Util.readFile(temp.getValue()));
            }

        }

        private void addBehind(StringBuilder stringBuilder) {
            stringBuilder.append(DASHDASH)
                    .append(boundary)
                    .append(DASHDASH)
                    .append(CRLF);
        }

        private void processPostParam(StringBuilder stringBuilder) {
            Set<Map.Entry<String, Object>> entrys = mUrlParams.entrySet();
            for (Iterator<Map.Entry<String, Object>> iterator = entrys.iterator();
                 iterator.hasNext(); ) {
                Map.Entry<String, Object> temp = iterator.next();
                addPostUrlFront(stringBuilder, temp);
                stringBuilder.append(temp.getValue());
                stringBuilder.append(CRLF);
            }
        }

        private void addPostUrlFront(StringBuilder stringBuilder, Map.Entry temp) {
            stringBuilder.append(DASHDASH);
            stringBuilder.append(boundary);
            stringBuilder.append(CRLF);
            stringBuilder.append("Content-Disposition")
                    .append(COLONSPACE)
                    .append("form-data; name=")
                    .append(temp.getKey())
                    .append(CRLF);
            stringBuilder.append(CRLF);
        }
        private void addPostFileFront(StringBuilder stringBuilder, Map.Entry temp) {
            stringBuilder.append(DASHDASH);
            stringBuilder.append(boundary);
            stringBuilder.append(CRLF);
            stringBuilder.append("Content-Type: form-data")
                    .append(CRLF);
            stringBuilder.append("Content-Disposition")
                    .append(COLONSPACE)
                    .append("application/octet-stream; name=")
                    .append(temp.getKey())
                    .append(CRLF);
            stringBuilder.append(CRLF);
        }

        private void processGetContent(Request request) {
            // GET only accept urlParams
            if (!mFileParams.isEmpty() || mIsMutilpart) {
                throw new RuntimeException("Can't add file params when use GET");
            }
            if (!mUrlParams.isEmpty()) {
                StringBuilder temp = new StringBuilder();
                Iterator<Map.Entry<String, Object>> iterator = mUrlParams.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Object> entry = iterator.next();
                    temp.append("&")
                            .append(entry.getKey())
                            .append("=")
                            .append(entry.getValue());
                }
                temp.replace(0, 1, "?");
                request.mUrl += temp.toString();
            }
        }

        private void processHeader(Request request) {
            StringBuilder stringBuilder = new StringBuilder();
            if (!mHeaders.isEmpty()) {
                for (Iterator<Map.Entry<String, String>> iterator = mHeaders.entrySet().iterator();
                     iterator.hasNext(); ) {
                    Map.Entry<String, String> entry = iterator.next();
                    stringBuilder.append(entry.getKey())
                            .append(COLONSPACE)
                            .append(entry.getValue())
                            .append(CRLF);
                }
                request.mHeaders = stringBuilder.toString();
            }
        }

        private enum EmptyCallback implements LomgCallBack {
            EmptyCallback;

            public void onFail(Request request, IOException exception) {
            }

            public void onSuccess(Response response) throws IOException {
            }
        }
    }

    public abstract Request requestCancel();

}
