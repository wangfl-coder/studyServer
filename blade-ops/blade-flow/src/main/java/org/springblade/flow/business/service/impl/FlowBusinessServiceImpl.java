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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.*;
import org.springblade.flow.business.service.FlowBusinessService;
import org.springblade.flow.core.constant.ProcessConstant;
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.entity.SingleFlow;
import org.springblade.flow.core.utils.TaskUtil;
import org.springblade.flow.engine.constant.FlowEngineConstant;
import org.springblade.flow.engine.mapper.FlowMapper;
import org.springblade.flow.engine.utils.FlowCache;

import org.springblade.system.cache.SysCache;
import org.springblade.task.vo.ExpertLabelTaskVO;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.feign.ILabelTaskClient;
import org.springblade.task.feign.IQualityInspectionTaskClient;
import org.springblade.task.feign.ITaskClient;
import org.springblade.task.vo.ExpertQualityInspectionTaskVO;
import org.springblade.task.vo.TaskVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

/**
 * 流程业务实现类
 *
 * @author Chill
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlowBusinessServiceImpl implements FlowBusinessService {

	private final TaskService taskService;
	private final HistoryService historyService;
	private final ILabelTaskClient iLabelTaskClient;
	private final ITaskClient iTaskClient;
	private final IQualityInspectionTaskClient iQualityInspectionTaskClient;
	private final IExpertClient iExpertClient;
	private final FlowMapper flowMapper;


	@Value("${spring.profiles.active}")
	public String env;
	private final RepositoryService repositoryService;

	@Override
	public IPage<SingleFlow> selectClaimPage(IPage<SingleFlow> page, BladeFlow bladeFlow) {
		String taskUser = TaskUtil.getTaskUser();
		String taskGroup;
		if (null != bladeFlow.getRoleId()) {
			List<String> res = SysCache.getRoleAliases(bladeFlow.getRoleId().toString());
			if (Func.isNotEmpty(res)) {
				taskGroup = StringUtil.collectionToCommaDelimitedString(res);
			}else {
				return null;
			}
		}else {
			taskGroup = TaskUtil.getCandidateGroup();
		}
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
		long count = claimRoleWithoutTenantIdQuery.count();
		// 设置总数u
		List<String> taskGroupList = Func.toStrList(taskGroup);
		Integer total = (Integer)iLabelTaskClient.queryLabelTaskClaimCount(taskGroupList).getData();
		if(bladeFlow.getCategoryName().equals("标注流程")){
			page.setTotal(total);
		} else if(bladeFlow.getCategoryName().equals("质检流程")){
			page.setTotal(count-total);
		}
		// 设置数据
		page.setRecords(flowList);
		return page;
	}

	@Override
	public SingleFlow selectOneClaimPage(String categoryName, Long roleId) {
//		String taskUser = TaskUtil.getTaskUser();
		String taskGroup;
		if (null != roleId) {
			List<String> res = SysCache.getRoleAliases(roleId.toString());
			if (Func.isNotEmpty(res)) {
				taskGroup = StringUtil.collectionToCommaDelimitedString(res);
			}else {
				return null;
			}
		}else {
			taskGroup = TaskUtil.getCandidateGroup();
		}

		TaskQuery taskQuery = taskService.createTaskQuery().taskWithoutTenantId().taskCandidateGroupIn(Func.toStrList(taskGroup))
			.includeProcessVariables().active().orderByTaskPriority().desc().orderByTaskCreateTime().desc();

		if (taskQuery.listPage(0, 1).size() != 0) {
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

			BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
			UserTask userTask = (UserTask)bpmnModel.getFlowElement(task.getTaskDefinitionKey());
			Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
			List<ExtensionElement> extCompId = extensionElements.get(ProcessConstant.COMPOSITION_ID);
			if (Func.isNotEmpty(extCompId))
				flow.setCompositionId(extCompId.get(0).getElementText());
			List<ExtensionElement> extCompType = extensionElements.get(ProcessConstant.COMPOSITION_TYPE);
			if (Func.isNotEmpty(extCompType))
				flow.setCompositionType(Integer.valueOf(extCompType.get(0).getElementText()));
			List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
			if (Func.isNotEmpty(extField))
				flow.setCompositionField(extField.get(0).getElementText());

			if (categoryName.equals("标注流程")) {
				LabelTask labelTask = iLabelTaskClient.queryLabelTask(task.getProcessInstanceId()).getData();

//					log.error("processInstanceId:"+task.getProcessInstanceId());
//					log.error("taskId:"+task.getId());

				flow.setTemplateId(labelTask.getTemplateId());
				flow.setPersonId(labelTask.getPersonId());
				flow.setPersonName(labelTask.getPersonName());
				flow.setSubTaskId(labelTask.getId());
			} else if (categoryName.equals("质检流程")) {
				QualityInspectionTask qualityInspectionTask = iQualityInspectionTaskClient.queryQualityInspectionTask(task.getProcessInstanceId()).getData();
				flow.setTemplateId(qualityInspectionTask.getTemplateId());
				flow.setPersonId(qualityInspectionTask.getPersonId());
				flow.setPersonName(qualityInspectionTask.getPersonName());
				flow.setSubTaskId(qualityInspectionTask.getId());
				flow.setInspectionTaskId(qualityInspectionTask.getInspectionTaskId());
				flow.setLabelTaskId(qualityInspectionTask.getLabelTaskId());
				flow.setAnnotationTaskId(qualityInspectionTask.getTaskId());
			}
			return flow;

		} else {
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
//		// 设置页数
//		page.setSize(count);
		// 设置总数
		Integer total = (Integer)iLabelTaskClient.queryLabelTaskTodoCount(taskUser).getData();
		if(bladeFlow.getCategoryName().equals("标注流程")){
			page.setTotal(total);
		} else if(bladeFlow.getCategoryName().equals("质检流程")){
			page.setTotal(count-total);
		}
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
		if (bladeFlow.getTaskId() != null) {
			R<org.springblade.task.entity.Task> taskRes = iTaskClient.getById(Long.valueOf(bladeFlow.getTaskId()));
			if (!taskRes.isSuccess()){
				log.error("获取任务信息失败！");
				return null;
			}
			org.springblade.task.entity.Task task = taskRes.getData();
//			historyQuery.processDefinitionId(task.getProcessDefinitionId());
		}
		String taskName = bladeFlow.getTaskName();
		if (bladeFlow.getProcessIsFinished() != null) {
			if (bladeFlow.getProcessIsFinished().equals(FlowEngineConstant.STATUS_FINISHED)) {
				historyQuery.finished();
			} else if (bladeFlow.getProcessIsFinished().equals(FlowEngineConstant.STATUS_UNFINISHED)) {
				historyQuery.unfinished();
			}
		}

		// 查询列表
		List<HistoricProcessInstance> historyList = new ArrayList<>();
		if (null == taskName) {
			historyList = historyQuery.listPage(Func.toInt((page.getCurrent() - 1) * page.getSize()), Func.toInt(page.getSize()));
		} else {
			historyList = historyQuery.list();
		}


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
				if (null != taskName && !taskName.equals(historyTask.getName()))
					return;
				flow.setTaskId(historyTask.getId());
				flow.setTaskName(historyTask.getName());
				flow.setTaskDefinitionKey(historyTask.getTaskDefinitionKey());

				BpmnModel bpmnModel = repositoryService.getBpmnModel(historyTask.getProcessDefinitionId());
				UserTask userTask = (UserTask)bpmnModel.getFlowElement(historyTask.getTaskDefinitionKey());
				Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
				List<ExtensionElement> extCompId = extensionElements.get(ProcessConstant.COMPOSITION_ID);
				if (Func.isNotEmpty(extCompId))
					flow.setCompositionId(extCompId.get(0).getElementText());
				List<ExtensionElement> extCompType = extensionElements.get(ProcessConstant.COMPOSITION_TYPE);
				if (Func.isNotEmpty(extCompType))
					flow.setCompositionType(Integer.valueOf(extCompType.get(0).getElementText()));
				List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
				if (Func.isNotEmpty(extField))
					flow.setCompositionField(extField.get(0).getElementText());
			}
			// Status
			if (historicProcessInstance.getEndActivityId() != null) {
				flow.setProcessIsFinished(FlowEngineConstant.STATUS_FINISHED);
			} else {
				flow.setProcessIsFinished(FlowEngineConstant.STATUS_UNFINISHED);
			}
			flow.setStatus(FlowEngineConstant.STATUS_FINISH);

			if (bladeFlow.getCategoryName().equals("标注流程")) {
				LabelTask labelTask = iLabelTaskClient.queryLabelTask(historicProcessInstance.getId()).getData();
				if(labelTask.getId() != null) {
					flow.setTemplateId(labelTask.getTemplateId());
					flow.setPersonId(labelTask.getPersonId());
					flow.setPersonName(labelTask.getPersonName());
					flow.setSubTaskId(labelTask.getId());
					flowList.add(flow);
				}
			} else if (bladeFlow.getCategoryName().equals("质检流程")) {
				QualityInspectionTask qualityInspectionTask = iQualityInspectionTaskClient.queryQualityInspectionTask(historicProcessInstance.getId()).getData();
				if(qualityInspectionTask.getId() != null) {
					flow.setTemplateId(qualityInspectionTask.getTemplateId());
					flow.setPersonId(qualityInspectionTask.getPersonId());
					flow.setPersonName(qualityInspectionTask.getPersonName());
					flow.setSubTaskId(qualityInspectionTask.getId());
					flow.setInspectionTaskId(qualityInspectionTask.getInspectionTaskId());
					flow.setLabelTaskId(qualityInspectionTask.getLabelTaskId());
					flow.setAnnotationTaskId(qualityInspectionTask.getTaskId());
					flowList.add(flow);
				}
			}

		});

		// 计算总数
		long count = 0;
		if (null == taskName) {
			count = historyQuery.count();
			// 设置总数
			page.setTotal(count);
			page.setRecords(flowList);
		} else {
			count = flowList.size();
			// 设置总数
			page.setTotal(count);
			int start = Func.toInt((page.getCurrent() - 1) * page.getSize());
			long end = (start + page.getSize()) > flowList.size() ? flowList.size() : (start + page.getSize());
			page.setRecords(flowList.subList(start, (int)end));
			page.setRecords(flowList);
		}
		return page;
	}

	@Override
	public IPage<SingleFlow> selectDonePage(IPage<SingleFlow> page, BladeFlow bladeFlow) {
		String taskUser = TaskUtil.getTaskUser();
		List<SingleFlow> flowList = new LinkedList<>();

		HistoricTaskInstanceQuery doneQuery = historyService.createHistoricTaskInstanceQuery().taskAssignee(taskUser).finished()
			.includeProcessVariables().orderByHistoricTaskInstanceEndTime().desc().taskDeleteReason(null);

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
			flow.setPriority(historicTaskInstance.getPriority());
			List<Comment> commentList = taskService.getTaskComments(historicTaskInstance.getId());
			if (commentList.size() > 0) {
				Comment comment = commentList.get(0);
				flow.setComment(comment.getFullMessage());
			}

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

			BpmnModel bpmnModel = repositoryService.getBpmnModel(historicTaskInstance.getProcessDefinitionId());
			UserTask userTask = (UserTask)bpmnModel.getFlowElement(historicTaskInstance.getTaskDefinitionKey());
			Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
			List<ExtensionElement> extCompId = extensionElements.get(ProcessConstant.COMPOSITION_ID);
			if (Func.isNotEmpty(extCompId))
				flow.setCompositionId(extCompId.get(0).getElementText());
			List<ExtensionElement> extCompType = extensionElements.get(ProcessConstant.COMPOSITION_TYPE);
			if (Func.isNotEmpty(extCompType))
				flow.setCompositionType(Integer.valueOf(extCompType.get(0).getElementText()));
			List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
			if (Func.isNotEmpty(extField))
				flow.setCompositionField(extField.get(0).getElementText());

			if (bladeFlow.getCategoryName().equals("标注流程")){
				LabelTask labelTask = iLabelTaskClient.queryLabelTask(historicTaskInstance.getProcessInstanceId()).getData();
				if (labelTask.getId() != null) {
					flow.setTemplateId(labelTask.getTemplateId());
					flow.setPersonId(labelTask.getPersonId());
					flow.setPersonName(labelTask.getPersonName());
					flow.setSubTaskId(labelTask.getId());
					flowList.add(flow);
				}
			} else if (bladeFlow.getCategoryName().equals("质检流程")){
				QualityInspectionTask qualityInspectionTask = iQualityInspectionTaskClient.queryQualityInspectionTask(historicTaskInstance.getProcessInstanceId()).getData();
				if (qualityInspectionTask.getId() != null) {
					flow.setTemplateId(qualityInspectionTask.getTemplateId());
					flow.setPersonId(qualityInspectionTask.getPersonId());
					flow.setPersonName(qualityInspectionTask.getPersonName());
					flow.setSubTaskId(qualityInspectionTask.getId());
					flow.setInspectionTaskId(qualityInspectionTask.getInspectionTaskId());
					flow.setLabelTaskId(qualityInspectionTask.getLabelTaskId());
					flow.setAnnotationTaskId(qualityInspectionTask.getTaskId());
					flowList.add(flow);
				}
			}

		});
		// 计算总数
		long count = doneQuery.count();
		// 设置总数
		Integer total = (Integer)iLabelTaskClient.queryLabelTaskDoneCount(taskUser).getData();
		if(bladeFlow.getCategoryName().equals("标注流程")){
			page.setTotal(total);
		} else if(bladeFlow.getCategoryName().equals("质检流程")){
			page.setTotal(count-total);
		}
		page.setRecords(flowList);
		return page;
	}

	@Override
	public IPage<SingleFlow> selectDonePageByPersonId(BladeFlow bladeFlow,IPage<SingleFlow> page) {
		String taskUser = TaskUtil.getTaskUser();
		List<SingleFlow> flowList = new LinkedList<>();
		List<ExpertLabelTaskVO> expertLabelTaskVOS = new ArrayList<>();
		List<ExpertQualityInspectionTaskVO> expertQualityInspectionTaskVOS = new ArrayList<>();

		if (bladeFlow.getCategoryName().equals("标注流程")){
			if(bladeFlow.getPersonId()!=null) {
				List<LabelTask> labelTasks = iLabelTaskClient.queryLabelTaskByPersonId(bladeFlow.getPersonId()).getData();
				for(LabelTask labelTask:labelTasks){
					ExpertLabelTaskVO expertLabelTaskVO = Objects.requireNonNull(BeanUtil.copy(labelTask, ExpertLabelTaskVO.class));
					expertLabelTaskVOS.add(expertLabelTaskVO);
				}
			}else if(bladeFlow.getExpertId() != null && !bladeFlow.getExpertId().isEmpty()){
				expertLabelTaskVOS = iLabelTaskClient.queryLabelTaskByExpertId(bladeFlow.getExpertId()).getData();
			}else {
				return selectDonePage(page, bladeFlow);
			}
			expertLabelTaskVOS.forEach(expertProcessInstanceVO -> {
				if (expertProcessInstanceVO.getId() != null) {
					SingleFlow flow = new SingleFlow();
					flow.setTemplateId(expertProcessInstanceVO.getTemplateId());
					flow.setPersonId(expertProcessInstanceVO.getPersonId());
					flow.setPersonName(expertProcessInstanceVO.getPersonName());
					flow.setSubTaskId(expertProcessInstanceVO.getId());
					flow.setProcessInstanceId(expertProcessInstanceVO.getProcessInstanceId());
					if(bladeFlow.getExpertId()!=null){
						flow.setExpertId(bladeFlow.getExpertId());
					}
					HistoricTaskInstanceQuery doneQuery = historyService.createHistoricTaskInstanceQuery().taskAssignee(taskUser).finished()
						.includeProcessVariables().taskDeleteReason(null).processInstanceId(flow.getProcessInstanceId());
					if (bladeFlow.getCategory() != null) {
						doneQuery.processCategoryIn(Func.toStrList(bladeFlow.getCategory()));
					}
					if (bladeFlow.getBeginDate() != null) {
						doneQuery.taskCompletedAfter(bladeFlow.getBeginDate());
					}
					if (bladeFlow.getEndDate() != null) {
						doneQuery.taskCompletedBefore(bladeFlow.getEndDate());
					}
					if (doneQuery.listPage(0, 1).size() != 0) {
						HistoricTaskInstance historicTaskInstance = doneQuery.listPage(0, 1).get(0);
						flow.setTaskId(historicTaskInstance.getId());
						flow.setTaskDefinitionKey(historicTaskInstance.getTaskDefinitionKey());
						flow.setTaskName(historicTaskInstance.getName());
						flow.setAssignee(historicTaskInstance.getAssignee());
						flow.setCreateTime(historicTaskInstance.getCreateTime());
						flow.setExecutionId(historicTaskInstance.getExecutionId());
						flow.setHistoryTaskEndTime(historicTaskInstance.getEndTime());
						flow.setVariables(historicTaskInstance.getProcessVariables());
						flow.setPriority(historicTaskInstance.getPriority());

						ProcessDefinition processDefinition = FlowCache.getProcessDefinition(historicTaskInstance.getProcessDefinitionId());
						flow.setProcessDefinitionId(processDefinition.getId());
						flow.setProcessDefinitionName(processDefinition.getName());
						flow.setProcessDefinitionKey(processDefinition.getKey());
						flow.setProcessDefinitionVersion(processDefinition.getVersion());
						flow.setCategory(processDefinition.getCategory());
						flow.setCategoryName(FlowCache.getCategoryName(processDefinition.getCategory()));

						//flow.setProcessInstanceId(historicTaskInstance.getProcessInstanceId());
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
						BpmnModel bpmnModel = repositoryService.getBpmnModel(historicTaskInstance.getProcessDefinitionId());
						UserTask userTask = (UserTask) bpmnModel.getFlowElement(historicTaskInstance.getTaskDefinitionKey());
						Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
						List<ExtensionElement> extCompId = extensionElements.get(ProcessConstant.COMPOSITION_ID);
						if (Func.isNotEmpty(extCompId))
							flow.setCompositionId(extCompId.get(0).getElementText());
						List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
						if (Func.isNotEmpty(extField))
							flow.setCompositionField(extField.get(0).getElementText());
						flowList.add(flow);
					}
				}
			});
		} else if (bladeFlow.getCategoryName().equals("质检流程")){
			if(bladeFlow.getPersonId()!=null) {
				List<QualityInspectionTask> qualityInspectionTasks = iQualityInspectionTaskClient.queryQualityInspectionTaskByPersonId(bladeFlow.getPersonId()).getData();
				for(QualityInspectionTask qualityInspectionTask:qualityInspectionTasks){
					ExpertQualityInspectionTaskVO expertQualityInspectionTaskVO = Objects.requireNonNull(BeanUtil.copy(qualityInspectionTask, ExpertQualityInspectionTaskVO.class));
					expertQualityInspectionTaskVOS.add(expertQualityInspectionTaskVO);
				}
			}else if(bladeFlow.getExpertId()!=null && !bladeFlow.getExpertId().isEmpty()){
				expertQualityInspectionTaskVOS = iQualityInspectionTaskClient.queryQualityInspectionTaskByExpertId(bladeFlow.getExpertId()).getData();
			}else{
				return selectDonePage(page,bladeFlow);
			}
			expertQualityInspectionTaskVOS.forEach(expertProcessInstanceVO -> {
				if (expertProcessInstanceVO.getId() != null) {
					SingleFlow flow = new SingleFlow();
					flow.setTemplateId(expertProcessInstanceVO.getTemplateId());
					flow.setPersonId(expertProcessInstanceVO.getPersonId());
					flow.setPersonName(expertProcessInstanceVO.getPersonName());
					flow.setSubTaskId(expertProcessInstanceVO.getId());
					flow.setInspectionTaskId(expertProcessInstanceVO.getInspectionTaskId());
					flow.setLabelTaskId(expertProcessInstanceVO.getLabelTaskId());
					flow.setAnnotationTaskId(expertProcessInstanceVO.getTaskId());
					flow.setProcessInstanceId(expertProcessInstanceVO.getProcessInstanceId());
					if(bladeFlow.getExpertId()!=null){
						flow.setExpertId(bladeFlow.getExpertId());
					}
					HistoricTaskInstanceQuery doneQuery = historyService.createHistoricTaskInstanceQuery().taskAssignee(taskUser).finished()
						.includeProcessVariables().taskDeleteReason(null).processInstanceId(flow.getProcessInstanceId());
					if (bladeFlow.getCategory() != null) {
						doneQuery.processCategoryIn(Func.toStrList(bladeFlow.getCategory()));
					}
					if (bladeFlow.getBeginDate() != null) {
						doneQuery.taskCompletedAfter(bladeFlow.getBeginDate());
					}
					if (bladeFlow.getEndDate() != null) {
						doneQuery.taskCompletedBefore(bladeFlow.getEndDate());
					}
					if (doneQuery.listPage(0, 1).size() != 0) {
						HistoricTaskInstance historicTaskInstance = doneQuery.listPage(0, 1).get(0);
						flow.setTaskId(historicTaskInstance.getId());
						flow.setTaskDefinitionKey(historicTaskInstance.getTaskDefinitionKey());
						flow.setTaskName(historicTaskInstance.getName());
						flow.setAssignee(historicTaskInstance.getAssignee());
						flow.setCreateTime(historicTaskInstance.getCreateTime());
						flow.setExecutionId(historicTaskInstance.getExecutionId());
						flow.setHistoryTaskEndTime(historicTaskInstance.getEndTime());
						flow.setVariables(historicTaskInstance.getProcessVariables());
						flow.setPriority(historicTaskInstance.getPriority());

						ProcessDefinition processDefinition = FlowCache.getProcessDefinition(historicTaskInstance.getProcessDefinitionId());
						flow.setProcessDefinitionId(processDefinition.getId());
						flow.setProcessDefinitionName(processDefinition.getName());
						flow.setProcessDefinitionKey(processDefinition.getKey());
						flow.setProcessDefinitionVersion(processDefinition.getVersion());
						flow.setCategory(processDefinition.getCategory());
						flow.setCategoryName(FlowCache.getCategoryName(processDefinition.getCategory()));

						//flow.setProcessInstanceId(historicTaskInstance.getProcessInstanceId());
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
						BpmnModel bpmnModel = repositoryService.getBpmnModel(historicTaskInstance.getProcessDefinitionId());
						UserTask userTask = (UserTask)bpmnModel.getFlowElement(historicTaskInstance.getTaskDefinitionKey());
						Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
						List<ExtensionElement> extCompId = extensionElements.get(ProcessConstant.COMPOSITION_ID);
						if (Func.isNotEmpty(extCompId))
							flow.setCompositionId(extCompId.get(0).getElementText());
						List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
						if (Func.isNotEmpty(extField))
							flow.setCompositionField(extField.get(0).getElementText());
						flowList.add(flow);
					}
				}
			});
		}
		page.setTotal(flowList.size());
		page.setRecords(flowList);
		return page;
		//return flowList;
	}

	@Override
	public boolean completeTask(BladeFlow flow) {
		String taskId = flow.getTaskId();
		String processInstanceId = flow.getProcessInstanceId();
		String taskGroup = TaskUtil.getCandidateGroup();
//		if (taskGroup.equals("ci")){
//			iLabelTaskClient.changeStatus(processInstanceId);
//		}
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
		if (flow.getCategoryName().equals("标注流程")) {
			LabelTask labelTask = iLabelTaskClient.queryLabelTask(processInstanceId).getData();
			R<org.springblade.task.entity.Task> taskRes = iTaskClient.getById(labelTask.getTaskId());
			if (!taskRes.isSuccess()){
				log.error("获取任务信息失败！");
				return false;
			}
			org.springblade.task.entity.Task task = taskRes.getData();
			R<Kv> res = iExpertClient.isInfoComplete(labelTask.getPersonId(), labelTask.getTemplateId());
			if (!res.isSuccess()) {
				log.error("获取专家信息是否完成失败！");
				return false;
			}
			Kv kv = res.getData();
			variables.put("priority", task.getPriority());
			log.error(ProcessConstant.BASICINFO_COMPLETE_KEY+ kv.getBool(ProcessConstant.BASICINFO_COMPLETE_KEY));
			log.error(ProcessConstant.HOMEPAGE_FOUND_KEY+ kv.getBool(ProcessConstant.HOMEPAGE_FOUND_KEY));
			variables.put(ProcessConstant.BASICINFO_COMPLETE_KEY, kv.getBool(ProcessConstant.BASICINFO_COMPLETE_KEY));
			variables.put(ProcessConstant.HOMEPAGE_FOUND_KEY, kv.getBool(ProcessConstant.HOMEPAGE_FOUND_KEY));
//			variables.put("isHomepageFound", true);
			if (!kv.getBool(ProcessConstant.HOMEPAGE_FOUND_KEY)){
				flowMapper.updateStatistic(env,labelTask.getId(),2);
			}
			if (kv.getBool(ProcessConstant.BASICINFO_COMPLETE_KEY)){
				flowMapper.updateStatistic(env,labelTask.getId(), 3);
			}
			//			boolean isBiComplete = iLabelTaskClient.isBiComplete(taskId);
		}
		log.error("ProcessConstant.BASICINFO_COMPLETE_KEY:"+variables.get(ProcessConstant.BASICINFO_COMPLETE_KEY));
		log.error("ProcessConstant.HOMEPAGE_FOUND_KEY:"+variables.get(ProcessConstant.HOMEPAGE_FOUND_KEY));
		// 完成任务
		taskService.complete(taskId, variables);
		if(variables.get("priority")!=null){
			int priority = (int) variables.get("priority");
			setTaskPriorityByProcessInstanceId(processInstanceId, priority);
		}
		return true;
	}

	@Override
	public boolean changeTaskComment(BladeFlow flow) {
		String taskId = flow.getTaskId();
		String processInstanceId = flow.getProcessInstanceId();
		String taskGroup = TaskUtil.getCandidateGroup();

		String commentMsg = Func.toStr(flow.getComment(), ProcessConstant.PASS_COMMENT);
		// 修改评论
		if (StringUtil.isNoneBlank(taskId, commentMsg)) {
			List<Comment> commentList = taskService.getTaskComments(taskId);
			if (commentList.size() > 0) {
				Comment comment = commentList.get(0);
				comment.setFullMessage(commentMsg);
				taskService.saveComment(comment);
			}
		}
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
			Map<String, Object> tmp = task.getTaskLocalVariables();
			Map<String, Object> tmp1 = task.getProcessVariables();
//			Map<String, List<ExtensionElement>> extensionElements = task.getEx
//			Map<String, Object> tmp2 = task.get
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

			BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
			UserTask userTask = (UserTask)bpmnModel.getFlowElement(task.getTaskDefinitionKey());
			Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
			List<ExtensionElement> extCompId = extensionElements.get(ProcessConstant.COMPOSITION_ID);
			if (Func.isNotEmpty(extCompId))
				flow.setCompositionId(extCompId.get(0).getElementText());
			List<ExtensionElement> extCompType = extensionElements.get(ProcessConstant.COMPOSITION_TYPE);
			if (Func.isNotEmpty(extCompType))
				flow.setCompositionType(Integer.valueOf(extCompType.get(0).getElementText()));
			List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
			if (Func.isNotEmpty(extField))
				flow.setCompositionField(extField.get(0).getElementText());
			if (bladeFlow.getCategoryName().equals("标注流程")) {
				LabelTask labelTask = iLabelTaskClient.queryLabelTask(task.getProcessInstanceId()).getData();
				if (labelTask.getId() != null) {
					flow.setTemplateId(labelTask.getTemplateId());
					flow.setPersonId(labelTask.getPersonId());
					flow.setPersonName(labelTask.getPersonName());
					flow.setSubTaskId(labelTask.getId());
					flowList.add(flow);
				}
			} else if (bladeFlow.getCategoryName().equals("质检流程")) {
				QualityInspectionTask qualityInspectionTask = iQualityInspectionTaskClient.queryQualityInspectionTask(task.getProcessInstanceId()).getData();
				if (qualityInspectionTask.getId() != null) {
					flow.setTemplateId(qualityInspectionTask.getTemplateId());
					flow.setPersonId(qualityInspectionTask.getPersonId());
					flow.setPersonName(qualityInspectionTask.getPersonName());
					flow.setSubTaskId(qualityInspectionTask.getId());
					flow.setInspectionTaskId(qualityInspectionTask.getInspectionTaskId());
					flow.setLabelTaskId(qualityInspectionTask.getLabelTaskId());
					flow.setLabelProcessInstanceId(qualityInspectionTask.getLabelProcessInstanceId());
					flow.setAnnotationTaskId(qualityInspectionTask.getTaskId());
					flowList.add(flow);
				}
			}
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


	@Override
	public boolean setTaskPriorityByProcessInstanceId(String processInstanceId, int priority) {
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
		tasks.forEach(t -> {
			taskService.setPriority(t.getId(), priority);
		});
		return true;
	}

	@Override
	public boolean setTaskPriorityByProcessInstanceIds(List<String> processInstanceIds, int priority) {
		processInstanceIds.forEach(processInstanceId -> {
			List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
			tasks.forEach(t -> {
				taskService.setPriority(t.getId(), priority);
			});
		});
		return true;
	}

	@Override
	public boolean todoTimeoutHandler() {
		TaskQuery todoQuery = taskService.createTaskQuery().taskAssigneeLike("taskUser_%").active()
			.includeProcessVariables().orderByTaskCreateTime().desc();
		List<Task> taskList = todoQuery.list();
		taskList.forEach(task -> {
			Date now = new Date();
			Duration d = DateUtil.between(task.getClaimTime(), now);
			if (d.getSeconds() > 60) {
				taskService.unclaim(task.getId());
			}
		});
		return true;
	}
}
