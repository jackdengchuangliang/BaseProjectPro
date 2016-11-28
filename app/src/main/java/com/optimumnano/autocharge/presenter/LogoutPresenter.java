package com.optimumnano.autocharge.presenter;

import android.content.Context;

import com.lgm.baseframe.common.http.HttpUtil;
import com.lgm.baseframe.common.http.PersistentCookieStore;
import com.lgm.baseframe.common.http.RequestUtil;
import com.optimumnano.autocharge.common.Constant;
import com.optimumnano.autocharge.view.ILogoutView;

import java.util.HashMap;
import java.util.Objects;

/**
 * 作者：刘广茂 on 2016/11/25 11:07
 * <p>
 * 邮箱：liuguangmao@optimumchina.com
 */
public class LogoutPresenter extends BasePresenter<ILogoutView> {
    public LogoutPresenter(ILogoutView mView) {
        super(mView);
    }
    public void logout(){
        RequestUtil.url(Constant.URL_LOGOUT)
                .requestType(HttpUtil.RequestBodyType.JSON)
                .params(getRequestParams(new HashMap<String,Object>()))
                .post();
        PersistentCookieStore persistentCookieStore = new PersistentCookieStore(((Context) getView()).getApplicationContext());
        persistentCookieStore.removeAll();
    }
}
