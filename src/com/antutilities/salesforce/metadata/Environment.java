package com.antutilities.salesforce.metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: scott.geosits
 * Date: 4/30/13
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class Environment extends EnvironmentalPropertyService {

    public static final String SCOTTDEV = "SCOTTDEV";

    public static void setupEnvironment(String env) {

        Map<String,String> envMap = new HashMap<String, String>();
        if (env.equals(SCOTTDEV)) {
            envMap.put("buildFilename", "/Users/sjgeosits/Workspace/varsityspiritorg/tempBulkBuild.xml");
            envMap.put("baseDir", "/Users/sjgeosits/Workspace/varsityspiritorg/");
            envMap.put("logDir", "/Users/sjgeosits/Workspace/varsityspiritorg/metadata/scottdev/lists/");
            envMap.put("apiVersion", "32.0");
            envMap.put("sf.oauthEndpoint", "/services/oauth2/token");
            envMap.put("sf.restEndpoint", "/services/data");
            envMap.put("sf.clientId", "3MVG9Gmy2zmPB01pw1yMGejjNJkVNOhIJhSmaj5yAE0hedMAWvMJH92XqMqd90iALIM0mb5yCchodeUjB7GU6");
            envMap.put("sf.clientSecret", "5170934007278527648");
            envMap.put("sf.server", "https://test.salesforce.com");
            envMap.put("sf.username", "sjgeosits@varsity.com.scott");
            envMap.put("sf.password", "josh102697LQPfdV5WV8zW0IFoUKqZy8");
        }
        setEnv(envMap);
    }
}
