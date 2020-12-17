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
package org.springblade.flow.business.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.flow.business.service.FlowBusinessService;
import org.springblade.flow.core.constant.ProcessConstant;
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.entity.SingleFlow;
import org.springblade.flow.core.utils.TaskUtil;
import org.springblade.flow.engine.constant.FlowEngineConstant;
import org.springblade.flow.engine.utils.FlowCache;

import org.springblade.task.entity.LabelTask;
import org.springblade.task.feign.ILabelTaskClient;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 流程业务实现类
 *
 * @author Chill
 */
@Service
@AllArgsConstructor
public class FlowBusinessServiceImpl implements FlowBusinessService {

	private final TaskService taskService;
	private final HistoryService historyService;
	private final ILabelTaskClient iLabelTaskClient;

	@Override
	public IPage<SingleFlow> selectClaimPage(IPage<SingleFlow> page, BladeFlow bladeFlow) {
		String taskUser = TaskUtil.getTaskUser();
		String taskGroup = TaskUtil.getCandidateGroup();
		List<SingleFlow> flowList = new LinkedList<>();

		// 个人等待签收的任务
		TaskQuery claimUserQuery = taskService.createTaskQuery().taskCandidateUser(taskUser)
			.includeProcessVariables().active().orderByTaskCreateTime().desc();
		// 定制流程等待签收的任务
		TaskQuery claimRoleWithTenantIdQuery = taskService.createTaskQuery().taskTenantId(AuthUtil.getTenantId()).taskCandidateGroupIn(Func.toStrList(taskGroup))
			.includeProcessVariables().active().orderByTaskCreateTime().desc();
		// 通用流程等待签收的任务
		TaskQuery claimRoleWithoutTenantIdQuery = taskService.createTaskQuery().taskWithoutTenantId().taskCandidateGroupIn(Func.toStrList(taskGroup))
			.includeProcessVariables().active().orderByTaskPriority().desc().orderByTaskCreateTime().desc();

		// 构建列表数据
		buildFlowTaskList(bladeFlow, flowList, claimUserQuery, page, FlowEngineConstant.STATUS_CLAIM);
		buildFlowTaskList(bladeFlow, flowList, claimRoleWithTenantIdQuery, page, FlowEngineConstant.STATUS_CLAIM);
		buildFlowTaskList(bladeFlow, flowList, claimRoleWithoutTenantIdQuery, page, FlowEngineConstant.STATUS_CLAIM);

		// 计算总数
		long count = claimUserQuery.count() + claimRoleWithTenantIdQuery.count() + claimRoleWithoutTenantIdQuery.count();
		// 设置页数
		//page.setSize(page.getSize());
		// 设置总数u
		page.setTotal(count);
		// 设置数据
		page.setRecords(flowList);
		return page;
	}

	@Override
	public SingleFlow selectOneClaimPage() {
//		String taskUser = TaskUtil.getTaskUser();
		String taskGroup = TaskUtil.getCandidateGroup();

		TaskQuery taskQuery = taskService.createTaskQuery().taskWithoutTenantId().taskCandidateGroupIn(Func.toStrList(taskGroup))
			.includeProcessVariables().active().orderByTaskPriority().desc().orderByTaskCreateTime().desc();
				if(taskQuery.listPage(0, 1).size()!=0){
				Task task = taskQuery.listPage(0, 1).get(0);
				SingleFlow flow = new SingleFlow();
				flow.setTaskId(task.getId());
				flow.setTaskDefinitionKey(task.getTaskDefinitionKey());
				flow.setTaskName(task.getName());
				flow.setAssignee(task.getAssignee());
				flow.setCreateTime(task.getCreateTime());
				flow.setClaimTime(task.getClaimTime());
				flow.setExecutionId(task.getExecutionId());
				flow.setVariables(task.getProcessVariables());
				flow.setPriority(task.getPriority());
				ProcessDefinition processDefinition = FlowCache.getProcessDefinition(task.getProcessDefinitionId());
				flow.setCategory(processDefinition.getCategory());
				flow.setCategoryName(FlowCache.getCategoryName(processDefinition.getCategory()));
				flow.setProcessDefinitionId(processDefinition.getId());
				flow.setProcessDefinitionName(processDefinition.getName());
				flow.setProcessDefinitionKey(processDefinition.getKey());
				flow.setProcessDefinitionVersion(processDefinition.getVersion());
				flow.setProcessInstanceId(task.getProcessInstanceId());
				LabelTask labelTask = iLabelTaskClient.queryLabelTask(task.getProcessInstanceId()).getData();
				flow.setTemplateId(labelTask.getTemplateId());
				flow.setPersonId(labelTask.getPersonId());
				flow.setPersonName(labelTask.getPersonName());
				flow.setSubTaskId(labelTask.getId());
				return flow;
		}else{
			return new SingleFlow();
		}
	}

