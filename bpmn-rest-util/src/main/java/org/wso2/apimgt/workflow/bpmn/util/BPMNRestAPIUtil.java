package org.wso2.apimgt.workflow.bpmn.util;

import net.minidev.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Map;

public class BPMNRestAPIUtil {

    enum METHODS {GET, POST}

    ;

    private static Log log = LogFactory.getLog(BPMNRestAPIUtil.class);

    private static String AUTH_HEADER = "Authorization";
    private static String contentTypeJSON="application/json";

    public static void callRestAPI(String url, String authHeader, String payload) {
        callRestAPI(METHODS.POST, url, authHeader, contentTypeJSON, payload, null);
    }

    public static void callRestAPI(String url, String authHeader, Map<String, String> getParams) {
        callRestAPI(METHODS.GET, url, authHeader, contentTypeJSON, null, getParams);
    }

    public static void callRestAPI(METHODS method, String url, String authHeader, String contentType, String
            postPayload, Map<String, String> getParams) {
        HttpClient httpClient = new HttpClient();
        HttpMethod methodObj = null;

        try {
            if (method == METHODS.POST) {
                PostMethod postMethod = new PostMethod(url);
                StringRequestEntity requestEntity = new StringRequestEntity(
                        postPayload,
                        "application/json",
                        "UTF-8");

                postMethod.setRequestEntity(requestEntity);
                methodObj = postMethod;
            } else {
                GetMethod getMethod = new GetMethod(url);
                NameValuePair[] nameValuePair = new NameValuePair[getParams.size()];
                int i = 0;
                for (String name : getParams.keySet()) {
                    NameValuePair param = new NameValuePair();
                    param.setName(name);
                    param.setValue(getParams.get(name));
                    nameValuePair[i++] = param;
                }
                getMethod.setQueryString(nameValuePair);
                methodObj = getMethod;
            }

            methodObj.addRequestHeader("Content-Type", "application/json");
            if (authHeader != null) {
                methodObj.addRequestHeader(AUTH_HEADER, authHeader);
            }

            int statusCode = httpClient.executeMethod(methodObj);

            if (statusCode != HttpStatus.SC_OK) {
                log.error("Method failed: " + methodObj.getStatusLine());
            }

        } catch (HttpException e) {
            log.error("Fatal protocol violation: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.error("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Release the connection.
            methodObj.releaseConnection();
        }
    }

    /*public static void callGetRestAPI(String url, String authHeader, Map<String, String> params) {
        HttpClient httpClient = new HttpClient();

        GetMethod method = new GetMethod(url);
        method.addRequestHeader("Content-Type", "application/json");
        if (authHeader != null) {
            method.addRequestHeader(AUTH_HEADER, authHeader);
        }

        try {
            NameValuePair[] nameValuePair = new NameValuePair[params.size()];
            int i = 0;
            for (String name : params.keySet()) {
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
    }*/

    public static String createBasicAuthHeader(String userName, String password) {
        return "Basic " + new String(Base64.encodeBase64((userName + ":" + password).getBytes()));
    }

    public static JSONObject createVariable(String name, String value) {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("value", value);
        return object;
    }
}
