package org.wso2.apimgt.workflow.bpmn.listeners;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * This Executor will set the publisher as the assignee of the task.
 */
public class ChangeAssigneeTaskListner implements TaskListener {
    public void notify(DelegateTask task) {
        String provider = task.getVariable("apiProvider").toString();
        if(provider!=null) {
            task.setAssignee(provider);
        }
    }
}