	@Override
	public IPage<SingleFlow> selectTodoPage(IPage<SingleFlow> page, BladeFlow bladeFlow) {
		String taskUser = TaskUtil.getTaskUser();
		List<SingleFlow> flowList = new LinkedList<>();

		// 已签收的任务
		TaskQuery todoQuery = taskService.createTaskQuery().taskAssignee(taskUser).active()
			.includeProcessVariables().orderByTaskCreateTime().desc();

		// 构建列表数据
		buildFlowTaskList(bladeFlow, flowList, todoQuery, page, FlowEngineConstant.STATUS_TODO);

		// 计算总数
		long count = todoQuery.count();
		// 设置页数
		page.setSize(count);
		// 设置总数
		page.setTotal(count);
		// 设置数据
		page.setRecords(flowList);
		return page;
	}

	@Override
	public IPage<SingleFlow> selectSendPage(IPage<SingleFlow> page, BladeFlow bladeFlow) {
		String taskUser = TaskUtil.getTaskUser();
		List<SingleFlow> flowList = new LinkedList<>();

		HistoricProcessInstanceQuery historyQuery = historyService.createHistoricProcessInstanceQuery().startedBy(taskUser).orderByProcessInstanceStartTime().desc();

		if (bladeFlow.getCategory() != null) {
			historyQuery.processDefinitionCategory(bladeFlow.getCategory());
		}
		if (bladeFlow.getBeginDate() != null) {
			historyQuery.startedAfter(bladeFlow.getBeginDate());
		}
		if (bladeFlow.getEndDate() != null) {
			historyQuery.startedBefore(bladeFlow.getEndDate());
		}

		// 查询列表
		List<HistoricProcessInstance> historyList = historyQuery.listPage(Func.toInt((page.getCurrent() - 1) * page.getSize()), Func.toInt(page.getSize()));

		historyList.forEach(historicProcessInstance -> {
			SingleFlow flow = new SingleFlow();
			// historicProcessInstance
			flow.setCreateTime(historicProcessInstance.getStartTime());
			flow.setEndTime(historicProcessInstance.getEndTime());
			flow.setVariables(historicProcessInstance.getProcessVariables());
			String[] businessKey = Func.toStrArray(StringPool.COLON, historicProcessInstance.getBusinessKey());
			if (businessKey.length > 1) {
				flow.setBusinessTable(businessKey[0]);
				flow.setBusinessId(businessKey[1]);
			}
			flow.setHistoryActivityName(historicProcessInstance.getName());
			flow.setProcessInstanceId(historicProcessInstance.getId());
			flow.setHistoryProcessInstanceId(historicProcessInstance.getId());
			// ProcessDefinition
			ProcessDefinition processDefinition = FlowCache.getProcessDefinition(historicProcessInstance.getProcessDefinitionId());
			flow.setProcessDefinitionId(processDefinition.getId());
			flow.setProcessDefinitionName(processDefinition.getName());
			flow.setProcessDefinitionVersion(processDefinition.getVersion());
			flow.setProcessDefinitionKey(processDefinition.getKey());
			flow.setCategory(processDefinition.getCategory());
			flow.setCategoryName(FlowCache.getCategoryName(processDefinition.getCategory()));
			flow.setProcessInstanceId(historicProcessInstance.getId());
			// HistoricTaskInstance
			List<HistoricTaskInstance> historyTasks = historyService.createHistoricTaskInstanceQuery().processInstanceId(historicProcessInstance.getId()).orderByHistoricTaskInstanceEndTime().desc().list();
			if (Func.isNotEmpty(historyTasks)) {
				HistoricTaskInstance historyTask = historyTasks.iterator().next();
				flow.setTaskId(historyTask.getId());
				flow.setTaskName(historyTask.getName());
				flow.setTaskDefinitionKey(historyTask.getTaskDefinitionKey());
			}
			// Status
			if (historicProcessInstance.getEndActivityId() != null) {
				flow.setProcessIsFinished(FlowEngineConstant.STATUS_FINISHED);
			} else {
				flow.setProcessIsFinished(FlowEngineConstant.STATUS_UNFINISHED);
			}
			flow.setStatus(FlowEngineConstant.STATUS_FINISH);
			LabelTask labelTask = iLabelTaskClient.queryLabelTask(historicProcessInstance.getId()).getData();
			flow.setTemplateId(labelTask.getTemplateId());
			flow.setPersonId(labelTask.getPersonId());
			flow.setPersonName(labelTask.getPersonName());
			flow.setSubTaskId(labelTask.getId());
			flowList.add(flow);
		});

		// 计算总数
		long count = historyQuery.count();
		// 设置总数
		page.setTotal(count);
		page.setRecords(flowList);
		return page;
	}

