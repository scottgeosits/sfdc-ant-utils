package com.antutilities.salesforce.utils;

public class SalesforceStringUtils {

	public static String LINE_SEP = System.getProperty("line.separator");

	public static String arrayToString(String[] array) {
    	
    	StringBuffer str = new StringBuffer();
    	for (int idx = 0; idx < array.length; idx++) {
    		str.append(array[idx]).append(LINE_SEP);
    	}
    	return str.toString();
    }

}
