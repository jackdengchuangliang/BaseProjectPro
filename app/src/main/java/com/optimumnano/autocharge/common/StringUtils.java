package com.optimumnano.autocharge.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	protected static final String TAG = StringUtils.class.getSimpleName();

	/**
	 * 判断字符串是否为null或者""
	 */
	public static boolean isEmptyOrNull(String content) {
		if (content == null || content.equals("")) {
			return true;
		}
		return false;
	}


	/**
	 * 是否为数字
	 */
	public static boolean isDigit(String digitString) {
		if (!isEmptyOrNull(digitString)) {
			String regex = "[0-9]*";
			return isMatch(regex, digitString);
		}
		return false;
	}


	/**
	 *  通过正则表达式判断是否为手机号
	 * @param phoneString
	 * @return
	 */
	public static boolean isPhoneNumber(String phoneString) {
		//^1[34578]\\d{9}$
		String format = "^1[34578]\\d{9}$";
		return isMatch(format, phoneString);
	}
	/**
	 *  字符串正则校验
	 * @param regex
	 *            正则表达式
	 * @param string
	 *            需要检验的字符串
	 * @return
	 */
	public static boolean isMatch(String regex, String string) {

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(string);
		return matcher.matches();
	}



}