	@Override
	public IPage<SingleFlow> selectDonePage(IPage<SingleFlow> page, BladeFlow bladeFlow) {
		String taskUser = TaskUtil.getTaskUser();
		List<SingleFlow> flowList = new LinkedList<>();

		HistoricTaskInstanceQuery doneQuery = historyService.createHistoricTaskInstanceQuery().taskAssignee(taskUser).finished()
			.includeProcessVariables().orderByHistoricTaskInstanceEndTime().desc();

		if (bladeFlow.getCategory() != null) {
			doneQuery.processCategoryIn(Func.toStrList(bladeFlow.getCategory()));
		}
		if (bladeFlow.getBeginDate() != null) {
			doneQuery.taskCompletedAfter(bladeFlow.getBeginDate());
		}
		if (bladeFlow.getEndDate() != null) {
			doneQuery.taskCompletedBefore(bladeFlow.getEndDate());
		}

		// 查询列表
		List<HistoricTaskInstance> doneList = doneQuery.listPage(Func.toInt((page.getCurrent() - 1) * page.getSize()), Func.toInt(page.getSize()));
		doneList.forEach(historicTaskInstance -> {
			SingleFlow flow = new SingleFlow();
			flow.setTaskId(historicTaskInstance.getId());
			flow.setTaskDefinitionKey(historicTaskInstance.getTaskDefinitionKey());
			flow.setTaskName(historicTaskInstance.getName());
			flow.setAssignee(historicTaskInstance.getAssignee());
			flow.setCreateTime(historicTaskInstance.getCreateTime());
			flow.setExecutionId(historicTaskInstance.getExecutionId());
			flow.setHistoryTaskEndTime(historicTaskInstance.getEndTime());
			flow.setVariables(historicTaskInstance.getProcessVariables());

			ProcessDefinition processDefinition = FlowCache.getProcessDefinition(historicTaskInstance.getProcessDefinitionId());
			flow.setProcessDefinitionId(processDefinition.getId());
			flow.setProcessDefinitionName(processDefinition.getName());
			flow.setProcessDefinitionKey(processDefinition.getKey());
			flow.setProcessDefinitionVersion(processDefinition.getVersion());
			flow.setCategory(processDefinition.getCategory());
			flow.setCategoryName(FlowCache.getCategoryName(processDefinition.getCategory()));

			flow.setProcessInstanceId(historicTaskInstance.getProcessInstanceId());
			flow.setHistoryProcessInstanceId(historicTaskInstance.getProcessInstanceId());
			HistoricProcessInstance historicProcessInstance = getHistoricProcessInstance((historicTaskInstance.getProcessInstanceId()));
			if (Func.isNotEmpty(historicProcessInstance)) {
				String[] businessKey = Func.toStrArray(StringPool.COLON, historicProcessInstance.getBusinessKey());
				flow.setBusinessTable(businessKey[0]);
				flow.setBusinessId(businessKey[1]);
				if (historicProcessInstance.getEndActivityId() != null) {
					flow.setProcessIsFinished(FlowEngineConstant.STATUS_FINISHED);
				} else {
					flow.setProcessIsFinished(FlowEngineConstant.STATUS_UNFINISHED);
				}
			}
			flow.setStatus(FlowEngineConstant.STATUS_FINISH);
			LabelTask labelTask = iLabelTaskClient.queryLabelTask(historicTaskInstance.getProcessInstanceId()).getData();
			flow.setTemplateId(labelTask.getTemplateId());
			flow.setPersonId(labelTask.getPersonId());
			flow.setPersonName(labelTask.getPersonName());
			flow.setSubTaskId(labelTask.getId());
			flowList.add(flow);
		});
		// 计算总数
		long count = doneQuery.count();
		// 设置总数
		page.setTotal(count);
		page.setRecords(flowList);
		return page;
	}

