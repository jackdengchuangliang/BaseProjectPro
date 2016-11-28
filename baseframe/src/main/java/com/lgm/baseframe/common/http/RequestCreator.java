package com.lgm.baseframe.common.http;

import com.alibaba.fastjson.JSON;
import com.lgm.baseframe.model.RequestResult;
import com.lgm.baseframe.ui.IBaseView;

import java.util.Map;

/**
 * Created by mfwn on 2016/11/10.
 */

public class RequestCreator {
    private String requestUrl;
    private Map<String, Object> requestParams;
    private HttpUtil.RequestMethod requestMethod = HttpUtil.RequestMethod.GET;
    private HttpCallbackListener callbackListener;
    private HttpUtil.RequestBodyType type = HttpUtil.RequestBodyType.FORM;

    private IBaseView view;
    private HttpUtil httpUtil;

    private boolean isShowProgressDialog = false;
    private boolean isShowErrorToast = false;


    public RequestCreator(String url) {
        if (url == null || url.equals("")) {
            throw new IllegalArgumentException("url can not be null");
        }
        requestUrl = url;
    }


    public RequestCreator params(Map<String, Object> params) {
        requestParams = params;
        return this;
    }

    public RequestCreator injectView(IBaseView view) {
        this.view = view;
        return this;
    }


    public RequestCreator tag(Object object) {
        RequestUtil.getInstance().tag(object, this);
        return this;
    }

    public RequestCreator showProgressDialog(Object object) {
        isShowProgressDialog = true;
        return this;
    }


    public RequestCreator showErrorToast(Object object) {
        isShowErrorToast = true;
        return this;
    }


    public void post() {
        requestMethod = HttpUtil.RequestMethod.POST;
        request();
    }

    public void get() {
        requestMethod = HttpUtil.RequestMethod.GET;
        request();
    }

    public void put() {
        requestMethod = HttpUtil.RequestMethod.PUT;
        request();
    }


    public void delete() {
        requestMethod = HttpUtil.RequestMethod.DELETE;
        request();
    }

    public void startRequest() {
        request();
    }


    public RequestCreator requestMethod(HttpUtil.RequestMethod method) {
        requestMethod = method;
        return this;
    }

    public RequestCreator callback(HttpCallbackListener listener) {
        callbackListener = listener;
        return this;
    }

    public RequestCreator requestType(HttpUtil.RequestBodyType type) {
        this.type = type;
        return this;
    }

    private void request() {
        httpUtil = new HttpUtil();
        switch (requestMethod) {
            case DELETE:
                httpUtil.delete(requestUrl, requestParams,
                        type, new TopHttpCallbackListener(callbackListener));
                break;
            case GET:
                httpUtil.get(requestUrl, requestParams,
                        new TopHttpCallbackListener(callbackListener));
                break;
            case POST:
                httpUtil.post(requestUrl, requestParams,
                        type, new TopHttpCallbackListener(callbackListener));
                break;
            case PUT:
                httpUtil.put(requestUrl, requestParams,
                        type, new TopHttpCallbackListener(callbackListener));
                break;
        }
        if (view != null && isShowProgressDialog) {
            view.showLoading();
        }
    }

    public void cancel() {
        if (httpUtil != null) {
            httpUtil.disConnect();
        }
    }

    class TopHttpCallbackListener extends HttpCallbackListener {
        HttpCallbackListener httpCallbackListener;

        public TopHttpCallbackListener(HttpCallbackListener customListener) {
            this.httpCallbackListener = customListener;
        }

        @Override
        public void onRequestSuccess(String result, Object requestData) {
            if (view != null) {
                view.hideLoading();
            }
            RequestResult requestResult = JSON.parseObject(result, RequestResult.class);
            if (requestResult.status == 0) {
                if (httpCallbackListener != null) {
                    httpCallbackListener.onRequestSuccess(requestResult.result, requestData);
                }
            } else {
                if (httpCallbackListener != null) {
                    boolean b = httpCallbackListener.onRequestFailed(requestResult.status, requestResult.resultMsg);
                    if (view != null && isShowErrorToast&&!b) {
                        view.onError(requestResult.status, requestResult.resultMsg);
                    }
                }
            }
        }

        @Override
        public boolean onRequestFailed(int resultCode, String resultMsg) {
            if (view != null) {
                view.hideLoading();
            }
            if (httpCallbackListener != null) {
                if (!httpCallbackListener.onRequestFailed(resultCode, resultMsg)) {
                    //TODO
                }
            }
            return false;
        }

        @Override
        public void onConnectionFailed(Exception ex) {
            if (view != null) {
                view.hideLoading();
            }
            if (httpCallbackListener != null) {
                view.onConnectionFailed(ex);
                httpCallbackListener.onConnectionFailed(ex);
            }
        }

        @Override
        public void onHttpStateError(String result, int statusCode) {
            if (view != null) {
                view.hideLoading();
            }
            if (httpCallbackListener != null) {
                view.onHttpStateError(result, statusCode);
                httpCallbackListener.onHttpStateError(result, statusCode);
            }
        }
    }

}
