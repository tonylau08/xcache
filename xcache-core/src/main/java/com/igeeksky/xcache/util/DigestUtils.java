package com.igeeksky.xcache.util;

import java.security.MessageDigest;

/**
 * 摘要算法工具类
 * @author Tony.Lau
 * @create 2017-01-12 00:01:13
 */
public class DigestUtils {
	
	/**
	 * byte[]根据MD5摘要算法转HexString(默认大写)
	 * @param plainText 明文字符串
	 * @return 16进制字符串
	 */
	public static String MD5(byte[] data){
		try {
			return encrypt(Algorithm.MD5, false, data);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * 字符串根据MD5摘要算法转HexString(默认大写)
	 * @param plainText 明文字符串
	 * @return 16进制字符串
	 */
	public static String MD5(String plainText){
		try {
			return encrypt(Algorithm.MD5, false, plainText.getBytes());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * 字符串根据SHA1摘要算法转HexString(默认小写)
	 * @param plainText 明文字符串
	 * @return 16进制字符串
	 */
	public static String SHA1(String plainText){
		try {
			return encrypt(Algorithm.SHA_1, true, plainText.getBytes());
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	/**
	 * byte[]根据SHA512摘要算法转HexString(默认大写)
	 * @param plainText 明文字符串
	 * @return 16进制字符串
	 */
	public static String SHA512(byte[] data){
		try {
			return encrypt(Algorithm.SHA_512, false, data);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * 字符串根据SHA512摘要算法转HexString(默认大写)
	 * @param plainText 明文字符串
	 * @return 16进制字符串
	 */
	public static String SHA512(String plainText){
		try {
			return encrypt(Algorithm.SHA_512, false, plainText.getBytes());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * 字符串根据摘要算法转HexString(默认大写)
	 * @param algorithm 选择信息摘要算法
	 * @param plainText 明文字符串
	 * @return 16进制字符串
	 */
	public static String encrypt(Algorithm algorithm, String plainText){
		try {
			return encrypt(algorithm, false, plainText.getBytes());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * 字符串根据摘要算法转HexString
	 * @param algorithm 选择信息摘要算法
	 * @param toLowerCase 16进制的字符串是否转换成小写
	 * @param plainText 明文字符串
	 * @return 16进制字符串
	 */
	public static String encrypt(Algorithm algorithm, boolean toLowerCase, String plainText){
		try {
			return encrypt(algorithm, toLowerCase, plainText.getBytes());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * byte[]根据摘要算法转HexString
	 * @param algorithm 选择信息摘要算法
	 * @param toLowerCase 16进制的字符串是否转换成小写
	 * @param data 待处理byte数组
	 * @return 16进制字符串
	 */
	public static String encrypt(Algorithm algorithm, boolean toLowerCase, byte[] data){
		try {
			//获取算法实例
			MessageDigest instance = MessageDigest.getInstance(algorithm.getName());
			instance.update(data);
			
			//生成信息摘要
			byte[] digest = instance.digest();
			
			//转换成16进制字符
			return HexUtil.encodeHexString(toLowerCase, digest);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * 摘要算法类型
	 * @author Tony.Lau
	 * @create 2017-01-12 00:00:22
	 */
	public static enum Algorithm{
		
		MD5(128, "MD5"),
		SHA_1(160, "SHA-1"),
		SHA_224(224, "SHA-224"),
		SHA_256(256, "SHA-256"),
		SHA_384(384, "SHA-384"),
		SHA_512(512, "SHA-512");
		
		private int bit;
		private String name;
		
		private Algorithm(int bit, String name){
			this.bit = bit;
			this.name = name;
		}
		
		public String getName() {
			return name;
		}

		public int getBit() {
			return bit;
		}
	}
}
