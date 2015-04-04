package com.antutilities.salesforce.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A generic class to load files as strings.
 *
 * @author Scott GEOSITS
 * @created Oct 3, 2005 3:31:31 PM
 */
public class FileLoader {

    /**
     * Constant to hold buffer size for reading files.
     */
    private static final int BUFFER_SIZE = 1024;

    /**
     * End-Of-File flag.
     */
    private static final int EOF = -1;

    /**
     * Line separator delimiter - loaded from system properties.
     */
    private static final String NEWLINE = System.getProperty("line.separator");

    /**
     * Comment delimiter.
     */
    private String _comment = "#";

    /**
     * Ignore whitespace-only lines?
     */
    private boolean _ignoreWhitespaceLines = true;

    /**
     * List containing all of the lines of the file.
     */
    private List _entries = null;

    /**
     * Method to load data from File.
     *
     * @param fileName
     *            the name of the file to load.
     *
     * @return data from file as a String
     *
     * @throws IOException
     *             thrown if there is an issue loading the file.
     */
    public String loadData(String fileName) throws IOException {

        File fileObj = new File(fileName);
        StringBuffer output = new StringBuffer();
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        char[] buffer = new char[BUFFER_SIZE];

        int len = 0;

        while ((len = br.read(buffer, 0, BUFFER_SIZE)) != EOF) {
            output.append(buffer, 0, len);
        }

        return output.toString();
    }

    /**
     * Load the file and return a List of the lines.
     *
     * @param fileName
     *            the name of the file to load.
     * @return the list of the file lines.
     * @throws IOException
     *             if there is an issue reading the properties file
     */
    public List loadDataAsList(String fileName) throws IOException {

        this._entries = new ArrayList();

        // Open a buffered input stream from the file.
        BufferedReader in = new BufferedReader(new FileReader(fileName));
        String str;

        // Read in the file line by line.
        while (null != (str = in.readLine())) {
            if (str.indexOf(_comment) < 0) {
                if ((str.trim().length() > 0) || (!_ignoreWhitespaceLines)) {
                    _entries.add(str);
                }
            }
        }

        // Close the input stream and return the collection of lines.
        in.close();

        return this._entries;
    }

    /**
     * Load the file and return a String array of the lines.
     *
     * @param fileName
     *            the name of the file to load.
     * @return the array of the file lines.
     * @throws IOException
     *             if there is an issue reading the properties file
     */
    public String[] loadDataAsArray(String fileName) throws IOException {

    	List<String> strList = loadDataAsList(fileName);
    	String[] strArray = new String[strList.size()];
    	int counter = 0;
    	for (String str : strList) {
    		strArray[counter++] = str;
    	}

        return strArray;
    }

    /**
     * Change the comment character
     *
     * @param comment
     *            The new comment character. Lines can begin with whitespace
     *            before the comment.
     */
    public void setComment(String comment) {
        this._comment = comment;
    }

    /**
     * Retrieve the current comment character.
     *
     * @return return The current comment character.
     */
    public String getComment() {
        return _comment;
    }

    /**
     * Get the value of the ignore whitespace lines setting.
     *
     * @return the current flag setting.
     */
    public boolean getIgnoreWhitespace() {
        return _ignoreWhitespaceLines;
    }

    /**
     * Override the ignore-whitespace-lines setting.
     *
     * @param ignore
     *            the new flag setting.
     */
    public void setIgnoreWhitespace(boolean ignore) {
        this._ignoreWhitespaceLines = ignore;
    }

}