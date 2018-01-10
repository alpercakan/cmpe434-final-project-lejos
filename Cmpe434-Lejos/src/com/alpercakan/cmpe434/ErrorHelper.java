package com.alpercakan.cmpe434;

import java.util.ArrayList;

public class ErrorHelper {
	public static ArrayList errorList = new ArrayList();
	
	public static void pushNewError(String s) {
		errorList.add(s);
	}
	
	public static void pushNewError(Exception e) {
		errorList.add(e.getStackTrace().toString());
	}
	
	public static String getLastError() {
		if (errorList.isEmpty())
			return "";
		
		return (String) errorList.get(errorList.size() - 1);
	}
}
