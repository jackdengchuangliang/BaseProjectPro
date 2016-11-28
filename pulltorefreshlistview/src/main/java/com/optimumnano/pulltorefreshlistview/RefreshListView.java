package com.optimumnano.pulltorefreshlistview;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;



import java.util.Date;

/**
 * 带有下拉刷新的ListView
 * 
 * @author 刘广茂
 * 
 */
public class RefreshListView extends ListView implements OnScrollListener {

	/**
	 * 控件状态-下拉过界
	 */
	private static final int RELEASE_TO_REFRESH = 0;
	/**
	 * 控件状态-下拉中
	 */
	private static final int PULL_TO_REFRESH = 1;
	/**
	 * 控件状态-正在加载
	 */
	private static final int REFRESHING = 2;

	/**
	 * 控件状态-加载完成||无需加载状态
	 */
	private static final int DONE = 3;

	/**
	 * 控件状态-上拉刷新
	 */
	private static final int RAISE_TO_REFRESH = 5;

	/**
	 * 控件状态-上拉时松手刷新
	 */
	private static final int RELEASE_RAISE_TO_REFRESH = 6;

	/**
	 * 滑动距离/提示信息高度 的比值
	 */
	private static final int RATIO = 3;

	/**
	 * 上下文
	 */
	private Context mContext;

	/**
	 * 下拉提示控件
	 */
	private View headerView;

	/**
	 * 提示信息所在的文本控件
	 */
	private TextView headerMsgView;

	/**
	 * 提示控件的时间显示文本控件
	 */
	private TextView headerTimeView;

	/**
	 * 进度条
	 */
	private ProgressBar progressBar;

	/**
	 * 下拉箭头控件
	 */
	private ImageView arrowImageView;

	/**
	 * 底部提示框
	 */
	private View footerView;

	/**
	 * 底部提示信息
	 */
	private TextView footerMsgView;

	/**
	 * 底部进度条
	 */
	private View footerProgressView;

	/**
	 * 当前状态
	 */
	private int mPullRefreshState;

	/**
	 * 下拉提示控件高度
	 */
	private int mHeaderHeight;

	/**
	 * 上拉提示控件高度
	 */
	private int mFooterHeight;

	/**
	 * 箭头翻转动画
	 */
	private RotateAnimation animation;

	/**
	 * 下拉箭头翻转动画
	 */
	private RotateAnimation reverseAnimation;

	/**
	 * 下拉越界后是否有拖回界限内
	 */
	private boolean isBack;

	/**
	 * 第一个可见条目的序号
	 */
	private int firstItemIndex;

	/**
	 * 总条目数
	 */
	private int totalSize;

	/**
	 * 最后一个可见条目的序号
	 */
	private boolean isFootBarWork = true;

	/**
	 * 是否已开始记录事件位置
	 */
	private boolean isRecored;

	/**
	 * 触摸事件起始位置
	 */
	private int startY;

	/**
	 * 界限倍数
	 */
	private int raiseBoundary = 3;

	/**
	 * 刷新时的回调监听
	 */
	private OnRefreshListener mOnRefreshListener;

	/**
	 * 是否初始化头部信息栏
	 */
	private boolean isInitHeader;

	/**
	 * 是否初始化底部信息栏
	 */
	private boolean isInitFooter;

	/**
	 * 简单构造方法
	 * 
	 * @param context
	 *            上下文
	 */
	public RefreshListView(Context context) {
		super(context);
		this.mContext = context;
		if (isInEditMode()) {
			return;
		}
		initHeader();
		initFooter();
		isInitHeader = true;
		isInitFooter = true;
		setOnScrollListener(this);
	}

	/**
	 * 系统调用的构造方法 当在xml中配置本控件时调用
	 * 
	 * @param context
	 *            上下文
	 * @param attrs
	 *            xml中配置的参数
	 */
	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		TypedArray array = context.obtainStyledAttributes(attrs,
				R.styleable.RefreshListView);
		isInitHeader = array.getBoolean(
				R.styleable.RefreshListView_show_header, true);
		isInitFooter = array.getBoolean(
				R.styleable.RefreshListView_show_footer, true);
		if (isInEditMode()) {
			return;
		}
		if (isInitHeader) {
			initHeader();
		}

