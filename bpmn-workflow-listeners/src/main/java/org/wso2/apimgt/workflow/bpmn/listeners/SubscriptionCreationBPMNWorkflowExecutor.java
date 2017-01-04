package org.wso2.apimgt.workflow.bpmn.listeners;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apimgt.workflow.bpmn.util.BPMNRestAPIUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Triggering APIM to continue the subscription flow is done by this listener.
 */
public class SubscriptionCreationBPMNWorkflowExecutor implements TaskListener {

    private static Log log = LogFactory.getLog(SubscriptionCreationBPMNWorkflowExecutor.class);

    public void notify(DelegateTask task) {

        log.debug("Updating APIM subscription status.");

        String callbackURL = task.getVariable("callBackURL").toString();
        String workflowReference = task.getVariable("workflowExternalRef").toString();
        String status = task.getVariable("status").toString();
        String description = "Task Status update";

        String apimUser = System.getProperty("APIM_USER","admin");
        String apimPass = System.getProperty("APIM_PASSWORD","admin");

        String authHeader = BPMNRestAPIUtil.createBasicAuthHeader(apimUser, apimPass);

        Map<String, String> params = new HashMap<String, String>();
        params.put("workflowReference", workflowReference);
        params.put("status", "true".equals(status)?"APPROVED":"REJECTED");
        params.put("description", description);

        BPMNRestAPIUtil.callRestAPI(callbackURL, authHeader, params);

    }
}
