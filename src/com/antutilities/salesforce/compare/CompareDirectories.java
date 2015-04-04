package com.antutilities.salesforce.compare;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.tools.ant.util.StringUtils;

import com.antutilities.salesforce.utils.FileLoader;
import com.antutilities.salesforce.utils.SalesforceStringUtils;


public class CompareDirectories {

	private FileLoader fl = new FileLoader();
	private String srcDir = "";
	private String destDir = "";
	private String outputDir = "";
	private String sharedDirectory = "";
	private static DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");

	private String getHTMLHeader() {
		StringBuffer header = new StringBuffer();
		header.append("<html>");
		header.append("<head>");
		header.append("<style>");
		header.append("body { font-family: Tahoma; font-size: 1.0em; color: #00055; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("td { font-size: 0.8em; color: #00000; font-weight: bold; }").append(SalesforceStringUtils.LINE_SEP);
		header.append(".filehidden { display: none; } ");
		header.append("div.collapsible { display: block; position: relative; font-size: 0.8em; color: #00000; height: auto; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("div.filename { display: block; position: relative; font-size: 0.8em; color: #00000; width: 50%; height: auto; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("ul { padding: 0 0 0 0; margin: 0 0 0 0; } ");
		header.append("li { display: inline; list-style-image: none; width: 50%; position: relative; } ");
		header.append("li.fileheader, li.directoryheaderleft, li.directoryheaderright, ");
		header.append("li.directory, li.match, li.matchbold, li.insert, ");
		header.append("li.delete, li.change, li.move, li.empty ");
		header.append(" { font-size: 0.6em; display: inline-block; width: 48%; } ");
		header.append("li.directoryheaderleft { cursor: pointer; font-size: 0.8em; background-color: #ff0000; font-weight: bold; color: #ffffff }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.directoryheaderright { cursor: pointer; font-size: 0.8em; background-color: #0000ff; font-weight: bold; color: #ffffff }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.fileheader { cursor: pointer; font-size: 0.8em; background-color: #000000; font-weight: bold; color: #ffffff }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.directory { font-weight: bold; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.match { background-color: #ffffff; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.matchbold { background-color: #ffffff; font-weight: bold; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.insert { background-color: #ccccff; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.delete { background-color: #ffcccc; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.change { background-color: #aaffaa; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.move { background-color: #ffff00; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.empty { background-color: #cccccc; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.spacer { font-size: 0.6em; display: inline-block; background-color: #ffffff; width: 4%; }").append(SalesforceStringUtils.LINE_SEP);
		//header.append("li.headerspacer { font-size: 0.8em; background-color: #000000; display: inline-block; width: 4%; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.headerspacer { font-size: 0.8em; background-color: #ffffff; display: inline-block; width: 4%; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.directoryheaderspacerleft { font-size: 0.8em; background-color: #ff0000; display: inline-block; width: 4%; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.directoryheaderspacerright { font-size: 0.8em; background-color: #0000ff; display: inline-block; width: 4%; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("table { table-layout:fixed; width:100%; } ").append(SalesforceStringUtils.LINE_SEP);
		header.append("table td { word-wrap:break-word; } ").append(SalesforceStringUtils.LINE_SEP);
		header.append("tr { width: 100%; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("#maintable { width: 100%;} ").append(SalesforceStringUtils.LINE_SEP);
		header.append("a:link, a:visited, a:hover { text-decoration: none; cursor: pointer; color: #000000; } ");
		header.append("</style>");
		header.append("</head>");
		header.append("<body>");
		header.append("<div id=\"maintable\">");
		return header.toString();
	}
	
	private String getHTMLIndexHeader() {
		StringBuffer header = new StringBuffer();
		header.append("<html>");
		header.append("<head>");
		header.append("<style>");
		header.append("body { font-family: Tahoma; font-size: 1.0em; color: #00055; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("h1 { font-size: 1.1em; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.indexmatch { background-color: #ffffff; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.indexchange { background-color: #aaffaa; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.indexinsert { background-color: #ccccff; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li.indexdelete { background-color: #ffcccc; }").append(SalesforceStringUtils.LINE_SEP);
		header.append("li { display: block; list-style-image: none; width: 100%; font-size: 0.7em; position: relative; } ");
		header.append("li.label { display: block; font-size: 0.9em; color: #000000; font-weight: bold; width: 40%; } ");
		header.append("li.value { display: block; font-size: 0.9em; color: #0000b0; font-weight: normal; width: 60%; } ");
		header.append("ul { padding: 0 0 0 0; margin: 0 0 0 0; } ");
		header.append("a:link, a:visited, a:hover { cursor: pointer; color: #000000; } ");
		header.append("#showallfileslabel { font-size: 0.8em; color: #000000; } ");
		header.append("</style>");
		header.append("</head>");
		header.append("<body>");
		header.append("<ul>");
		return header.toString();
	}

	private String getHTMLFooter() {
		StringBuffer footer = new StringBuffer();
		// Note add the following line if using tables.
		footer.append("</div>");
		footer.append("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7/jquery.js\"></script>");
		footer.append("<script>");
		footer.append("		(function($) {");
		footer.append("			$(\".collapsible\").on(\"click\", function(e) {");
		footer.append("				var id = $(this).attr(\"id\");");
		footer.append("				var childId = id.substring(7);");
		footer.append(" 			var oldTitle = $(\"#title-\" + childId).text().substring(3);");
		footer.append("				if ($(\"#\" + childId).is(\":visible\")) {");
		footer.append("					$(\"#\" + childId).fadeOut('slow');");
		footer.append("					$(\"#title-\" + childId).text(\" + \" + oldTitle);"); 
		footer.append("				}");
		footer.append("				else {");
		footer.append("					$(\"#\" + childId).fadeIn('slow');");
		footer.append("					$(\"#title-\" + childId).text(\" - \" + oldTitle);"); 
		footer.append("				}");
		footer.append("			});");
		footer.append("		})(jQuery);");
		footer.append("</script>");
		footer.append("</body>");
		footer.append("</html>");
		return footer.toString();
	}
	
	private String getHTMLIndexFooter() {
		StringBuffer footer = new StringBuffer();
		// Note add the following line if using tables.
		footer.append("</ul>");
		footer.append("</div>");
		footer.append("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7/jquery.js\"></script>");
		footer.append("<script>");
		footer.append("		(function($) {");
		footer.append("			$(\"#showallfiles\").on(\"click\", function(e) {");
		footer.append("				if ($(this).is(\":checked\")) {");
		footer.append("					$(\"li.indexmatch\").hide();");
		footer.append("				}");
		footer.append("				else {");
		footer.append("					$(\"li.indexmatch\").show();");
		footer.append("				}");
		footer.append("			});");
		footer.append("		})(jQuery);");
		footer.append("</script>");
		footer.append("</body>");
		footer.append("</html>");
		return footer.toString();
	}

	public void compareDirectories(String currentSrcDir, String currentDestDir) throws Exception {
		
		StringBuffer htmlDiffOutput;

		//System.out.println("Current src dir = " + currentSrcDir);
		//System.out.println("Current dest dir = " + currentDestDir);
		
		Map<String, FileDetails> fileMap = new TreeMap<String, FileDetails>();
		
		File sourceDirFile = new File(currentSrcDir);
		File[] sourceFiles = sourceDirFile.listFiles();
		for (int idx = 0; idx < sourceFiles.length; idx++) {
			File f = sourceFiles[idx];
			fileMap.put(f.getName(), new FileDetails(f.getName(), f, true, false));
		}
		
		File destDirFile = new File(currentDestDir);
		File[] destFiles = destDirFile.listFiles();
		for (int idx = 0; idx < destFiles.length; idx++) {
			File f = destFiles[idx];
			FileDetails fi = fileMap.get(f.getName());
			if (fi != null) {
				fi.setInDest(true);
			}
			else {
				fileMap.put(f.getName(), new FileDetails(f.getName(), f, false, true));
			}
		}
		
		// Go through each file and compare.
		for (FileDetails fi : fileMap.values()) {
			File f = fi.getFile();
			
			if (f.isDirectory()) {
				
				// Create a directory file.
				openDirectoryOverviewFile(f.getName());
				writeDirectoryOutput(f.getName());
				
				// And write to the top level directory package file.
				if (fi.isInSource() && fi.isInDest()) {
					// If directory exists in both, compare them.
					compareDirectories(currentSrcDir + "/" + f.getName(), currentDestDir + "/" + f.getName());
				}
				else if (fi.isInSource()) {
					// If directory exists in source but not in dest, 
					// List the directory
					htmlDiffOutput = new StringBuffer();
					//String out = StringUtils.replace(getFileHeader(f.getName(), "&nbsp;"), "class=\"fileheader\"", "class=\"directoryheaderleft\"");
					//out = StringUtils.replace(out, "class=\"headerspacer\"", "class=\"directoryheaderspacerleft\"");
					//htmlDiffOutput.append(out);
					String out = "";
					
					// Get all files under.
					File[] subdirFiles = f.listFiles();
					for (int jdx = 0; jdx < subdirFiles.length; jdx++) {
						String filename = subdirFiles[jdx].getName();
						out = getFileRow(filename, "matchbold", "&nbsp;", "matchbold");
						htmlDiffOutput.append(out);
					}

					//htmlDiffOutput.append(getFileFooter()).append(SalesforceStringUtils.LINE_SEP);
					//writeIndexOutput(htmlDiffOutput.toString(), "directoryheaderspacerleft");
				}
				else {
					// If directory exists in dest but not in source,
					// List the directory
					htmlDiffOutput = new StringBuffer();
					String out = StringUtils.replace(getFileHeader("&nbsp;", f.getName()), "class=\"fileheader\"", "class=\"directoryheaderright\"");
					out = StringUtils.replace(out, "class=\"headerspacer\"", "class=\"directoryheaderspacerright\"");
					htmlDiffOutput.append(out);
					
					// Get all files under.
					File[] subdirFiles = f.listFiles();
					for (int jdx = 0; jdx < subdirFiles.length; jdx++) {
						String filename = subdirFiles[jdx].getName();
						out = getFileRow("&nbsp;", "matchbold", filename, "matchbold");
						htmlDiffOutput.append(out);
					}

					htmlDiffOutput.append(getFileFooter()).append(SalesforceStringUtils.LINE_SEP);
					//writeOutput(htmlDiffOutput.toString());
				}
				closeDirectoryOverviewFile(f.getName());
			}
			else {
				if (fi.isInSource() && fi.isInDest()) {
					// If file exists in both,
					compareFiles(currentSrcDir + "/" + f.getName(), currentDestDir + "/" + f.getName());
				}
				else if (fi.isInSource()) {
					// If file exists in source but not in dest, 
					htmlDiffOutput = new StringBuffer();
					htmlDiffOutput.append(getFileHeader(fi.getFile().getAbsolutePath().substring(srcDir.length() + 1), "&nbsp;"));
					List<String> lines = fl.loadDataAsList(fi.getFile().getAbsolutePath());
					for (String line : lines) {
						line = cleanupForHTML(line);
						htmlDiffOutput.append(getFileRow(line, "delete", "&nbsp;", "empty"));
					}
					htmlDiffOutput.append(getFileFooter()).append(SalesforceStringUtils.LINE_SEP);
					String dirFile = fi.getFile().getAbsolutePath().substring(srcDir.length() + 1);
					writeOutput(dirFile + ".htm", htmlDiffOutput.toString());
					writeIndexOutput(dirFile, "indexdelete");
				}
				else {
					// If file exists in dest but not in source,
					htmlDiffOutput = new StringBuffer();
					htmlDiffOutput.append(getFileHeader("&nbsp;",fi.getFile().getAbsolutePath().substring(destDir.length() + 1)));
					List<String> lines = fl.loadDataAsList(fi.getFile().getAbsolutePath());
					for (String line : lines) {
						line = cleanupForHTML(line);
						htmlDiffOutput.append(getFileRow("&nbsp;", "empty", line, "insert"));
					}
					htmlDiffOutput.append(getFileFooter()).append(SalesforceStringUtils.LINE_SEP);

					String dirFile = fi.getFile().getAbsolutePath().substring(destDir.length() + 1);
					writeOutput(dirFile + ".htm", htmlDiffOutput.toString());
					writeIndexOutput(dirFile, "indexinsert");
				}
			}
		}
	}
	
	public void compareFiles(String srcFileName, String destFileName) throws Exception {

		StringBuffer htmlDiffOutput = new StringBuffer();
		String dirFile = srcFileName.substring(srcDir.length() + 1);
		
		TextDiff diff = new TextDiff();
		Report rept = diff.compare(srcFileName, destFileName);

		if (rept.size() == 1 && rept.getCommand(0).command.equals("Match")) {
			EditCommand cmd = rept.getCommand(0);
			writeIndexOutput(dirFile, "indexmatch");
			return;
		}
		else {
			htmlDiffOutput.append(getFileHeader(srcFileName.substring(srcDir.length() + 1), destFileName.substring(destDir.length() + 1)));
			for (int idx = 0; idx < rept.size(); idx++) {
				EditCommand cmd = rept.getCommand(idx);
				if (cmd.command.equals("Insert before")
					|| cmd.command.equals("Append")) {
					
					// Insert a block of new lines into the old file.
					// Lines exist in dest, but not in src
					String[] lines = cmd.newLines.lines;
					for (int jdx = 0; jdx < lines.length; jdx++) {
						String line = cleanupForHTML(lines[jdx]);
						htmlDiffOutput.append(getFileRow("&nbsp;", "empty", line, "insert"));
					}
				}
				else if (cmd.command.equals("Match")) {
					// Lines match - no edit required
					String[] newLines = cmd.newLines.lines;
					if (newLines.length > 10) {
						for (int jdx = 0; jdx < 5; jdx++) {
							String newLine = cleanupForHTML(newLines[jdx]);
							htmlDiffOutput.append(getFileRow(newLine, "match", newLine, "match"));
						}
						htmlDiffOutput.append(getFileRow("...", "match", "...", "match"));
						for (int jdx = newLines.length - 6; jdx < newLines.length; jdx++) {
							String newLine = cleanupForHTML(newLines[jdx]);
							htmlDiffOutput.append(getFileRow(newLine, "match", newLine, "match"));
						}
					}
					else {
						// Just display 'em all...
						for (int jdx = 0; jdx < newLines.length; jdx++) {
							String newLine = cleanupForHTML(newLines[jdx]);
							htmlDiffOutput.append(getFileRow(newLine, "match", newLine, "match"));
						}
					}
				}
				else if (cmd.command.equals("Move")) {
					// A block of matching lines have changed order
					// Change a block in the old file to a different block in the new file.
					String[] oldLines = cmd.oldLines.lines;
					String[] newLines = cmd.newLines.lines;
					int maxLength = oldLines.length > newLines.length ? oldLines.length : newLines.length;
					
					if (maxLength > 50) {
						htmlDiffOutput.append(getFileRow("*** TOO MANY LINES DIFFERENT ***", "match", "*** TOO MANY LINES DIFFERENT ***", "match"));
					}
					else {
						for (int jdx = 0; jdx < maxLength; jdx++) {
							
							String oldLine = "";
							String newLine = "";
							
							if (jdx < oldLines.length) {
								oldLine = cleanupForHTML(oldLines[jdx]);
							}
							if (jdx < newLines.length) {
								newLine = cleanupForHTML(newLines[jdx]);
							}
							if (oldLine.trim().equals(newLine.trim())) {
								htmlDiffOutput.append(getFileRow(oldLine, "match", newLine, "match"));
							}
							else {
								htmlDiffOutput.append(getFileRow(oldLine, "move", newLine, "move"));
							}
						}
					}
				}
				else if (cmd.command.equals("Change")) {

					// Change a block in the old file to a different block in the new file.
					String[] oldLines = cmd.oldLines.lines;
					String[] newLines = cmd.newLines.lines;
					int maxLength = oldLines.length > newLines.length ? oldLines.length : newLines.length;

					if (maxLength > 50) {
						htmlDiffOutput.append(getFileRow("*** TOO MANY LINES DIFFERENT ***", "change", "*** TOO MANY LINES DIFFERENT ***", "change"));
					}
					else {
						for (int jdx = 0; jdx < maxLength; jdx++) {
							
							String oldLine = "";
							String newLine = "";
							
							if (jdx < oldLines.length) {
								oldLine = cleanupForHTML(oldLines[jdx]);
							}
							if (jdx < newLines.length) {
								newLine = cleanupForHTML(newLines[jdx]);
							}
							if (oldLine.trim().equals(newLine.trim())) {
								htmlDiffOutput.append(getFileRow(oldLine, "match", newLine, "match"));
							}
							else {
								htmlDiffOutput.append(getFileRow(oldLine, "change", newLine, "change"));
							}
						}
					}
				}
				else if (cmd.command.equals("Delete")) {
					// Delete a block from the old file.
					// Lines exist in src, but not in dest
					String[] lines = cmd.oldLines.lines;
					for (int jdx = 0; jdx < lines.length; jdx++) {
						String line = cleanupForHTML(lines[jdx]);
						htmlDiffOutput.append(getFileRow(line, "delete", "&nbsp;", "empty"));
					}
				}
				else {
					System.out.println("ERROR - UNRECOGNIZED COMMAND TYPE.");
				}
			}
			htmlDiffOutput.append(getFileFooter()).append(SalesforceStringUtils.LINE_SEP);
		}
		
		writeOutput(dirFile + ".htm", htmlDiffOutput.toString());
		writeIndexOutput(dirFile, "indexchange");
	}

	private void writeOutput(String filename, String output) throws IOException {
		String revisedFilename = StringUtils.replace(filename,  "/", "-");
		revisedFilename = StringUtils.replace(revisedFilename, "\\", "-");
		FileWriter fw = new FileWriter(this.outputDir + "/files/" + revisedFilename);
		fw.append(getHTMLHeader());
		fw.append(output);
		fw.append(getHTMLFooter());
		fw.close();
	}
	
	private void writeIndexOutput(String componentname, String classname) throws IOException {
		String revisedFilename = StringUtils.replace(componentname, "/", "-");
		int pos1 = componentname.lastIndexOf("/");
		if (pos1 >= 0) {
			String directory = componentname.substring(0, pos1);
			
			StringBuffer sb = new StringBuffer();
			sb.append("<li class=\"");
			sb.append(classname);
			sb.append("\">");
			if (!classname.equals("indexmatch")) {
				sb.append("<a href=\"");
				sb.append("files/" + revisedFilename);
				sb.append(".htm\" target=\"metadataFrame\">");
				sb.append(componentname);
				sb.append("</a>");
			}
			else {
				sb.append(componentname);
			}
			sb.append("</li>");
			writeFile(this.outputDir + "/allmetadata-frame.htm", sb.toString(), true);

			sb = new StringBuffer();
			sb.append("<li class=\"");
			sb.append(classname);
			sb.append("\">");
			if (!classname.equals("indexmatch")) {
				sb.append("<a href=\"");
				sb.append(revisedFilename);
				sb.append(".htm\" target=\"metadataFrame\">");
				sb.append(componentname.substring(directory.length() + 1));
				sb.append("</a>");
			}
			else {
				sb.append(componentname.substring(directory.length() + 1));
			}
			sb.append("</li>");
			writeFile(this.outputDir + "/files/" + directory + ".htm", sb.toString(), true);
		}
	}
	
	private void writeDirectoryOutput(String componentName) throws IOException {
		String revisedFilename = StringUtils.replace(componentName, "/", "-");

		StringBuffer sb = new StringBuffer();
		sb.append("<li>");
		sb.append("<a href=\"");
		sb.append("files/" + revisedFilename);
		sb.append(".htm\" target=\"metadataListFrame\">");
		sb.append(componentName);
		sb.append("</a>");
		sb.append("</li>");

		writeFile(this.outputDir + "/overview-frame.htm", sb.toString(), true);
	}
	
	private String cleanupForHTML(String line) {
		line = StringUtils.replace(line, "<", "&lt;");
		line = StringUtils.replace(line, ">", "&gt;");
		line = StringUtils.replace(line, " ", "&nbsp;");
		line = StringUtils.replace(line, "	", "&nbsp;&nbsp;&nbsp;&nbsp;");
		return line;
	}

	private String getFileHeader(String file1, String file2) {

		StringBuffer output = new StringBuffer();
		String id = StringUtils.replace(file1,  ".", "-");
		id = StringUtils.replace(id, "/", "-");
		
		String title = "";
		String filename = "";
		int pos = file1.lastIndexOf("/");
		if (pos < 0) {
			pos = file2.lastIndexOf("/");
			title = file2.substring(pos + 1);
			filename = StringUtils.replace(file2, "/", "-");
		}
		else {
			title = file1.substring(pos + 1);
			filename = StringUtils.replace(file1, "/", "-");
		}
		
		output.append("<h1><a href=\"").append(this.outputDir + "/files/" + filename).append(".htm\" target=\"_newpage\">");
		output.append(title).append("</a></h1>").append(SalesforceStringUtils.LINE_SEP);
		output.append("<ul>");
		output.append("<li id=\"title-").append(id).append("\" class=\"directoryheaderleft\">&nbsp;");
		output.append(srcDir.substring(this.sharedDirectory.length()));
		output.append("</li>");
		output.append("<li class=\"headerspacer\">");
		output.append("&nbsp;");
		output.append("</li>");
		output.append("<li class=\"directoryheaderright\">&nbsp;");
		output.append(destDir.substring(this.sharedDirectory.length()));
		output.append("</li>");
		return output.toString();
	}

	private String getFileFooter() {
		return "</ul></div></div>";
	}
	
	private String getFileRow(String entry1, String class1, String entry2, String class2) {
		StringBuffer output = new StringBuffer();
		output.append("<li class=\"").append(class1).append("\">");
		output.append(entry1);
		output.append("</li>");
		output.append("<li class=\"spacer\" >");
		output.append("&nbsp;");
		output.append("</li>");
		output.append("<li class=\"").append(class2).append("\">");
		output.append(entry2);
		output.append("</li>");
		output.append(SalesforceStringUtils.LINE_SEP);
		return output.toString();
	}

	public String getSharedSrcDestDirectoryPortion() {
		List<String> srcDirList = new LinkedList<String>();
		List<String> destDirList = new LinkedList<String>();
		
		StringTokenizer srcT = new StringTokenizer(srcDir, "/");
		while (srcT.hasMoreElements()) {
			srcDirList.add((String)srcT.nextElement());
		}
		StringTokenizer destT = new StringTokenizer(destDir, "/");
		while (destT.hasMoreElements()) {
			destDirList.add((String)destT.nextElement());
		}
		
		// Now go through each list.
		StringBuffer sharedPortion = new StringBuffer();
		sharedPortion.append("/");
		
		for (int idx = 0; idx < srcDirList.size(); idx++) {
			if (!srcDirList.get(idx).equals(destDirList.get(idx))) {
				// Found a difference.  Break.
				break;
			}
			else {
				sharedPortion.append(srcDirList.get(idx)).append("/");
			}
		}
		
		return sharedPortion.toString();
	}
	
	public String getSrcDir() {
		return srcDir;
	}


	public void setSrcDir(String srcDir) {
		this.srcDir = srcDir;
	}


	public String getDestDir() {
		return destDir;
	}


	public void setDestDir(String destDir) {
		this.destDir = destDir;
	}


	public String getOutputDir() {
		return outputDir;
	}


	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	private void writeFile(String filename, String output, boolean append) throws IOException {
		
		FileWriter fw = new FileWriter(filename, append);
		fw.append(output);
		fw.close();
	}
	
	private void openOverviewFiles() throws IOException {

		StringBuffer sb = new StringBuffer();
		
		// Main frameset
		sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">");
		sb.append("<!--NewPage-->").append(SalesforceStringUtils.LINE_SEP);
		sb.append("<html>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("   <head>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("      <title>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("      Salesforce Org Difference Report").append(SalesforceStringUtils.LINE_SEP);
		sb.append("      </title>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("      <script type=\"text/javascript\">").append(SalesforceStringUtils.LINE_SEP);
		sb.append("          targetPage = \"\" + window.location.search;").append(SalesforceStringUtils.LINE_SEP);
		sb.append("          if (targetPage != \"\" && targetPage != \"undefined\")").append(SalesforceStringUtils.LINE_SEP);
		sb.append("              targetPage = targetPage.substring(1);").append(SalesforceStringUtils.LINE_SEP);
		sb.append("          if (targetPage.indexOf(\":\") != -1)").append(SalesforceStringUtils.LINE_SEP);
		sb.append("              targetPage = \"undefined\";").append(SalesforceStringUtils.LINE_SEP);
		sb.append("          function loadFrames() {").append(SalesforceStringUtils.LINE_SEP);
		sb.append("              if (targetPage != \"\" && targetPage != \"undefined\")").append(SalesforceStringUtils.LINE_SEP);
		sb.append("                   top.classFrame.location = top.targetPage;").append(SalesforceStringUtils.LINE_SEP);
		sb.append("          }").append(SalesforceStringUtils.LINE_SEP);
		sb.append("      </script>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("     <noscript>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("     </noscript>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("   </head>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("   <frameset cols=\"27%,73%\" title=\"\" onLoad=\"top.loadFrames()\">").append(SalesforceStringUtils.LINE_SEP);
		sb.append("      <frameset rows=\"40%,60%\" title=\"\" onLoad=\"top.loadFrames()\">").append(SalesforceStringUtils.LINE_SEP);
		sb.append("         <frame src=\"overview-frame.htm\" name=\"componentTypeFrame\" title=\"All Components\">").append(SalesforceStringUtils.LINE_SEP);
		sb.append("         <frame src=\"allmetadata-frame.htm\" name=\"metadataListFrame\" title=\"All Metadata\">").append(SalesforceStringUtils.LINE_SEP);
		sb.append("      </frameset>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("      <frame src=\"overview-summary.htm\" name=\"metadataFrame\" title=\"Component type descriptions\">").append(SalesforceStringUtils.LINE_SEP);
		sb.append("      <noframes>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("         <h2>Frame Alert</h2>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("         <p>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("This document is designed to be viewed using the frames feature. If you see this message, you are using a non-frame-capable web client.").append(SalesforceStringUtils.LINE_SEP);
		sb.append("         <br/>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("Link to <a href=\"overview-summary.html\">Non-frame version.</A>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("      </noframes>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("   </frameset>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("</html>").append(SalesforceStringUtils.LINE_SEP);
		writeFile(this.outputDir + "/index.htm", sb.toString(), false);

		// Overview summary page.
		Date d = new Date();
		String dateVal = dateFormatter.format(d);
		
		sb = new StringBuffer();
		sb.append(getHTMLIndexHeader());
		sb.append("<h1>Salesforce Org Comparison</h1>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("<p>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("<li class=\"label\">Comparison Run On:</li>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("<li class=\"value\">").append(dateVal).append("</li>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("<li class=\"label\">Source Org:</li>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("<li class=\"value\">").append(srcDir).append("</li>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("<li class=\"label\">Target Org:</li>").append(SalesforceStringUtils.LINE_SEP);
		sb.append("<li class=\"value\">").append(destDir).append("</li>").append(SalesforceStringUtils.LINE_SEP);
		writeFile(this.outputDir + "/overview-summary.htm", sb.toString(), false);
		
		// Directory list
		sb = new StringBuffer();
		sb.append(getHTMLIndexHeader());
		sb.append("<h1><a href=\"allmetadata-frame.htm\" target=\"metadataListFrame\">All Components</a></h1>");
		writeFile(this.outputDir + "/overview-frame.htm", sb.toString(), false);

		// Global metadata list
		sb = new StringBuffer();
		sb.append(getHTMLIndexHeader());
		sb.append("<input type=\"checkbox\" id=\"showallfiles\">");
		sb.append("<span id=\"showallfileslabel\">Show Only Changed Components</span></input>");
		sb.append(SalesforceStringUtils.LINE_SEP);
		sb.append("<h1>All Components</h1>").append(SalesforceStringUtils.LINE_SEP);
		writeFile(this.outputDir + "/allmetadata-frame.htm", sb.toString(), false);

		// Create the files directory too.
		File f = new File(this.outputDir + "/files");
		if (!f.exists()) {
			f.mkdir();
		}
	}

	private void openDirectoryOverviewFile(String directory) throws IOException {
		StringBuffer sb = new StringBuffer();
		sb.append(getHTMLIndexHeader());
		sb.append("<input type=\"checkbox\" id=\"showallfiles\">");
		sb.append("<span id=\"showallfileslabel\">Show Only Changed Components</span></input>");
		sb.append(SalesforceStringUtils.LINE_SEP);
		sb.append("<h1>").append(directory.substring(0,1).toUpperCase()).append(directory.substring(1)).append("</h1>");
		writeFile(this.outputDir + "/files/" + directory + ".htm", sb.toString(), false);
	}
	
	private void closeDirectoryOverviewFile(String directory) throws IOException {
		writeFile(this.outputDir + "/files/" + directory + ".htm", getHTMLIndexFooter(), true);
	}

	private void closeOverviewFiles() throws IOException {
		writeFile(this.outputDir + "/overview-frame.htm", getHTMLIndexFooter(), true);
		writeFile(this.outputDir + "/overview-summary.htm", getHTMLIndexFooter(), true);
		writeFile(this.outputDir + "/allmetadata-frame.htm", getHTMLIndexFooter(), true);
	}

	public void execute() {
		try {
			this.sharedDirectory = getSharedSrcDestDirectoryPortion();
			openOverviewFiles();
			compareDirectories(this.srcDir, this.destDir);
			closeOverviewFiles();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		CompareDirectories cd = new CompareDirectories();
		//cd.setSrcDir("/Users/scott.geosits/Workspaces/VarsitySalesforce/orgs/prod/src");
		//cd.setDestDir("/Users/scott.geosits/Workspaces/VarsitySalesforce/orgs/qa/src");
		//cd.setSrcDir("/Users/scott.geosits/Desktop/src");
		//cd.setDestDir("/Users/scott.geosits/Desktop/dest");
		//cd.setOutputDir("/Users/scott.geosits/Desktop/junk");
		
		cd.setSrcDir("C:/Temp/CarlosBakerySF/orgs/cbProduction");
		cd.setDestDir("C:/Temp/CarlosBakerySF/orgs/cbdev");
		cd.setOutputDir("C:/Temp/junk");

		cd.execute();
	}
			
}
