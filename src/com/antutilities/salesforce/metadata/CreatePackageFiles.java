package com.antutilities.salesforce.metadata;

import com.antutilities.salesforce.utils.FileLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by sjgeosits on 4/4/15.
 */
public class CreatePackageFiles extends SalesforceService {

    private List<String> objectMetadataTypeList = new LinkedList<String>();
    private List<String> remainingMetadataTypeList = new LinkedList<String>();
    private String baseDir;
    private String logDir;

    public CreatePackageFiles() {
        super();

        objectMetadataTypeList.add("BusinessProcess");
        objectMetadataTypeList.add("RecordType");
        objectMetadataTypeList.add("WebLink");
        objectMetadataTypeList.add("ValidationRule");
        //objectMetadataTypeList.add("NamedFilter");
        objectMetadataTypeList.add("SharingReason");
        objectMetadataTypeList.add("ListView");
        objectMetadataTypeList.add("FieldSet");

        remainingMetadataTypeList.add("AccountCriteriaBasedSharingRule");
        remainingMetadataTypeList.add("AccountOwnerSharingRule");
        remainingMetadataTypeList.add("AccountSharingRules");
        remainingMetadataTypeList.add("CampaignCriteriaBasedSharingRule");
        remainingMetadataTypeList.add("CampaignOwnerSharingRule");
        remainingMetadataTypeList.add("CampaignSharingRules");
        remainingMetadataTypeList.add("CaseCriteriaBasedSharingRule");
        remainingMetadataTypeList.add("CaseOwnerSharingRule");
        remainingMetadataTypeList.add("CaseSharingRules");
        remainingMetadataTypeList.add("ContactCriteriaBasedSharingRule");
        remainingMetadataTypeList.add("ContactOwnerSharingRule");
        remainingMetadataTypeList.add("ContactSharingRules");
        remainingMetadataTypeList.add("CustomObjectCriteriaBasedSharingRule");
        remainingMetadataTypeList.add("CustomObjectSharingRules");
        remainingMetadataTypeList.add("LeadCriteriaBasedSharingRule");
        remainingMetadataTypeList.add("LeadOwnerSharingRule");
        remainingMetadataTypeList.add("LeadSharingRules");
        remainingMetadataTypeList.add("OpportunityCriteriaBasedSharingRule");
        remainingMetadataTypeList.add("OpportunityOwnerSharingRule");
        remainingMetadataTypeList.add("OpportunitySharingRules");
    }

