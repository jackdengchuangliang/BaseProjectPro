package com.lgm.baseframe.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lgm.baseframe.common.LogUtil;
import com.lgm.baseframe.common.Utils;
import com.lgm.baseframe.common.http.RequestUtil;
import com.lgm.baseframe.ui.IBaseView;

import butterknife.ButterKnife;


/**
 * Created by Administrator on 2015/12/28.
 */
public abstract class BaseFragment extends Fragment implements View.OnClickListener,IBaseView {

	private View rootView;



	public View getRootView() {
		return rootView;
	}


	public void doOnActivityCreated() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		if (getLayoutId() == 0) {
			return null;
		}
		if (rootView == null) {
			rootView = inflater.inflate(getLayoutId(), null, false);
		}
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		ButterKnife.bind(this, rootView);
		return rootView;
	}



	protected abstract int getLayoutId();

	protected void getViews() {
	}


	protected void setListeners() {
	}


	@SuppressWarnings("unchecked")
	protected <T extends View> T findViewById(int id) {
		return (T) getView().findViewById(id);
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getViews();
		setListeners();

	}



	public String getPageName() {
		return getClass().getName();
	}


	/**
	 * 得到根Fragment
	 *
	 * @return
	 */
	public Fragment getRootFragment() {
		Fragment fragment = getParentFragment();
		while (fragment.getParentFragment() != null) {
			fragment = fragment.getParentFragment();
		}
		return fragment;

	}

	public void showShortToast(String string) {
		Toast.makeText(getActivity(), string, Toast.LENGTH_SHORT).show();
	}


	@Override
	public void onClick(View v) {

	}

	public boolean onBackPressed() {
		return false;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		RequestUtil.getInstance().cancel(toString());
		ButterKnife.unbind(this);
	}

	@Override
	public void showLoading() {

		try {
			((BaseActivity)getActivity()).showLoading();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void hideLoading() {
		try {
			((BaseActivity)getActivity()).hideLoading();
		} catch (Exception e) {
			LogUtil.i("test","hideLoadingerror");
			e.printStackTrace();
		}
	}

	@Override
	public void onError(int errorCode, String errorMsg) {

	}

	@Override
	public void onConnectionFailed(Exception ex) {
		LogUtil.i("result", "onConnectionFailed");
		Utils.showShortToast(getContext(), "网络链接异常");
		ex.printStackTrace();
	}

}
