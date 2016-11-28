package com.lgm.baseframe.common.http;

/**
 * Created by Administrator on 2016/1/14.
 */
public abstract class HttpCallbackListener implements HttpUtil.HTTPLiStener {
	/**
	 * 请求成功
	 *
	 * @param result      请求结果
	 * @param requestData 请求数据
	 */
	public abstract void onRequestSuccess(String result, Object requestData);

	/**
	 *服务器判定请求错误
	 *
     * @param resultCode 错误码
     * @param resultMsg 错误消息
     */
	public boolean onRequestFailed(int resultCode, String resultMsg) {
		return false;
	}

	/**
	 * 连接服务器失败，包括网络超时等
	 */
	public void onConnectionFailed(Exception ex) {
	}

	/**
	 * Http请求状态错误 （非200）
	 *
	 * @param statusCode 请求状态码
	 */
	public void onHttpStateError(String result, int statusCode){};
}