    public void execute() {
        try {
            generatePackageFiles();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setBaseDir(String baseDir) {
        if (!baseDir.endsWith("/")) {
            baseDir = baseDir + "/";
        }
        this.baseDir = baseDir;
    }

    public void setLogDir(String logDir) {
        if (!logDir.endsWith("/")) {
            logDir = logDir + "/";
        }
        this.logDir = logDir;
    }

    public Map<String, List<String>> getObjectLists() throws JSONException {

        Map<String, List<String>> map = new HashMap<String, List<String>>();
        map.put("standard", new ArrayList());
        map.put("custom", new ArrayList());

        if (login()) {
            String responseBody = restGet(salesforceRestUri + "/sobjects/");
            JSONObject objectsJson = new JSONObject(new JSONTokener(responseBody));
            JSONArray objectsJsonArray = objectsJson.getJSONArray("sobjects");

            List<String> list = new ArrayList<String>();

            if (objectsJsonArray!=null && objectsJsonArray.length()>0) {
                for (int j=0; j<objectsJsonArray.length(); j++) {

                    JSONObject objJson = objectsJsonArray.getJSONObject(j);
                    String custom = objJson.getString("custom");
                    if (custom.equalsIgnoreCase("true")) {
                        List<String> objList = map.get("custom");
                        objList.add(objJson.getString("name"));
                    }
                    else {
                        List<String> objList = map.get("standard");
                        objList.add(objJson.getString("name"));
                    }
                }
            }
        }
        else {
            log.error("ERROR: could not log into salesforce.");
        }

        return map;
    }

    public String buildObjectXML(Map<String, List<String>> objectMap) {

        StringBuffer sb = new StringBuffer();
        sb.append("\t<types>\r\n");
        sb.append("\t\t<members>*</members>\r\n");
        List<String> stdObjList = objectMap.get("standard");
        for (String o : stdObjList) {
            sb.append("\t\t<members>").append(o).append("</members>\r\n");
        }
        List<String> custObjList = objectMap.get("custom");
        for (String o : custObjList) {
            sb.append("\t\t<members>").append(o).append("</members>\r\n");
        }
        sb.append("\t\t<name>CustomObject</name>\r\n");
        sb.append("\t</types>\r\n");

        return sb.toString();
    }

    public List<String> parseLog(String fileName) {
        List<String> fields = new LinkedList<String>();

        FileLoader fl = new FileLoader();
        try {
            List<String> lines = fl.loadDataAsList(fileName);
            for (String line : lines) {
                // loop through the lines and get the ones that start with "FullName/Id:"
                // lines look like this: FullName/Id: Case.Queue_Feedback/00B000000095c8IEAQ
                if (line.contains("FullName/Id:")) {
                    String leftovers = line.substring(12);
                    int pos = leftovers.indexOf("/");
                    String field = leftovers.substring(0, pos);
                    fields.add(field.trim());
                }
            }
        }
        catch (IOException ex) {

        }
        return fields;
    }

    public String generateXMLTypeBlock(List<String> members, String name) {
        //Build an XML block like below, iterating over members
    	/*
	    	<types>
		    	<members>Account</members>
			    <name>CustomObject</name>
    		</types>
	    */

        StringBuffer sb = new StringBuffer();
        if (members.size() > 0) {
            sb.append("\t<types>\r\n");
            for (String s : members) {
                sb.append("\t\t<members>").append(s).append("</members>\r\n");
            }
            sb.append("\t\t<name>").append(name).append("</name>\r\n");
            sb.append("\t</types>\r\n");
        }
        return sb.toString();
    }

    public List<String> getStandardObjectFieldList(List<String> fieldList) {
        // we want to filter this list down to just the custom fields on the standard objects.
        // we don't want the custom fields on the custom objects, because they
        // will already get included with the custom objects.
        List<String> fields = new LinkedList<String>();

        for (String f : fieldList) {
            //Account.Auto_renewing__c  		//standard looks like this
            //Alert__c.Send_Support_Page__c 	//custom looks like this
            //Support_Alert__kav.Details__c		//custom also looks like this
            StringTokenizer st = new StringTokenizer(f, ".");
            String fo = st.nextToken();
            String ff = st.nextToken();
            boolean isCustom = false;
            if (fo.endsWith("__c")) {
                isCustom = true;
            }
            if (fo.endsWith("__kav")) {
                isCustom = true;
            }
            if (!isCustom) {
                fields.add(f.trim());
            }
        }
        return fields;
    }

    public void writePackageXML(String filename, String xml) throws IOException {

        FileWriter fw = new FileWriter(filename);
        fw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
        fw.append("<Package xmlns=\"http://soap.sforce.com/2006/04/metadata\">\r\n");
        fw.append(xml);
        fw.append("</Package>\r\n");
        fw.close();
    }

    public void generatePackageFiles() throws Exception {

        StringBuffer sb = new StringBuffer();

        // Custom objects
        Map<String, List<String>> map = getObjectLists();
        sb.append(buildObjectXML(map));

        // Custom Fields
        List<String> items = parseLog(logDir + "CustomField.log");
        List<String> stdObjFields = getStandardObjectFieldList(items);
        Collections.sort(stdObjFields);
        sb.append(generateXMLTypeBlock(stdObjFields, "CustomField"));

        // Get the rest of the other object-specific components
        for (String metadataType : objectMetadataTypeList) {
            items = parseLog(logDir + metadataType + ".log");
            Collections.sort(items);
            sb.append(generateXMLTypeBlock(items, metadataType));
        }
        System.out.println("Writing to " + baseDir + "/objects.xml");
        writePackageXML(baseDir + "/objects.xml", sb.toString());

        // Now get any remaining components which need to be dot qualified with info we get from ant.
        sb = new StringBuffer();

        for (String metadataType : remainingMetadataTypeList) {
            items = parseLog(logDir + metadataType + ".log");
            Collections.sort(items);
            sb.append(generateXMLTypeBlock(items, metadataType));
        }
        System.out.println("Writing to " + baseDir + "/remaining.xml");
        writePackageXML(baseDir + "/remaining.xml", sb.toString());
    }

    public static void main(String[] args) throws Exception {
        Environment.setupEnvironment(Environment.SCOTTDEV);
        CreatePackageFiles go = new CreatePackageFiles();

        go.setSfClientId((String) System.getenv("sf.clientId"));
        go.setSfClientSecret((String) System.getenv("sf.clientSecret"));
        go.setSfServer((String) System.getenv("sf.server"));
        go.setSfUsername((String) System.getenv("sf.username"));
        go.setSfPassword((String) System.getenv("sf.password"));
        go.setApiVersion((String) System.getenv("apiVersion"));
        go.setSfOauthEndpoint((String) System.getenv("sf.oauthEndpoint"));
        go.setSfRestEndpoint((String) System.getenv("sf.restEndpoint"));
        go.setBaseDir((String) System.getenv("baseDir"));
        go.setLogDir((String) System.getenv("logDir"));

        go.generatePackageFiles();
    }
}