		if (isInitFooter) {
			initFooter();
		}
		setOnScrollListener(this);
		array.recycle();
	}

	/**
	 * 设置刷新监听
	 * 
	 * @param listener
	 *            刷新监听器
	 */
	public void setOnRefreshListener(OnRefreshListener listener) {
		this.mOnRefreshListener = listener;
	}

	/**
	 * 设置上拉时触动加载更多的边界 默认为2倍底部信息栏高度
	 * 
	 * @param boundary
	 *            界限值与底部信息栏的比值
	 */
	public void setRaiseBoundary(int boundary) {
		if (boundary <= 1) {
			throw new IllegalArgumentException("该比值不可小于1");
		}
		this.raiseBoundary = boundary;
	}

	/**
	 * 设置顶部提示信息颜色
	 * 
	 * @param color
	 *            色值
	 */
	public void setHeadMsgTextColor(int color) {
		headerMsgView.setTextColor(color);
	}

	/**
	 * 设置顶部提示信息文字大小
	 * 
	 * @param size
	 *            文字大小
	 */
	public void setHeadMsgTextSize(int size) {
		headerMsgView.setTextSize(size);
	}

	/**
	 * 设置底部提示信息颜色
	 * 
	 * @param color
	 *            色值
	 */
	public void setFootMsgTextColor(int color) {
		footerMsgView.setTextColor(color);
	}

	/**
	 * 设置底部提示信息文字大小
	 * 
	 * @param size
	 *            文字大小
	 */
	public void setFootMsgTextSize(int size) {
		footerMsgView.setTextSize(size);
	}

	/**
	 * 设置底部提示信息文字
	 * 
	 * @param str
	 *            文字
	 */
	public void setFootMsgText(String str) {
		footerMsgView.setText(str);
	}

	/**
	 * 设置顶部信息栏不可见 若在配置文件中配置为可见，需在设置适配器之后调用
	 * 
	 */
	public void setHeaderInvisiable() {
		if (isInitHeader) {
			if (headerView != null) {
				this.removeHeaderView(headerView);
			}
		}
		isInitHeader = false;
	}

	/**
	 * 设置底部信息栏不可见 若在配置文件中配置为可见，需在设置适配器之后调用
	 */
	public void setFooterInvisiable() {
		if (isInitFooter) {
			if (footerView != null) {
				this.removeFooterView(footerView);
			}
			isInitFooter = false;
		}
	}

	@Override
	public void setAdapter(ListAdapter adapter) {

		if (adapter != null) {
			adapter.registerDataSetObserver(new DataSetObserver() {
				@Override
				public void onChanged() {
					// 延时调用加载完毕后的处理方法，保证数据已经展示咱界面
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							pullRefreshComplete();
							raiseRefreshComplete();
						}
					}, 10);
					super.onChanged();
				}
			});
		}
		super.setAdapter(adapter);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		ListAdapter adapter = getAdapter();
		if (adapter != null) {
			if (adapter.getCount() - 1 == getLastVisiblePosition()) {
				isFootBarWork = true;
			} else {
				isFootBarWork = false;
			}
		}

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
	                     int visibleItemCount, int totalItemCount) {
		/* 获取当前所能看到的项目序号 以及总条数 */
		firstItemIndex = firstVisibleItem;
		totalSize = totalItemCount;
		if (getLastVisiblePosition() == totalItemCount - 1) {
			isFootBarWork = true;
		} else {
			isFootBarWork = false;
		}
	}


	/**
	 * 重写onTouchEvent 处理ListView的上拉和下拉事件
	 * 
	 * @param ev
	 *            触摸事件
	 * @return 触摸事件是否被消耗掉
	 */

	long startTime;

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 手势落在屏幕上，记录起始位置
			if (!isRecored) {
				isRecored = true;
				startY = (int) ev.getY();
			}
			startTime = System.currentTimeMillis();
			break;
		case MotionEvent.ACTION_MOVE:
			// 手势移动，根据手势判断是下拉还是上拉
			int tempY = (int) ev.getY();
			if (!isRecored && (firstItemIndex == 0 || isFootBarWork)) {
				isRecored = true;
				startY = tempY;
			}
			if (tempY - startY > 0 && firstItemIndex == 0 && isInitHeader) {
				// 下拉动作
				pullToRefresh(tempY);
			} else if (tempY - startY < 0 && isFootBarWork && isInitFooter) {
				// 上拉动作
				raiseToRefresh(tempY);
			}
			if (tempY - startY == 0) {
				// 交替动作界限 将标志位重置
				mPullRefreshState = DONE;
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			int upY = (int) ev.getY();
			if (isInitHeader) {
				// 手势抬起，执行对应的操作
				if (mPullRefreshState != REFRESHING) {
					// 未处于正在加载状态中
					if (mPullRefreshState == DONE) {

					}
					if (mPullRefreshState == PULL_TO_REFRESH) {
						// 下拉刷新界限内
						mPullRefreshState = DONE;
						changeHeaderViewByState();
					}
					if (mPullRefreshState == RELEASE_TO_REFRESH) {
						// 触发刷新
						mPullRefreshState = REFRESHING;
						changeHeaderViewByState();
						onRefreshHeader();
					}
				}
			}
			if (isInitFooter && isFootBarWork) {
				if (mPullRefreshState != RELEASE_RAISE_TO_REFRESH
						|| mPullRefreshState != RELEASE_TO_REFRESH) {
					// 上拉刷新
					if (-1 * (upY - startY) < mFooterHeight * raiseBoundary) {
						// 界限内
						mPullRefreshState = DONE;
					} else {
						// 触发上拉刷新
						mPullRefreshState = REFRESHING;
						onRefreshFooter();
					}
					changeFooterViewByState();
				}
				if (mPullRefreshState == RAISE_TO_REFRESH) {
					// 上拉刷新界限内
					mPullRefreshState = DONE;
					changeFooterViewByState();
				}
			}
			isFootBarWork = false;
			isRecored = false;
			isBack = false;
			break;

		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 初始化下拉提示控件
	 */
	private void initHeader() {
		/* 获取子控件 */
		headerView = View.inflate(mContext,
				R.layout.refresh_listview_header_view, null);
		headerMsgView = (TextView) headerView
				.findViewById(R.id.head_tipsTextView);
		headerTimeView = (TextView) headerView
				.findViewById(R.id.head_lastUpdatedTextView);
		arrowImageView = (ImageView) headerView
				.findViewById(R.id.head_arrowImageView);
		progressBar = (ProgressBar) headerView
				.findViewById(R.id.head_progressBar);
		measureView(headerView);
		headerTimeView.setVisibility(GONE);
		/* 获取提示控件高度 */
		mHeaderHeight = headerView.getMeasuredHeight();
		headerView.setPadding(0, -1 * mHeaderHeight, 0, 0);
		headerView.invalidate();
		addHeaderView(headerView, null, false);
		setOnScrollListener(this);

		/* 下拉箭头的旋转动画 */
		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);
		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);
		mPullRefreshState = DONE;
	}

	/**
	 * 初始化底部信息
	 */
	private void initFooter() {
		/* 获取底部“加载更多”控件 */
		isInitFooter = true;
		footerView = View.inflate(mContext,
				R.layout.refresh_listview_footer_view, null);
		footerMsgView = (TextView) footerView.findViewById(R.id.footerMsg);
		footerProgressView = footerView.findViewById(R.id.footerProgress);
		measureView(footerView);
		mFooterHeight = footerView.getMeasuredHeight();
		footerView.invalidate();
		addFooterView(footerView);
		footerView.setOnClickListener(footLoadMoreListner);
	}

	/**
	 * 测量view的的子控件 计算view及其子控件的显示尺寸
	 * 
	 * @param view
	 *            需要计算的view控件
	 * 
	 */
	@SuppressWarnings("deprecation")
	private void measureView(View view) {
		ViewGroup.LayoutParams p = view.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		view.measure(childWidthSpec, childHeightSpec);
	}

	/**
	 * 上拉刷新
	 * 
	 * @param tempY
	 *            触摸时的Y坐标
	 */
	private void raiseToRefresh(int tempY) {
		if (mPullRefreshState != REFRESHING && isRecored) {
			if (mPullRefreshState == DONE) {
				// 开始拖动
				mPullRefreshState = RAISE_TO_REFRESH;
				changeFooterViewByState();
			}
			if (mPullRefreshState == RAISE_TO_REFRESH
					|| mPullRefreshState == RAISE_TO_REFRESH) {
				// 拖动过程中
				setSelection(totalSize - 1);
				if (-1 * ((tempY - startY) / RATIO) >= mFooterHeight
						* raiseBoundary) {
					mPullRefreshState = RELEASE_RAISE_TO_REFRESH;
				} else {
					mPullRefreshState = RAISE_TO_REFRESH;
					changeFooterViewByState();
				}
			}
			/* 设置Padding值来展示上拉动作 */
			if (firstItemIndex == 0) {
				this.setPadding(0, (tempY - startY) / RATIO, 0, -1
						* (tempY - startY) / RATIO);
			}
			footerView.setPadding(0, 0, 0, -1 * (tempY - startY) / RATIO);
		}
	}

	/**
	 * 根据状态更改低端的提示信息
	 */
	private void changeFooterViewByState() {
		switch (mPullRefreshState) {
		case RAISE_TO_REFRESH:
			// 未达临界
			footerMsgView.setVisibility(VISIBLE);
			footerProgressView.setVisibility(GONE);
			break;
		case DONE:
			// 正常状态
			this.setPadding(0, 0, 0, 0);
			footerView.setPadding(0, 0, 0, 0);
			footerMsgView.setVisibility(VISIBLE);
			footerProgressView.setVisibility(GONE);
			break;
		case RELEASE_RAISE_TO_REFRESH:
			// 超过临界值
			footerMsgView.setVisibility(VISIBLE);
			footerProgressView.setVisibility(GONE);
			break;
		case REFRESHING:
			// 正在刷新中
			this.setPadding(0, 0, 0, 0);
			footerView.setPadding(0, 0, 0, 0);
			footerMsgView.setVisibility(GONE);
			footerProgressView.setVisibility(VISIBLE);
			break;
		default:
			break;
		}
	}

	/**
	 * 下拉刷新
	 * 
	 * @param tempY
	 *            触摸时的Y坐标
	 */
	private void pullToRefresh(int tempY) {
		// 控件无刷新，后台无更新数据，并且触发了开始记录触摸事件位置
		if (mPullRefreshState != REFRESHING && isRecored) {
			if (mPullRefreshState == RELEASE_TO_REFRESH) {
				// 处于下拉越界状态
				setSelection(0);
				if (((tempY - startY) / RATIO < mHeaderHeight)
						&& (tempY - startY) > 0) {
					mPullRefreshState = PULL_TO_REFRESH;
					changeHeaderViewByState();
				} else if (tempY - startY <= 0) {
					mPullRefreshState = DONE;
					changeHeaderViewByState();
				}
			}

			if (mPullRefreshState == PULL_TO_REFRESH) {
				// 正常下拉状态
				setSelection(0);
				if ((tempY - startY) / RATIO >= mHeaderHeight) {
					mPullRefreshState = RELEASE_TO_REFRESH;
					isBack = true;
					changeHeaderViewByState();
				} else if (tempY - startY <= 0) {
					mPullRefreshState = DONE;
					changeHeaderViewByState();
				}
			}
			// 正常状态
			if (mPullRefreshState == DONE) {
				if (tempY - startY > 0) {
					mPullRefreshState = PULL_TO_REFRESH;
					changeHeaderViewByState();
				}
			}

			/* 设置Padding值来展示下拉动作 */
			if (mPullRefreshState == PULL_TO_REFRESH) {
				headerView.setPadding(0, -1 * mHeaderHeight + (tempY - startY)
						/ RATIO, 0, 0);
			}
			if (mPullRefreshState == RELEASE_TO_REFRESH) {
				headerView.setPadding(0, (tempY - startY) / RATIO
						- mHeaderHeight, 0, 0);
			}

		}
	}

	/**
	 * 根据状态改变顶端的提示信息
	 */
	private void changeHeaderViewByState() {
		switch (mPullRefreshState) {
		// 下拉越界
		case RELEASE_TO_REFRESH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			headerMsgView.setVisibility(View.VISIBLE);
			headerTimeView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);
			headerMsgView.setText("松手可刷新");

			break;
		// 下拉但未越界
		case PULL_TO_REFRESH:
			progressBar.setVisibility(View.GONE);
			headerMsgView.setVisibility(View.VISIBLE);
			headerTimeView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);
			}
			headerMsgView.setText("下拉可刷新");
			break;

		// 到达可刷新临界并松手
		case REFRESHING:
			headerView.setPadding(0, 0, 0, 0);
			progressBar.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			headerMsgView.setText("正在加载，请稍候 ...");
			headerTimeView.setVisibility(View.VISIBLE);
			break;

		// 加载完成或无动作
		case DONE:
			progressBar.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.setImageResource(R.mipmap.arrow);
			headerMsgView.setText("加载完成");
			headerTimeView.setVisibility(View.VISIBLE);
			headerView.setPadding(0, -1 * mHeaderHeight, 0, 0);
			break;
		default:
			break;
		}
	}

	/**
	 * 下拉刷新 加载完毕
	 */
	public void pullRefreshComplete() {
		if (isInitHeader) {
			mPullRefreshState = DONE;
			headerTimeView.setText("上次更新:" + new Date().toLocaleString());
			changeHeaderViewByState();
		}
	}

	/**
	 * 上拉刷新完毕
	 */
	public void raiseRefreshComplete() {
		if (isInitFooter) {
			footerView.setClickable(true);
			mPullRefreshState = DONE;
			changeFooterViewByState();
		}
	}

	/**
	 * 下拉刷新加载中
	 */
	private void onRefreshHeader() {
		if (mOnRefreshListener != null) {
			mOnRefreshListener.onPullDownRefresh(this);
		}
	}

	/**
	 * 上拉刷新
	 */
	private void onRefreshFooter() {
		footerView.setClickable(false);
		if (mOnRefreshListener != null) {
			mOnRefreshListener.onRaiseRefresh(this);
		}
	}

	/**
	 * 刷新时的回调接口
	 * 
	 * @author 刘广茂
	 * 
	 */
	public interface OnRefreshListener {
		/**
		 * 下拉刷新
		 */
		void onPullDownRefresh(ListView v);

		/**
		 * 上拉刷新
		 */
		void onRaiseRefresh(ListView v);
	}

	/**
	 * 底部提示栏监听事件
	 */
	private OnClickListener footLoadMoreListner = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 触发上拉刷新
			mPullRefreshState = REFRESHING;
			changeFooterViewByState();
			onRefreshFooter();
		}
	};
}
