package edu.buffalo.cse.cse486586.simpledht;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;


public class SHA1 {

	public static String genHash(String input){
		MessageDigest sha1;
		try {
			sha1 = MessageDigest.getInstance("SHA-1");
			byte[] sha1Hash = sha1.digest(input.getBytes());
			Formatter formatter = new Formatter();
			for (byte b : sha1Hash) {
				formatter.format("%02x", b);
			}
				return formatter.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
}
