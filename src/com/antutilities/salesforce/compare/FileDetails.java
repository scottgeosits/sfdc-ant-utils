package com.antutilities.salesforce.compare;

import java.io.File;

public class FileDetails {

	private String name;
	private File file;
	private boolean inSource;
	private boolean inDest;
	
	public FileDetails() {
		
	}
	public FileDetails(String name, File file, boolean inSource, boolean inDest) {
		this.name = name;
		this.file = file;
		this.inSource = inSource;
		this.inDest = inDest;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public File getFile() {
		return file;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public boolean isInSource() {
		return inSource;
	}
	public void setInSource(boolean inSource) {
		this.inSource = inSource;
	}
	public boolean isInDest() {
		return inDest;
	}
	public void setInDest(boolean inDest) {
		this.inDest = inDest;
	}
	
}
