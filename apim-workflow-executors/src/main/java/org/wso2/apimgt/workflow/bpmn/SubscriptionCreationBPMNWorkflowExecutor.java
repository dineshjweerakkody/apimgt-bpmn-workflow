package org.wso2.apimgt.workflow.bpmn;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.apimgt.workflow.bpmn.util.BPMNRestAPIUtil;
import org.wso2.carbon.apimgt.impl.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;

public class SubscriptionCreationBPMNWorkflowExecutor extends WorkflowExecutor {

    private static Log log = LogFactory.getLog(SubscriptionCreationBPMNWorkflowExecutor.class);

    private String username;
    private String password;
    private String serviceEndpoint;
    private String processDefId;

    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION;
    }

    public List<WorkflowDTO> getWorkflowDetails(String s) throws WorkflowException {
        return null;
    }

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        log.info("Executing Subscription BPMN Workflow executor");

        SubscriptionWorkflowDTO subsWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;

        String user = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        String domain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        String userWithDomain = subsWorkflowDTO.getSubscriber();
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(domain)) {
            userWithDomain = user + APIConstants.EMAIL_DOMAIN_SEPARATOR + domain;
        }

        userWithDomain = APIUtil.replaceEmailDomainBack(userWithDomain);

        try {
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(userWithDomain);
            APIIdentifier apiIdentifier = new APIIdentifier(subsWorkflowDTO.getApiProvider(), subsWorkflowDTO
                    .getApiName(), subsWorkflowDTO.getApiVersion());
            API api = apiProvider.getAPI(apiIdentifier);

            String authHeader = BPMNRestAPIUtil.createBasicAuthHeader(username, password);

            JSONObject payloadObject = new JSONObject();
            payloadObject.put("processDefinitionId", processDefId);
            JSONArray variables = new JSONArray();
            variables.add(BPMNRestAPIUtil.createVariable("apiName", subsWorkflowDTO.getApiName()));
            variables.add(BPMNRestAPIUtil.createVariable("apiVersion", subsWorkflowDTO.getApiVersion()));
            variables.add(BPMNRestAPIUtil.createVariable("apiContext", subsWorkflowDTO.getApiContext()));
            variables.add(BPMNRestAPIUtil.createVariable("apiProvider", subsWorkflowDTO.getApiProvider()));
            variables.add(BPMNRestAPIUtil.createVariable("subscriber", subsWorkflowDTO.getSubscriber()));
            variables.add(BPMNRestAPIUtil.createVariable("applicationName", subsWorkflowDTO.getApplicationName()));
            variables.add(BPMNRestAPIUtil.createVariable("tierName", subsWorkflowDTO.getTierName()));
            variables.add(BPMNRestAPIUtil.createVariable("workflowExternalRef", subsWorkflowDTO.getExternalWorkflowReference()));
            variables.add(BPMNRestAPIUtil.createVariable("callBackURL", subsWorkflowDTO.getCallbackUrl() != null ?
                                                                        subsWorkflowDTO.getCallbackUrl(): "?"));
            payloadObject.put("variables", variables);
            String payload = payloadObject.toString();

            BPMNRestAPIUtil.callRestAPI(serviceEndpoint, authHeader, payload);

            super.execute(workflowDTO);
        } catch (APIManagementException e) {
            log.error("Error in SubscriptionCreationBPMNWorkflowExecutor", e);
        }

        return new GeneralWorkflowResponse();
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);
        log.info("Subscription Creation [Complete] Workflow Invoked. Workflow ID : " + workflowDTO
                .getExternalWorkflowReference() + "Workflow State : " + workflowDTO.getStatus());

        if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            try {
                apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),
                                                   APIConstants.SubscriptionStatus.UNBLOCKED);
            } catch (APIManagementException e) {
                log.error("Could not complete subscription creation workflow", e);
                throw new WorkflowException("Could not complete subscription creation workflow", e);
            }
        } else if (WorkflowStatus.REJECTED.equals(workflowDTO.getStatus())) {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            try {
                apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),
                                                   APIConstants.SubscriptionStatus.REJECTED);
            } catch (APIManagementException e) {
                log.error("Could not complete subscription creation workflow", e);
                throw new WorkflowException("Could not complete subscription creation workflow", e);
            }
        }
        return new GeneralWorkflowResponse();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    /**
     * This should be the full endpoint to start a task
     * Ex : https://localhost:9443/bpmn/runtime/process-instances
     *
     * @param serviceEndpoint
     */
    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public String getProcessDefId() {
        return processDefId;
    }

    public void setProcessDefId(String processDefId) {
        this.processDefId = processDefId;
    }
}