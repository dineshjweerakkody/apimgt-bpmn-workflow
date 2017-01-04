package org.wso2.apimgt.workflow.bpmn.listeners.util;

import net.minidev.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Map;

public class BPMNRestAPIUtil {

    private static Log log = LogFactory.getLog(BPMNRestAPIUtil.class);

    private static String AUTH_HEADER = "Authorization";

    public static void callRestAPI(String url, String authHeader, Map<String, String> params) {
        HttpClient httpClient = new HttpClient();

        GetMethod method = new GetMethod(url);
        method.addRequestHeader("Content-Type", "application/json");
        if (authHeader != null) {
            method.addRequestHeader(AUTH_HEADER, authHeader);
        }

        try {
            NameValuePair [] nameValuePair= new NameValuePair[params.size()];
            int i=0;
            for(String name : params.keySet()){
                NameValuePair param = new NameValuePair();
                param.setName(name);
                param.setValue(params.get(name));
                nameValuePair[i++] = param;
            }

            method.setQueryString(nameValuePair);
            // Execute the method.
            int statusCode = httpClient.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                log.error("Method failed: " + method.getStatusLine());
            }

        } catch (HttpException e) {
            log.error("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.error("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
    }

    public static String createBasicAuthHeader(String userName, String password) {
        return "Basic " + new String(Base64.encodeBase64((userName + ":" + password).getBytes()));
    }

    public static JSONObject createVariable(String name, String value){
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("value", value);
        return object;
    }
}
