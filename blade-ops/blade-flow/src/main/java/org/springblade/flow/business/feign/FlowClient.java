/*
 *      Copyright (c) 2018-2028, Chill Zhuang All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: Chill 庄骞 (smallchill@163.com)
 */
package org.springblade.flow.business.feign;

import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.ValuedDataObject;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.flow.business.service.FlowBusinessService;
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.entity.SingleFlow;
import org.springblade.flow.core.feign.IFlowClient;
import org.springblade.flow.core.utils.TaskUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程远程调用实现类
 *
 * @author Chill
 */
@NonDS
@RestController
@AllArgsConstructor
public class FlowClient implements IFlowClient {

	private final RuntimeService runtimeService;
	private final IdentityService identityService;
	private final TaskService taskService;
	private final HistoryService historyService;
	private final FlowBusinessService flowBusinessService;
	private final RepositoryService repositoryService;

	@Override
	@PostMapping(START_PROCESS_INSTANCE_BY_ID)
	public R<BladeFlow> startProcessInstanceById(String processDefinitionId, String businessKey, @RequestBody Map<String, Object> variables) {
		// 设置流程启动用户
		identityService.setAuthenticatedUserId(TaskUtil.getTaskUser());

		Process process = repositoryService.getBpmnModel(processDefinitionId).getMainProcess();
		// 获取之前存入dataObjects的变量
		List<ValuedDataObject> datas = repositoryService.getBpmnModel(processDefinitionId).getMainProcess().getDataObjects();
		for (ValuedDataObject data : datas) {
			variables.put(data.getName(), data.getValue());
		}

		// 开启流程
		ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId, businessKey, variables);
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
		if(variables.get("priority")!=null){
			tasks.forEach(t -> {
				int p = (int)variables.get("priority");
				taskService.setPriority(t.getId(), p);
			});
		}

		// 组装流程通用类
		BladeFlow flow = new BladeFlow();
		flow.setProcessInstanceId(processInstance.getId());
		return R.data(flow);
	}

	@Override
	@PostMapping(START_PROCESS_INSTANCE_BY_ID_PARALLEL)
	public R<BladeFlow> startProcessInstanceByIdParallel(Long createUser, String processDefinitionId, String businessKey, @RequestBody Map<String, Object> variables) {
		// 设置流程启动用户
		identityService.setAuthenticatedUserId(TaskUtil.getTaskUser(createUser.toString()));

		Process process = repositoryService.getBpmnModel(processDefinitionId).getMainProcess();
		// 获取之前存入dataObjects的变量
		List<ValuedDataObject> datas = repositoryService.getBpmnModel(processDefinitionId).getMainProcess().getDataObjects();
		for (ValuedDataObject data : datas) {
			variables.put(data.getName(), data.getValue());
		}

		// 开启流程
		ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinitionId, businessKey, variables);
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();
		if(variables.get("priority")!=null){
			tasks.forEach(t -> {
				int p = (int)variables.get("priority");
				taskService.setPriority(t.getId(), p);
			});
		}

		// 组装流程通用类
		BladeFlow flow = new BladeFlow();
		flow.setProcessInstanceId(processInstance.getId());
		return R.data(flow);
	}

	@Override
	@PostMapping(START_PROCESS_INSTANCE_BY_KEY)
	public R<BladeFlow> startProcessInstanceByKey(String processDefinitionKey, String businessKey, @RequestBody Map<String, Object> variables) {
		// 设置流程启动用户
		identityService.setAuthenticatedUserId(TaskUtil.getTaskUser());
		// 开启流程
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
		// 组装流程通用类
		BladeFlow flow = new BladeFlow();
		flow.setProcessInstanceId(processInstance.getId());
		return R.data(flow);
	}

	@Override
	@PostMapping(COMPLETE_TASK)
	public R completeTask(String taskId, String processInstanceId, String comment, @RequestBody Map<String, Object> variables) {
		// 增加评论
		if (StringUtil.isNoneBlank(processInstanceId, comment)) {
			taskService.addComment(taskId, processInstanceId, comment);
		}
		// 非空判断
		if (Func.isEmpty(variables)) {
			variables = Kv.create();
		}
		// 完成任务
		taskService.complete(taskId, variables);
		return R.success("流程提交成功");
	}

	@Override
	@PostMapping(MK_COMPLETE_TASK)
	public R completeTask(@ApiParam("任务信息") @RequestBody SingleFlow flow) {
		if (!flow.getStatus().equals("finish")) {
			try {
				return R.data(flowBusinessService.completeTask(flow));
			} catch (FlowableObjectNotFoundException e) {
				return R.data(e.getMessage());
			}
		}else {
			return R.data(flowBusinessService.changeTaskComment(flow));
		}
	}

	@Override
	@GetMapping(TASK_VARIABLE)
	public R<Object> taskVariable(String taskId, String variableName) {
		return R.data(taskService.getVariable(taskId, variableName));
	}

	@Override
	@GetMapping(TASK_VARIABLES)
	public R<Map<String, Object>> taskVariables(String taskId) {
		return R.data(taskService.getVariables(taskId));
	}

	@Override
	@PostMapping(IS_PROCESS_INSTANCES_FINISHED)
	public R<Kv> isProcessInstancesFinished(@RequestBody List<String> ids) {
		Kv kv = Kv.create();
		ids.forEach(id -> {
			HistoricProcessInstance historicProcessInstance = historyService
				.createHistoricProcessInstanceQuery()
				.processInstanceId(id)
				.singleResult();
			if (historicProcessInstance == null) {
				kv.set(id, false);
			} else {
				if (historicProcessInstance.getEndActivityId() != null) {
					kv.set(id, true);
				} else {
					kv.set(id, false);
				}
			}
		});
		return R.data(kv);
	}

	@Override
	@PostMapping(SET_TASK_PRIORITY_BY_PROCESS_INSTANCE_ID)
	public R<Boolean> setTaskPriorityByProcessInstanceId(String processInstanceId, int priority) {
		Boolean res = flowBusinessService.setTaskPriorityByProcessInstanceId(processInstanceId, priority);
		return R.status(res);
	}

	@Override
	@PostMapping(SET_TASK_PRIORITY_BY_PROCESS_INSTANCE_IDS)
	public R<Boolean> setTaskPriorityByProcessInstanceIds(List<String> processInstanceIds, int priority) {
		Boolean res = flowBusinessService.setTaskPriorityByProcessInstanceIds(processInstanceIds, priority);
		return R.status(res);
	}

	@Override
	@PostMapping(TODO_TIMEOUT_HANDLER)
	public R<Boolean> todoTimeoutHandler() {
		Boolean res = flowBusinessService.todoTimeoutHandler();
		return R.status(res);
	}
}
