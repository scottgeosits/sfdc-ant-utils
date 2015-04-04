package com.antutilities.salesforce.metadata;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.antutilities.salesforce.utils.SalesforceStringUtils;

public class CreateDataloaderMapping {

	private String outputFile;
	private String objectName;
	private String srcDir;
	
	public String getSrcDir() {
		return srcDir;
	}

	public void setSrcDir(String srcDir) {
		this.srcDir = srcDir;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public List<String> getObjectFieldNames(String fileName) throws Exception {
				
		List<String> fields = new LinkedList<String>();
		
		File fXmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();

		if (!doc.getDocumentElement().getNodeName().equals("CustomObject")) {
			throw new IllegalArgumentException("XML file is not of correct structure.");
		}
		
		NodeList nList = doc.getDocumentElement().getChildNodes();
		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				if (nNode.getNodeName().equals("fields")) {

					NodeList nameNodeList = eElement.getElementsByTagName("fullName");
					if (nameNodeList.getLength() > 0) {
						String name = nameNodeList.item(0).getTextContent();
						fields.add(name);
					}
				}
			}
		}
		return fields;
	}

	public void createDataloaderMapping() throws Exception {
		// Get the field names from the metadata.
		List<String> fields = getObjectFieldNames(this.srcDir + "/" + this.objectName + ".object");
		FileWriter fw = new FileWriter(this.outputFile);
		fw.append("Id=Id").append(SalesforceStringUtils.LINE_SEP);
		fw.append("Name=Name").append(SalesforceStringUtils.LINE_SEP);
		for (String f : fields) {
			fw.append(f).append("=").append(f).append(SalesforceStringUtils.LINE_SEP);
		}
		fw.close();
	}
	
	public void execute() {
		try {
			createDataloaderMapping();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		CreateDataloaderMapping cdm = new CreateDataloaderMapping();
		cdm.setObjectName("Account");
		cdm.setOutputFile("/Users/sjgeosits/Workspace/VarsitySalesforce/workarea/accountExportMapping.sdl");
		cdm.setSrcDir("/Users/sjgeosits/Workspace/VarsitySalesforce/orgs/prod/src/objects");
		cdm.createDataloaderMapping();
	}

}
