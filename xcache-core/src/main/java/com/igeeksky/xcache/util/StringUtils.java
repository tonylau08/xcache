package com.igeeksky.xcache.util;

public class StringUtils {
	
	private StringUtils(){}
	
	/**
	 * 如果字符串为空或者空白，返回true；否则返回false。
	 * @param msg
	 * @return
	 */
	public static boolean isEmpty(String str){
		return (null == str || str.trim().length() == 0);
	}
	
	/**
	 * 如果字符串不为空且非空白，返回true；否则返回false。
	 * @param msg
	 * @return
	 */
	public static boolean isNotEmpty(String str){
		return (null != str && !(str.trim().length() == 0));
	}

}