	@Override
	public boolean completeTask(BladeFlow flow) {
		String taskId = flow.getTaskId();
		String processInstanceId = flow.getProcessInstanceId();
		String taskGroup = TaskUtil.getCandidateGroup();
		if (taskGroup.equals("ci")){
			iLabelTaskClient.changeStatus(processInstanceId);
		}
		String comment = Func.toStr(flow.getComment(), ProcessConstant.PASS_COMMENT);
		// 增加评论
		if (StringUtil.isNoneBlank(processInstanceId, comment)) {
			taskService.addComment(taskId, processInstanceId, comment);
		}
		// 创建变量
		Map<String, Object> variables = flow.getVariables();
		if (variables == null) {
			variables = Kv.create();
		}
		variables.put(ProcessConstant.PASS_KEY, flow.isPass());
		// 完成任务
		taskService.complete(taskId, variables);
		return true;
	}

	/**
	 * 构建流程
	 *
	 * @param bladeFlow 流程通用类
	 * @param flowList  流程列表
	 * @param taskQuery 任务查询类
	 * @param status    状态
	 */
	private void buildFlowTaskList(BladeFlow bladeFlow, List<SingleFlow> flowList, TaskQuery taskQuery, IPage<SingleFlow> page, String status) {
		if (bladeFlow.getCategory() != null) {
			taskQuery.processCategoryIn(Func.toStrList(bladeFlow.getCategory()));
		}
		if (bladeFlow.getBeginDate() != null) {
			taskQuery.taskCreatedAfter(bladeFlow.getBeginDate());
		}
		if (bladeFlow.getEndDate() != null) {
			taskQuery.taskCreatedBefore(bladeFlow.getEndDate());
		}
		List<Task> taskList = taskQuery.listPage(Func.toInt((page.getCurrent() - 1) * page.getSize()), Func.toInt(page.getSize()));
		taskList.forEach(task -> {
			SingleFlow flow = new SingleFlow();
			flow.setTaskId(task.getId());
			flow.setTaskDefinitionKey(task.getTaskDefinitionKey());
			flow.setTaskName(task.getName());
			flow.setAssignee(task.getAssignee());
			flow.setCreateTime(task.getCreateTime());
			flow.setClaimTime(task.getClaimTime());
			flow.setExecutionId(task.getExecutionId());
			flow.setVariables(task.getProcessVariables());
			flow.setPriority(task.getPriority());

			HistoricProcessInstance historicProcessInstance = getHistoricProcessInstance(task.getProcessInstanceId());
			if (Func.isNotEmpty(historicProcessInstance)) {
				String[] businessKey = Func.toStrArray(StringPool.COLON, historicProcessInstance.getBusinessKey());
				flow.setBusinessTable(businessKey[0]);
				flow.setBusinessId(businessKey[1]);
			}

			ProcessDefinition processDefinition = FlowCache.getProcessDefinition(task.getProcessDefinitionId());
			flow.setCategory(processDefinition.getCategory());
			flow.setCategoryName(FlowCache.getCategoryName(processDefinition.getCategory()));
			flow.setProcessDefinitionId(processDefinition.getId());
			flow.setProcessDefinitionName(processDefinition.getName());
			flow.setProcessDefinitionKey(processDefinition.getKey());
			flow.setProcessDefinitionVersion(processDefinition.getVersion());
			flow.setProcessInstanceId(task.getProcessInstanceId());
			flow.setStatus(status);
			LabelTask labelTask = iLabelTaskClient.queryLabelTask(task.getProcessInstanceId()).getData();
			flow.setTemplateId(labelTask.getTemplateId());
			flow.setPersonId(labelTask.getPersonId());
			flow.setPersonName(labelTask.getPersonName());
			flow.setSubTaskId(labelTask.getId());
			flowList.add(flow);
		});
	}

	/**
	 * 获取历史流程
	 *
	 * @param processInstanceId 流程实例id
	 * @return HistoricProcessInstance
	 */
	private HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
		return historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
	}

}
