package com.konka.dhtsearch.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.konka.dhtsearch.Node;

public class Util {
	// md5加密
	public static String md5(String inputText) {
		return encrypt(inputText, "md5");
	}

	// sha加密
	public static String sha(String inputText) {
		return encrypt(inputText, "sha-1");
	}

	public static String rundom_id(int length) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			char c = (char) (Math.random() * 255);
			builder.append(c);
		}
		return builder.toString();
	}

	public static String random_tranctionId() {
		return sha(rundom_id(20));
	}

	public static byte[] nodesToBytes(List<Node> nodes) {
		int size = nodes.size();
		byte[] nodesbyte = new byte[size * 26];
		for (int i = 0; i < size; i++) {
			Node node = nodes.get(i);
			byte[] id = node.getKey().getBytes();
			byte[] address = node.getInetAddress().getAddress();
			byte[] port = Util.intTobytes(node.getPort());
			// System.arraycopy(s,0,a,0,s.length);
			System.arraycopy(id, 0, nodesbyte, 0 + i * 26, id.length);
			System.arraycopy(address, 0, nodesbyte, 0 + i * 26 + id.length, address.length);
			System.arraycopy(port, 0, nodesbyte, 0 + i * 26 + id.length + address.length, port.length);
		}
		return nodesbyte;
	}

	/**
	 * md5或者sha-1加密
	 * 
	 * @param inputText
	 *            要加密的内容
	 * @param algorithmName
	 *            加密算法名称：md5或者sha-1，不区分大小写
	 * @return
	 */
	private static String encrypt(String inputText, String algorithmName) {
		if (inputText == null || "".equals(inputText.trim())) {
			throw new IllegalArgumentException("请输入要加密的内容");
		}
		if (algorithmName == null || "".equals(algorithmName.trim())) {
			algorithmName = "md5";
		}
		String encryptText = null;
		try {
			MessageDigest m = MessageDigest.getInstance(algorithmName);
			m.update(inputText.getBytes("UTF8"));
			byte s[] = m.digest();
			// m.digest(inputText.getBytes("UTF8"));
			return hex(s);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return encryptText;
	}

	// 返回十六进制字符串
	public static String hex(byte[] arr) {
		// System.out.println("t的长度=" + arr.length);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < arr.length; ++i) {
			sb.append(Integer.toHexString((arr[i] & 0xFF) | 0x100).substring(1, 3));
		}
		return sb.toString();
	}

	/**
	 * 将2byte 的端口转为int
	 */
	public static int bytesToInt(byte[] src) {
		int value = 0;
		value |= (src[0] & 0xFF) << 8;
		value |= (src[1] & 0xFF) << 0;
		return value;
	}

	private static int parse(char c) {
		if (c >= 'a')
			return (c - 'a' + 10) & 0x0f;
		if (c >= 'A')
			return (c - 'A' + 10) & 0x0f;
		return (c - '0') & 0x0f;
	}

	public static byte[] HexString2Bytes(String hexstr) {
		byte[] b = new byte[hexstr.length() / 2];
		int j = 0;
		for (int i = 0; i < b.length; i++) {
			char c0 = hexstr.charAt(j++);
			char c1 = hexstr.charAt(j++);
			b[i] = (byte) ((parse(c0) << 4) | parse(c1));
		}
		return b;
	}

	// public static byte[] stringTobytes(String value) {
	// byte[] src = new byte[value.length() / 2];
	// int int16 = Integer.parseInt(value, 16);
	// for (int i = 0; i < src.length; i++) {
	// src[i] = (byte) ((int16 >> 0 + i * 8) & 0xFF);
	// }
	// return src;
	// }

	/**
	 * 将int端口转为2 byte
	 */
	public static byte[] intTobytes(int value) {
		byte[] src = new byte[2];
		src[1] = (byte) ((value >> 8) & 0xFF);
		src[0] = (byte) ((value >> 0) & 0xFF);
		return src;
	}

	/**
	 * byte数组中取int数值，本方法适用于(低位在后，高位在前)的顺序。和intToBytes2（）配套使用
	 */
	public static int bytesToInt2(byte[] src, int offset) {
		int value;
		value = (int) (((src[offset] & 0xFF) << 24) //
				| ((src[offset + 1] & 0xFF) << 16) //
				| ((src[offset + 2] & 0xFF) << 8) //
		| (src[offset + 3] & 0xFF));
		return value;
	}

	public static void main(String[] args) {
		// byte[] bb = { 10, 10, 10, 10 };
		// int dd = bytesToInt2(bb, 0);
		// System.out.println(dd);
		System.out.println(hex(HexString2Bytes("aaaa3222223d22")));
	}
}
