package com.longshine.electriccars.baidu.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.navisdk.adapter.BNRouteGuideManager;
import com.baidu.navisdk.adapter.BNRouteGuideManager.CustomizedLayerItem;
import com.baidu.navisdk.adapter.BNRouteGuideManager.OnNavigationListener;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviBaseCallbackModel;
import com.baidu.navisdk.adapter.BaiduNaviCommonModule;
import com.baidu.navisdk.adapter.NaviModuleFactory;
import com.baidu.navisdk.adapter.NaviModuleImpl;
import com.baidu.vi.VDeviceAPI;
import com.longshine.electriccars.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 导航界面
 * @author jack
 */
public class BNGuideActivity extends Activity {

	private final String TAG = BNGuideActivity.class.getSimpleName();
	private BNRoutePlanNode mBNRoutePlanNode = null;
	private BaiduNaviCommonModule mBaiduNaviCommonModule = null;

	/*
     * 对于导航模块有两种方式来实现发起导航。 1：使用通用接口来实现 2：使用传统接口来实现
     *
     */
	// 是否使用通用接口
	private boolean useCommonInterface = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		BaiduNavigation.activityList.add(this);
		createHandler();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		}
		View view = null;
		if (useCommonInterface) {
			//使用通用接口
			mBaiduNaviCommonModule = NaviModuleFactory.getNaviModuleManager().getNaviCommonModule(
					NaviModuleImpl.BNaviCommonModuleConstants.ROUTE_GUIDE_MODULE, this,
					BNaviBaseCallbackModel.BNaviBaseCallbackConstants.CALLBACK_ROUTEGUIDE_TYPE, mOnNavigationListener);
			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onCreate();
				view = mBaiduNaviCommonModule.getView();
			}

		} else {
			//使用传统接口
			view = BNRouteGuideManager.getInstance().onCreate(this,mOnNavigationListener);
		}


		if (view != null) {
			setContentView(view);
		}

		Intent intent = getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				mBNRoutePlanNode = (BNRoutePlanNode) bundle.getSerializable(BaiduNavigation.ROUTE_PLAN_NODE);
			}
		}
		//显示自定义图标
		if (hd != null) {
			hd.sendEmptyMessage(MSG_SHOW);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(useCommonInterface) {
			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onResume();
			}
		} else {
			BNRouteGuideManager.getInstance().onResume();
		}

	}

	protected void onPause() {
		try {
			super.onPause();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(useCommonInterface) {
			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onPause();
			}
		} else {
			BNRouteGuideManager.getInstance().onPause();
		}

	}

	@Override
	protected void onDestroy() {
		try {
			super.onDestroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(useCommonInterface) {
			if(mBaiduNaviCommonModule != null) {
				VDeviceAPI.unsetNetworkChangedCallback();
				hd.removeCallbacksAndMessages(null);
				mBaiduNaviCommonModule.onDestroy();
				mBaiduNaviCommonModule=null;
			}
		} else {
			BNRouteGuideManager.getInstance().onDestroy();
		}
		hd=null;
		BaiduNavigation.activityList.remove(this);

	}


	@Override
	protected void onStop() {
		try {
			super.onStop();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(useCommonInterface) {
			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onStop();
			}
		} else {
			BNRouteGuideManager.getInstance().onStop();
		}

	}

	@Override
	public void onBackPressed() {
		if(useCommonInterface) {
			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onBackPressed(false);
			}
		} else {
			BNRouteGuideManager.getInstance().onBackPressed(false);
		}
	}

	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(useCommonInterface) {
			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onConfigurationChanged(newConfig);
			}
		} else {
			BNRouteGuideManager.getInstance().onConfigurationChanged(newConfig);
		}

	};


	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

		if(useCommonInterface) {
			if(mBaiduNaviCommonModule != null) {
				Bundle mBundle = new Bundle();
				mBundle.putInt(RouteGuideModuleConstants.KEY_TYPE_KEYCODE, keyCode);
				mBundle.putParcelable(RouteGuideModuleConstants.KEY_TYPE_EVENT, event);
				mBaiduNaviCommonModule.setModuleParams(RouteGuideModuleConstants.METHOD_TYPE_ON_KEY_DOWN, mBundle);
				try {
					Boolean ret = (Boolean)mBundle.get(RET_COMMON_MODULE);
					if(ret) {
						return true;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	@Override
	protected void onStart() {
		super.onStart();
		// TODO Auto-generated method stub
		if(useCommonInterface) {
			if(mBaiduNaviCommonModule != null) {
				mBaiduNaviCommonModule.onStart();
			}
		} else {
			BNRouteGuideManager.getInstance().onStart();
		}
	}
	private void addCustomizedLayerItems() {
		List<CustomizedLayerItem> items = new ArrayList<CustomizedLayerItem>();
		CustomizedLayerItem item1 = null;
		if (mBNRoutePlanNode != null) {
			item1 = new CustomizedLayerItem(mBNRoutePlanNode.getLongitude(), mBNRoutePlanNode.getLatitude(),
					mBNRoutePlanNode.getCoordinateType(), getResources().getDrawable(R.mipmap.ic_map_location),
					CustomizedLayerItem.ALIGN_CENTER);
			items.add(item1);

			BNRouteGuideManager.getInstance().setCustomizedLayerItems(items);
		}
		BNRouteGuideManager.getInstance().showCustomizedLayer(true);
	}

	private static final int MSG_SHOW = 1;
	private static final int MSG_HIDE = 2;
	private static final int MSG_RESET_NODE = 3;
	private Handler hd = null;

	private void createHandler() {
		if (hd == null) {
			hd = new Handler(getMainLooper()) {
				public void handleMessage(android.os.Message msg) {
					if (msg.what == MSG_SHOW) {
						addCustomizedLayerItems();
					} else if (msg.what == MSG_HIDE) {
						BNRouteGuideManager.getInstance().showCustomizedLayer(false);
					} else if (msg.what == MSG_RESET_NODE) {
//						BNRouteGuideManager.getInstance().resetEndNodeInNavi(
//								new BNRoutePlanNode(114.3793395781, 22.7236035873, "沃特玛", null, CoordinateType.BD09LL));
					}
				};
			};
		}
	}

	private OnNavigationListener mOnNavigationListener = new OnNavigationListener() {

		@Override
		public void onNaviGuideEnd() {
			//退出导航
			overridePendingTransition(R.anim.next_enter_anim,R.anim.next_exit_anim);
			finish();
			onDestroy();
			Toast.makeText(BNGuideActivity.this,"退出导航",Toast.LENGTH_SHORT).show();
		}

		@Override
		public void notifyOtherAction(int actionType, int arg1, int arg2, Object obj) {

			if (actionType == 0) {
				//导航到达目的地 自动退出
				Log.i(TAG, "notifyOtherAction actionType = " + actionType + ",导航到达目的地！");
				Toast.makeText(BNGuideActivity.this,"到达目的地!",Toast.LENGTH_SHORT).show();
			}

			Log.i(TAG, "actionType:" + actionType + "arg1:" + arg1 + "arg2:" + arg2 + "obj:" + obj.toString());
		}

	};

	private final static String RET_COMMON_MODULE = "module.ret";

	private interface RouteGuideModuleConstants {
		final static int METHOD_TYPE_ON_KEY_DOWN = 0x01;
		final static String KEY_TYPE_KEYCODE = "keyCode";
		final static String KEY_TYPE_EVENT = "event";
	}
}
