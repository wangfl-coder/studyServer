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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
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
import org.springblade.composition.entity.Composition;
import org.springblade.composition.entity.Template;
import org.springblade.composition.feign.ICompositionClient;
import org.springblade.composition.feign.IStatisticsClient;
import org.springblade.composition.feign.ITemplateClient;
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
import org.springblade.system.entity.Role;
import org.springblade.task.entity.MergeExpertTask;
import org.springblade.task.feign.IMergeExpertTaskClient;
import org.springblade.task.vo.CompositionClaimCountVO;
import org.springblade.task.vo.CompositionClaimListVO;
import org.springblade.task.vo.ExpertLabelTaskVO;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.feign.ILabelTaskClient;
import org.springblade.task.feign.IQualityInspectionTaskClient;
import org.springblade.task.feign.ITaskClient;
import org.springblade.task.vo.ExpertQualityInspectionTaskVO;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

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
	private final ILabelTaskClient labelTaskClient;
	private final ITaskClient taskClient;
	private final IQualityInspectionTaskClient qualityInspectionTaskClient;
	private final IMergeExpertTaskClient mergeExpertTaskClient;
	private final IExpertClient expertClient;
	private final FlowMapper flowMapper;
	private final IStatisticsClient statisticsClient;
	private final RuntimeService runtimeService;
	private final ITemplateClient templateClient;
	private final ICompositionClient compositionClient;


	private final RepositoryService repositoryService;

	@Override
	public IPage<SingleFlow> selectClaimPage(IPage<SingleFlow> page, SingleFlow bladeFlow) {
		String taskUser = TaskUtil.getTaskUser();
		String taskGroup;
		List<String> labelRes = new ArrayList<>();
		List<String> inspectionRes = new ArrayList<>();
		if (null != bladeFlow.getRoleId()) {
			List<String> res = SysCache.getRoleAliases(bladeFlow.getRoleId().toString());
			if (Func.isNotEmpty(res)) {
				taskGroup = StringUtil.collectionToCommaDelimitedString(res);
			} else {
				return null;
			}
		}else if (null != bladeFlow.getCompositionId()) {
			labelRes = flowMapper.getLabelRoleAliasByCompositionId(Long.valueOf(bladeFlow.getCompositionId()));
			inspectionRes = flowMapper.getInspectionRoleAliasByCompositionId(Long.valueOf(bladeFlow.getCompositionId()));
			String roles[] = AuthUtil.getUserRole().split(",");
			labelRes.retainAll(Arrays.asList(roles));
			inspectionRes.retainAll(Arrays.asList(roles));
			labelRes.addAll(inspectionRes);
			if (Func.isNotEmpty(labelRes)) {
				taskGroup = StringUtil.collectionToCommaDelimitedString(labelRes);
			} else {
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
			.includeProcessVariables().active().orderByTaskPriority().desc().orderByTaskCreateTime().desc();
		// 通用流程等待签收的任务
		TaskQuery claimRoleWithoutTenantIdQuery = taskService.createTaskQuery().taskWithoutTenantId().taskCandidateGroupIn(Func.toStrList(taskGroup))
			.includeProcessVariables().active().orderByTaskPriority().desc().orderByTaskCreateTime().desc();

		// 构建列表数据
		buildFlowTaskList(bladeFlow, flowList, claimUserQuery, page, FlowEngineConstant.STATUS_CLAIM);
//		buildFlowTaskList(bladeFlow, flowList, claimRoleWithTenantIdQuery, page, FlowEngineConstant.STATUS_CLAIM);
//		buildFlowTaskList(bladeFlow, flowList, claimRoleWithoutTenantIdQuery, page, FlowEngineConstant.STATUS_CLAIM);
		buildClaimPageList(bladeFlow, flowList, claimRoleWithTenantIdQuery, page, FlowEngineConstant.STATUS_CLAIM);
		buildClaimPageList(bladeFlow, flowList, claimRoleWithoutTenantIdQuery, page, FlowEngineConstant.STATUS_CLAIM);


		// 计算总数
//		long count = claimRoleWithoutTenantIdQuery.count();
		int count = flowList.size();
		page.setTotal(count);

		// 设置数据
		page.setRecords(flowList);

		return page;
	}

	@Override
	public SingleFlow selectOneClaimPage(String categoryName, Long roleId, Long compositionId) {
//		String taskUser = TaskUtil.getTaskUser();
		String taskGroup;
		List<String> labelRes = new ArrayList<>();
		List<String> inspectionRes = new ArrayList<>();
		if (null != roleId) {
			List<String> res = SysCache.getRoleAliases(roleId.toString());
			if (Func.isNotEmpty(res)) {
				taskGroup = StringUtil.collectionToCommaDelimitedString(res);
			}else {
				return null;
			}
		}else if (null != compositionId) {
			labelRes = flowMapper.getLabelRoleAliasByCompositionId(compositionId);
			inspectionRes = flowMapper.getInspectionRoleAliasByCompositionId(compositionId);
			String roles[] = AuthUtil.getUserRole().split(",");
			labelRes.retainAll(Arrays.asList(roles));
			inspectionRes.retainAll(Arrays.asList(roles));
			labelRes.addAll(inspectionRes);
			if (Func.isNotEmpty(labelRes)) {
				taskGroup = StringUtil.collectionToCommaDelimitedString(labelRes);
			} else {
				return null;
			}
		}else {
			taskGroup = TaskUtil.getCandidateGroup();
		}

		// .includeProcessVariables()有几率取不到一部分任务 flowable6.4.2
//		TaskQuery claimRoleWithTenantIdQuery = taskService.createTaskQuery().taskTenantId(AuthUtil.getTenantId()).taskCandidateGroupIn(Func.toStrList(taskGroup))
//			/*.includeProcessVariables()*/.active().orderByTaskPriority().desc().orderByTaskCreateTime().desc();
//		TaskQuery claimRoleWithoutTenantIdQuery = taskService.createTaskQuery().taskWithoutTenantId().taskCandidateGroupIn(Func.toStrList(taskGroup))
//			/*.includeProcessVariables()*/.active().orderByTaskPriority().desc().orderByTaskCreateTime().desc();
//
//		SingleFlow singleFlow = claimOne(claimRoleWithTenantIdQuery, FlowEngineConstant.STATUS_CLAIM, categoryName, compositionId);
//		if (singleFlow == null) {
//			singleFlow = claimOne(claimRoleWithoutTenantIdQuery, FlowEngineConstant.STATUS_CLAIM, categoryName, compositionId);
//		}
		String taskId = claimOneByCompositionId(Func.toStrList(taskGroup), AuthUtil.getUserId(), compositionId);
		TaskQuery claimQuery = taskService.createTaskQuery().taskId(taskId);
		SingleFlow singleFlow = claimOne2(claimQuery, FlowEngineConstant.STATUS_CLAIM, categoryName, compositionId);
		if (singleFlow == null) {
			singleFlow = new SingleFlow();
		}
		return singleFlow;
	}
;
	@Override
	public IPage<SingleFlow> selectTodoPage(IPage<SingleFlow> page, BladeFlow bladeFlow) {
		String taskUser = TaskUtil.getTaskUser();
		List<SingleFlow> flowList = new LinkedList<>();

		// 已签收的任务
		TaskQuery todoQuery = taskService.createTaskQuery().taskAssignee(taskUser).active()
			.includeProcessVariables().orderByTaskCreateTime().desc();

		// 构建列表数据
		buildFlowTaskList(bladeFlow, flowList, todoQuery, page, FlowEngineConstant.STATUS_TODO);

		for(SingleFlow flow: flowList) {
			if (null != flow.getCompositionId()) {
				Role role = flowMapper.getRoleByTemplateComposition(flow.getTemplateId(), Long.valueOf(flow.getCompositionId()));
				flow.setRoleId(role.getId());
			}
		}
		// 计算总数
		long count = todoQuery.count();
//		// 设置页数
//		page.setSize(count);
		// 设置总数
		Integer total = (Integer)labelTaskClient.queryLabelTaskTodoCount(taskUser).getData();
		if (total!=null) {
			if (bladeFlow.getCategoryName().equals("标注流程")) {
				page.setTotal(total);
			} else if (bladeFlow.getCategoryName().equals("质检流程")) {
				page.setTotal(count - total);
			}
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
			R<org.springblade.task.entity.Task> taskRes = taskClient.getById(Long.valueOf(bladeFlow.getTaskId()));
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
					flow.setCompositionId(Long.valueOf(extCompId.get(0).getElementText()));
				List<ExtensionElement> extCompType = extensionElements.get(ProcessConstant.COMPOSITION_TYPE);
				if (Func.isNotEmpty(extCompType))
					flow.setCompositionType(Integer.valueOf(extCompType.get(0).getElementText()));
				List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
				if (Func.isNotEmpty(extField))
					flow.setCompositionField(extField.get(0).getElementText());
				List<ExtensionElement> inspectionType = extensionElements.get(ProcessConstant.INSPECTION_TYPE);
				if (Func.isNotEmpty(inspectionType))
					flow.setInspectionType(Integer.valueOf(inspectionType.get(0).getElementText()));
			}
			// Status
			if (historicProcessInstance.getEndActivityId() != null) {
				flow.setProcessIsFinished(FlowEngineConstant.STATUS_FINISHED);
			} else {
				flow.setProcessIsFinished(FlowEngineConstant.STATUS_UNFINISHED);
			}
			flow.setStatus(FlowEngineConstant.STATUS_FINISH);

			if ("标注流程".equals(bladeFlow.getCategoryName())) {
				LabelTask labelTask = labelTaskClient.queryLabelTask(historicProcessInstance.getId()).getData();
				if(labelTask.getId() != null) {
					flow.setTemplateId(labelTask.getTemplateId());
					flow.setPersonId(labelTask.getPersonId());
					flow.setPersonName(labelTask.getPersonName());
					flow.setSubTaskId(labelTask.getId());
					flowList.add(flow);
				}
			} else if ("质检流程".equals(bladeFlow.getCategoryName())) {
				QualityInspectionTask qualityInspectionTask = qualityInspectionTaskClient.queryQualityInspectionTask(historicProcessInstance.getId()).getData();
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
			} else if ("合并专家流程".equals(bladeFlow.getCategoryName())) {
				MergeExpertTask mergeExpertTask = mergeExpertTaskClient.queryMergeExpertTask(historicProcessInstance.getId()).getData();
				if(mergeExpertTask.getId() != null) {
					flow.setPersonId(mergeExpertTask.getPersonId());
					flow.setPersonName(mergeExpertTask.getPersonName());
					flow.setSubTaskId(mergeExpertTask.getId());
					flow.setMergeExpertTaskId(mergeExpertTask.getMergeTaskId());
					flow.setLabelTaskId(mergeExpertTask.getLabelTaskId());
					flow.setAnnotationTaskId(mergeExpertTask.getTaskId());
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
			// 设置数据
//			page.setRecords(flowList);
			int start = Func.toInt((page.getCurrent() - 1) * page.getSize());
			long end = (start + page.getSize()) > flowList.size() ? flowList.size() : (start + page.getSize());
			page.setRecords(flowList.subList(start, (int)end));
		}
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
				flow.setCompositionId(Long.valueOf(extCompId.get(0).getElementText()));
			List<ExtensionElement> extCompType = extensionElements.get(ProcessConstant.COMPOSITION_TYPE);
			if (Func.isNotEmpty(extCompType))
				flow.setCompositionType(Integer.valueOf(extCompType.get(0).getElementText()));
			List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
			if (Func.isNotEmpty(extField))
				flow.setCompositionField(extField.get(0).getElementText());
			List<ExtensionElement> inspectionType = extensionElements.get(ProcessConstant.INSPECTION_TYPE);
			if (Func.isNotEmpty(inspectionType))
				flow.setInspectionType(Integer.valueOf(inspectionType.get(0).getElementText()));

			if ("标注流程".equals(bladeFlow.getCategoryName())){
				LabelTask labelTask = labelTaskClient.queryLabelTask(historicTaskInstance.getProcessInstanceId()).getData();
				if (labelTask.getId() != null) {
					flow.setTemplateId(labelTask.getTemplateId());
					flow.setPersonId(labelTask.getPersonId());
					flow.setPersonName(labelTask.getPersonName());
					flow.setSubTaskId(labelTask.getId());
					flowList.add(flow);
				}
			} else if ("质检流程".equals(bladeFlow.getCategoryName())){
				QualityInspectionTask qualityInspectionTask = qualityInspectionTaskClient.queryQualityInspectionTask(historicTaskInstance.getProcessInstanceId()).getData();
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
			} else if ("合并专家流程".equals(bladeFlow.getCategoryName())) {
				MergeExpertTask mergeExpertTask = mergeExpertTaskClient.queryMergeExpertTask(historicProcessInstance.getId()).getData();
				if(mergeExpertTask.getId() != null) {
					flow.setPersonId(mergeExpertTask.getPersonId());
					flow.setPersonName(mergeExpertTask.getPersonName());
					flow.setSubTaskId(mergeExpertTask.getId());
					flow.setMergeExpertTaskId(mergeExpertTask.getMergeTaskId());
					flow.setLabelTaskId(mergeExpertTask.getLabelTaskId());
					flow.setAnnotationTaskId(mergeExpertTask.getTaskId());
					flowList.add(flow);
				}
			}

		});
		// 计算总数
		long count = doneQuery.count();
		// 设置总数
		Integer total = (Integer)labelTaskClient.queryLabelTaskDoneCount(taskUser).getData();
		if("标注流程".equals(bladeFlow.getCategoryName())){
			page.setTotal(total);
		} else if("质检流程".equals(bladeFlow.getCategoryName())){
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
				List<LabelTask> labelTasks = labelTaskClient.queryLabelTaskByPersonId(bladeFlow.getPersonId()).getData();
				for(LabelTask labelTask:labelTasks){
					ExpertLabelTaskVO expertLabelTaskVO = Objects.requireNonNull(BeanUtil.copy(labelTask, ExpertLabelTaskVO.class));
					expertLabelTaskVOS.add(expertLabelTaskVO);
				}
			}else if(bladeFlow.getExpertId() != null && !bladeFlow.getExpertId().isEmpty()){
				expertLabelTaskVOS = labelTaskClient.queryLabelTaskByExpertId(bladeFlow.getExpertId()).getData();
			}else {
				return selectDonePage(page, bladeFlow);
			}
			expertLabelTaskVOS.forEach(expertProcessInstanceVO -> {
				if (expertProcessInstanceVO.getId() != null) {
					HistoricTaskInstanceQuery doneQuery = historyService.createHistoricTaskInstanceQuery().taskAssignee(taskUser).finished()
						.includeProcessVariables().processInstanceId(expertProcessInstanceVO.getProcessInstanceId());
					if (bladeFlow.getCategory() != null) {
						doneQuery.processCategoryIn(Func.toStrList(bladeFlow.getCategory()));
					}
					if (bladeFlow.getBeginDate() != null) {
						doneQuery.taskCompletedAfter(bladeFlow.getBeginDate());
					}
					if (bladeFlow.getEndDate() != null) {
						doneQuery.taskCompletedBefore(bladeFlow.getEndDate());
					}
					List<HistoricTaskInstance> doneList = doneQuery.listPage(Func.toInt((page.getCurrent() - 1) * page.getSize()), Func.toInt(page.getSize()));
					doneList.forEach(historicTaskInstance -> {
						SingleFlow flow = new SingleFlow();
						flow.setTemplateId(expertProcessInstanceVO.getTemplateId());
						flow.setPersonId(expertProcessInstanceVO.getPersonId());
						flow.setPersonName(expertProcessInstanceVO.getPersonName());
						flow.setSubTaskId(expertProcessInstanceVO.getId());
						flow.setProcessInstanceId(expertProcessInstanceVO.getProcessInstanceId());
						if(bladeFlow.getExpertId()!=null){
							flow.setExpertId(bladeFlow.getExpertId());
						}
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
							flow.setCompositionId(Long.valueOf(extCompId.get(0).getElementText()));
						List<ExtensionElement> extCompType = extensionElements.get(ProcessConstant.COMPOSITION_TYPE);
						if (Func.isNotEmpty(extCompType))
							flow.setCompositionType(Integer.valueOf(extCompType.get(0).getElementText()));
						List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
						if (Func.isNotEmpty(extField))
							flow.setCompositionField(extField.get(0).getElementText());
						List<ExtensionElement> inspectionType = extensionElements.get(ProcessConstant.INSPECTION_TYPE);
						if (Func.isNotEmpty(inspectionType))
							flow.setInspectionType(Integer.valueOf(inspectionType.get(0).getElementText()));
						flowList.add(flow);
					});
				}
			});
		} else if (bladeFlow.getCategoryName().equals("质检流程")){
			if(bladeFlow.getPersonId()!=null) {
				List<QualityInspectionTask> qualityInspectionTasks = qualityInspectionTaskClient.queryQualityInspectionTaskByPersonId(bladeFlow.getPersonId()).getData();
				for(QualityInspectionTask qualityInspectionTask:qualityInspectionTasks){
					ExpertQualityInspectionTaskVO expertQualityInspectionTaskVO = Objects.requireNonNull(BeanUtil.copy(qualityInspectionTask, ExpertQualityInspectionTaskVO.class));
					expertQualityInspectionTaskVOS.add(expertQualityInspectionTaskVO);
				}
			}else if(bladeFlow.getExpertId()!=null && !bladeFlow.getExpertId().isEmpty()){
				expertQualityInspectionTaskVOS = qualityInspectionTaskClient.queryQualityInspectionTaskByExpertId(bladeFlow.getExpertId()).getData();
			}else{
				return selectDonePage(page,bladeFlow);
			}
			expertQualityInspectionTaskVOS.forEach(expertProcessInstanceVO -> {
				if (expertProcessInstanceVO.getId() != null) {
					HistoricTaskInstanceQuery doneQuery = historyService.createHistoricTaskInstanceQuery().taskAssignee(taskUser).finished()
						.includeProcessVariables().processInstanceId(expertProcessInstanceVO.getProcessInstanceId());
					if (bladeFlow.getCategory() != null) {
						doneQuery.processCategoryIn(Func.toStrList(bladeFlow.getCategory()));
					}
					if (bladeFlow.getBeginDate() != null) {
						doneQuery.taskCompletedAfter(bladeFlow.getBeginDate());
					}
					if (bladeFlow.getEndDate() != null) {
						doneQuery.taskCompletedBefore(bladeFlow.getEndDate());
					}
					List<HistoricTaskInstance> doneList = doneQuery.listPage(Func.toInt((page.getCurrent() - 1) * page.getSize()), Func.toInt(page.getSize()));
					doneList.forEach(historicTaskInstance -> {
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
							flow.setCompositionId(Long.valueOf(extCompId.get(0).getElementText()));
						List<ExtensionElement> extCompType = extensionElements.get(ProcessConstant.COMPOSITION_TYPE);
						if (Func.isNotEmpty(extCompType))
							flow.setCompositionType(Integer.valueOf(extCompType.get(0).getElementText()));
						List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
						if (Func.isNotEmpty(extField))
							flow.setCompositionField(extField.get(0).getElementText());
						List<ExtensionElement> inspectionType = extensionElements.get(ProcessConstant.INSPECTION_TYPE);
						if (Func.isNotEmpty(inspectionType))
							flow.setInspectionType(Integer.valueOf(inspectionType.get(0).getElementText()));
						flowList.add(flow);
					});
				}
			});
		}
		page.setTotal(flowList.size());
		page.setRecords(flowList);
		return page;
		//return flowList;
	}

	@Override
	public boolean completeTask(SingleFlow flow) {
		String taskId = flow.getTaskId();
		String processInstanceId = flow.getProcessInstanceId();
		Map<String, Object> processVariables = runtimeService.getVariables(processInstanceId);
		Map<String, Object> taskVariables = taskService.getVariables(taskId);

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
			LabelTask labelTask = labelTaskClient.queryLabelTask(processInstanceId).getData();
			R<org.springblade.task.entity.Task> taskRes = taskClient.getById(labelTask.getTaskId());
			R<Composition> compositionRes = compositionClient.getById(Long.valueOf(flow.getCompositionId()));
			if (!taskRes.isSuccess() || !compositionRes.isSuccess()){
				log.error("获取任务信息失败！");
				return false;
			}
			org.springblade.task.entity.Task task = taskRes.getData();
			Composition currentComposition = compositionRes.getData();
			R<Kv> res = expertClient.isInfoComplete(labelTask.getPersonId(), labelTask.getTemplateId());
			if (!res.isSuccess()) {
				log.error("获取专家信息是否完成失败！");
				return false;
			}
			Boolean hasHomepage = (Boolean)processVariables.get("hasHomepage");
			if (null != flow.getCompositionType() && 1 == flow.getCompositionType() && hasHomepage) {
				Random random = Holder.RANDOM;
				boolean insertRealSet = random.nextInt(100) < task.getRealSetRate() ? true : false;
				if (insertRealSet && res.getData().getBool(ProcessConstant.HOMEPAGE_FOUND_KEY)) {
					R<Template> templateRes = templateClient.getTemplateById(task.getTemplateId());
					if (templateRes.isSuccess()) {
						Template template = templateRes.getData();
						R<Map<String, String>> startRes = labelTaskClient.startRealSetProcess(template.getRealSetProcessDefinitions(), task);
						if (startRes.isSuccess() && startRes.getData() != null) {
							Map<String, String> compositionLabelMap = startRes.getData();
							statisticsClient.initializeRealSetLabelTask(labelTask, compositionLabelMap);
						}
					}
				}
			}
			if (null != flow.getCompositionType() && 3 == flow.getCompositionType() && hasHomepage) {	//如果需要找主页而前面都没找到，补充信息提交后将前面的基本信息统计删除
				statisticsClient.ifNeedToRemoveBasicInfoStatistics(labelTask.getId(), labelTask.getTemplateId(),Long.valueOf(flow.getCompositionId()));
			}
			if (null != flow.getCompositionType() && 2 == flow.getCompositionType()) {
				R<List<Composition>> compositionsRes = templateClient.allCompositions(labelTask.getTemplateId());
				if (compositionsRes.isSuccess()) {
					List<Composition> compositions = compositionsRes.getData();
					for(Composition composition : compositions) {
						if (2 == composition.getAnnotationType()) {
							variables.put("biCounter"+composition.getId(), 0);
							variables.put("biSame"+composition.getId(), 0);
							variables.put("biNotfound"+composition.getId(), 0);
						}
					}
				}
				variables.put("isEduWorkEasy", false);
				variables.put("isEduWorkNeedInspect", false);
				variables.put("isBioNeedInspect", false);

				R<Kv> basicInfoStatusRes = statisticsClient.queryBasicInfoStatus(labelTask.getId(), labelTask.getTemplateId(),Long.valueOf(flow.getCompositionId()));
//				R<Kv> res2 = statisticsClient.queryBasicInfoStatus(labelTask.getId(), labelTask.getTemplateId(), 1348915419956248578L);
				if (!basicInfoStatusRes.isSuccess()) {
					log.error("查询基本信息状态失败！");
					return false;
				}
				Kv basicInfoDict = basicInfoStatusRes.getData();
				variables.put("biCounter"+flow.getCompositionId(), basicInfoDict.get("biCounter"));
				variables.put("biSame"+flow.getCompositionId(), basicInfoDict.get("biSame"));
				variables.put("biNotfound"+flow.getCompositionId(), basicInfoDict.get("biNotfound"));
			}

			if (4 == flow.getCompositionType()) {
				final Random random = Holder.RANDOM;
				boolean isEduWorkNeedInspect = random.nextInt(100) < task.getEduWorkInspectPercent() ? true : false;
				variables.put("isEduWorkNeedInspect", isEduWorkNeedInspect);
				if (isEduWorkNeedInspect) {
					statisticsClient.initializeSingleCompositionTask(3, labelTask.getId(), labelTask.getTemplateId(), Long.valueOf(flow.getCompositionId()));
				}
			}
			if (5 == flow.getCompositionType()) {
				final Random random = Holder.RANDOM;
				boolean isBioNeedInspect = random.nextInt(100) < task.getBioInspectPercent() ? true : false;
				variables.put("isBioNeedInspect", isBioNeedInspect);
				if (isBioNeedInspect) {
					statisticsClient.initializeSingleCompositionTask(3, labelTask.getId(), labelTask.getTemplateId(), Long.valueOf(flow.getCompositionId()));
				}
			}

			Kv kv = res.getData();
			variables.put("priority", task.getPriority());
			log.error(ProcessConstant.BASICINFO_COMPLETE_KEY+ kv.getBool(ProcessConstant.BASICINFO_COMPLETE_KEY));
			log.error(ProcessConstant.HOMEPAGE_FOUND_KEY+ kv.getBool(ProcessConstant.HOMEPAGE_FOUND_KEY));
			variables.put(ProcessConstant.BASICINFO_COMPLETE_KEY, kv.getBool(ProcessConstant.BASICINFO_COMPLETE_KEY));
			variables.put(ProcessConstant.HOMEPAGE_FOUND_KEY, kv.getBool(ProcessConstant.HOMEPAGE_FOUND_KEY));
			variables.put("isHpComplete", false);
//			if (!kv.getBool(ProcessConstant.HOMEPAGE_FOUND_KEY)){
//				flowMapper.updateStatistic(env,labelTask.getId(),2);
//			}
//			if (kv.getBool(ProcessConstant.BASICINFO_COMPLETE_KEY)){
//				flowMapper.updateStatistic(env,labelTask.getId(), 3);
//			}
			if(null!=flow.getRoleId() && -1!=flow.getRoleId()) {
				//ws0204
//				String roleAlias = SysCache.getRoleAlias(flow.getRoleId());
//				processVariables.put(roleAlias + "-" + AuthUtil.getUserId(), true);
//				taskVariables.put(roleAlias + "-" + AuthUtil.getUserId(), true);

				processVariables.put(currentComposition.getName()+"-"+AuthUtil.getUserId()+"-done", true);
			}
			taskService.setVariables(taskId, taskVariables);
			runtimeService.setVariables(processInstanceId, processVariables);
			//			boolean isBiComplete = labelTaskClient.isBiComplete(taskId);
			log.error("ProcessConstant.BASICINFO_COMPLETE_KEY:"+variables.get(ProcessConstant.BASICINFO_COMPLETE_KEY));
			log.error("ProcessConstant.HOMEPAGE_FOUND_KEY:"+variables.get(ProcessConstant.HOMEPAGE_FOUND_KEY));
			// 完成任务
			taskService.complete(taskId, variables);
//			statisticsClient.markAsComplete(1, labelTask.getId(), currentComposition.getId());
		} else {  //质检任务
			taskService.complete(taskId, variables);
		}


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
			if (extCompId != null) {
				Long extCompIdNum = Long.valueOf(extCompId.get(0).getElementText());
				if (extCompIdNum.longValue() == -1 && !AuthUtil.getTenantId().equals("000000")) {	//为老模版建的老任务，老数据兼容
					R<Long> res = compositionClient.getComplementCompositionId(AuthUtil.getTenantId());
					if (res.isSuccess()){
						flow.setCompositionId(res.getData());
					}
				} else {
					flow.setCompositionId(extCompIdNum);
				}
			}
			List<ExtensionElement> extCompType = extensionElements.get(ProcessConstant.COMPOSITION_TYPE);
			if (Func.isNotEmpty(extCompType))
				flow.setCompositionType(Integer.valueOf(extCompType.get(0).getElementText()));
			List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
			if (Func.isNotEmpty(extField))
				flow.setCompositionField(extField.get(0).getElementText());
			List<ExtensionElement> inspectionType = extensionElements.get(ProcessConstant.INSPECTION_TYPE);
			if (Func.isNotEmpty(inspectionType))
				flow.setInspectionType(Integer.valueOf(inspectionType.get(0).getElementText()));
			if ("标注流程".equals(bladeFlow.getCategoryName())) {
				LabelTask labelTask = labelTaskClient.queryLabelTask(task.getProcessInstanceId()).getData();
				if (labelTask!=null && labelTask.getId() != null) {
					flow.setTemplateId(labelTask.getTemplateId());
					flow.setPersonId(labelTask.getPersonId());
					flow.setPersonName(labelTask.getPersonName());
					flow.setSubTaskId(labelTask.getId());
					flowList.add(flow);
				}
			} else if ("质检流程".equals(bladeFlow.getCategoryName())) {
				QualityInspectionTask qualityInspectionTask = qualityInspectionTaskClient.queryQualityInspectionTask(task.getProcessInstanceId()).getData();
				if (qualityInspectionTask!=null && qualityInspectionTask.getId() != null) {
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
			} else if ("合并专家流程".equals(bladeFlow.getCategoryName())) {
				MergeExpertTask mergeExpertTask = mergeExpertTaskClient.queryMergeExpertTask(historicProcessInstance.getId()).getData();
				if(mergeExpertTask.getId() != null) {
					flow.setPersonId(mergeExpertTask.getPersonId());
					flow.setPersonName(mergeExpertTask.getPersonName());
					flow.setSubTaskId(mergeExpertTask.getId());
					flow.setMergeExpertTaskId(mergeExpertTask.getMergeTaskId());
					flow.setLabelTaskId(mergeExpertTask.getLabelTaskId());
					flow.setAnnotationTaskId(mergeExpertTask.getTaskId());
					flowList.add(flow);
				}
			}
		});
	}

	/**
	 * 构建流程
	 *
	 * @param bladeFlow 流程通用类
	 * @param flowList  流程列表
	 * @param taskQuery 任务查询类
	 * @param status    状态
	 */
	private void buildClaimPageList(BladeFlow bladeFlow, List<SingleFlow> flowList, TaskQuery taskQuery, IPage<SingleFlow> page, String status) {
		if (bladeFlow.getCategory() != null) {
			taskQuery.processCategoryIn(Func.toStrList(bladeFlow.getCategory()));
		}
		if (bladeFlow.getBeginDate() != null) {
			taskQuery.taskCreatedAfter(bladeFlow.getBeginDate());
		}
		if (bladeFlow.getEndDate() != null) {
			taskQuery.taskCreatedBefore(bladeFlow.getEndDate());
		}

		List<String> roles = Arrays.asList(AuthUtil.getUserRole().split(","));
		HashMap<Long, Integer> userRoleCountMap = new HashMap<>();
//		List<Task> taskList = taskQuery.listPage(Func.toInt((page.getCurrent() - 1) * page.getSize()), Func.toInt(page.getSize()));
		List<Task> taskList = taskQuery.list();
//		taskList.forEach(task -> {
		for(Task task: taskList) {
			Map<String, Object> tmp = task.getTaskLocalVariables();
			Map<String, Object> tmp1 = task.getProcessVariables();

			Map<String, Object> processVariables = task.getProcessVariables();
			if (processVariables.containsKey(task.getName()+"-"+AuthUtil.getUserId()+"-done")) {
				continue;
			}

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
			flow.setStatus(FlowEngineConstant.STATUS_CLAIM);

			BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
			UserTask userTask = (UserTask) bpmnModel.getFlowElement(task.getTaskDefinitionKey());
			Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
			List<ExtensionElement> extCompId = extensionElements.get(ProcessConstant.COMPOSITION_ID);
			if (Func.isNotEmpty(extCompId))
				flow.setCompositionId(Long.valueOf(extCompId.get(0).getElementText()));
			List<ExtensionElement> extCompType = extensionElements.get(ProcessConstant.COMPOSITION_TYPE);
			if (Func.isNotEmpty(extCompType))
				flow.setCompositionType(Integer.valueOf(extCompType.get(0).getElementText()));
			List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
			if (Func.isNotEmpty(extField))
				flow.setCompositionField(extField.get(0).getElementText());
			List<ExtensionElement> inspectionType = extensionElements.get(ProcessConstant.INSPECTION_TYPE);
			if (Func.isNotEmpty(inspectionType))
				flow.setInspectionType(Integer.valueOf(inspectionType.get(0).getElementText()));
			if ("标注流程".equals(bladeFlow.getCategoryName())) {
				LabelTask labelTask = labelTaskClient.queryLabelTask(task.getProcessInstanceId()).getData();
				if (labelTask != null && labelTask.getId() != null) {
					flow.setTemplateId(labelTask.getTemplateId());
					flow.setPersonId(labelTask.getPersonId());
					flow.setPersonName(labelTask.getPersonName());
					flow.setSubTaskId(labelTask.getId());
					flowList.add(flow);
				}
			} else if ("质检流程".equals(bladeFlow.getCategoryName())) {
				QualityInspectionTask qualityInspectionTask = qualityInspectionTaskClient.queryQualityInspectionTask(task.getProcessInstanceId()).getData();
				if (qualityInspectionTask != null && qualityInspectionTask.getId() != null) {
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
			} else if ("合并专家流程".equals(bladeFlow.getCategoryName())) {
				MergeExpertTask mergeExpertTask = mergeExpertTaskClient.queryMergeExpertTask(historicProcessInstance.getId()).getData();
				if(mergeExpertTask.getId() != null) {
					flow.setPersonId(mergeExpertTask.getPersonId());
					flow.setPersonName(mergeExpertTask.getPersonName());
					flow.setSubTaskId(mergeExpertTask.getId());
					flow.setMergeExpertTaskId(mergeExpertTask.getMergeTaskId());
					flow.setLabelTaskId(mergeExpertTask.getLabelTaskId());
					flow.setAnnotationTaskId(mergeExpertTask.getTaskId());
					flowList.add(flow);
				}
			}
//			if (flowList.size() > 19)
//				break;
//		});
		}
	}

	/**
	 * 构建流程
	 *
	 * @param taskQuery 任务查询类
	 * @param status    状态
	 */
	private SingleFlow claimOne(TaskQuery taskQuery, String status, String categoryName, Long compositionId) {
		List<Task> taskList = taskQuery.list();
		for (Task task : taskList) {
			SingleFlow flow = new SingleFlow();
			BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
			UserTask userTask = (UserTask)bpmnModel.getFlowElement(task.getTaskDefinitionKey());
			Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
			List<ExtensionElement> extCompId = extensionElements.get(ProcessConstant.COMPOSITION_ID);
			if (!Func.hasEmpty(compositionId, extCompId)) {
				Long extCompIdNum = Long.valueOf(extCompId.get(0).getElementText());
				boolean t1 = extCompIdNum!=null;
				boolean t2 = extCompIdNum.longValue() == -1;
				boolean t3 = !AuthUtil.getTenantId().equals("000000");
				if (extCompIdNum!=null && extCompIdNum.longValue() == -1 && !AuthUtil.getTenantId().equals("000000")) {	//为老模版建的老任务，老数据兼容
					flow.setCompositionId(compositionId);
				}else {
					if (!compositionId.equals(extCompIdNum))
						continue;
					flow.setCompositionId(Long.valueOf(extCompId.get(0).getElementText()));
				}
			}
//			Map<String, Object> processVariables = task.getProcessVariables();	// .includeProcessVariables()有几率取不到一部分任务 flowable6.4.2
			Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
			if (processVariables.containsKey(task.getName()+"-"+AuthUtil.getUserId()+"-done")) {
				continue;
			}
			List<ExtensionElement> extCompType = extensionElements.get(ProcessConstant.COMPOSITION_TYPE);
			if (Func.isNotEmpty(extCompType))
				flow.setCompositionType(Integer.valueOf(extCompType.get(0).getElementText()));
			List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
			if (Func.isNotEmpty(extField))
				flow.setCompositionField(extField.get(0).getElementText());
			List<ExtensionElement> inspectionType = extensionElements.get(ProcessConstant.INSPECTION_TYPE);
			if (Func.isNotEmpty(inspectionType))
				flow.setInspectionType(Integer.valueOf(inspectionType.get(0).getElementText()));

			flow.setTaskId(task.getId());
			flow.setTaskDefinitionKey(task.getTaskDefinitionKey());
			flow.setTaskName(task.getName());
			flow.setAssignee(task.getAssignee());
			flow.setCreateTime(task.getCreateTime());
			flow.setClaimTime(task.getClaimTime());
			flow.setExecutionId(task.getExecutionId());
			flow.setVariables(processVariables);
			flow.setPriority(task.getPriority());
			ProcessDefinition processDefinition = FlowCache.getProcessDefinition(task.getProcessDefinitionId());
			flow.setCategory(processDefinition.getCategory());
			flow.setCategoryName(FlowCache.getCategoryName(processDefinition.getCategory()));
			flow.setProcessDefinitionId(processDefinition.getId());
			flow.setProcessDefinitionName(processDefinition.getName());
			flow.setProcessDefinitionKey(processDefinition.getKey());
			flow.setProcessDefinitionVersion(processDefinition.getVersion());
			flow.setProcessInstanceId(task.getProcessInstanceId());


			if ("标注流程".equals(categoryName)) {
				LabelTask labelTask = labelTaskClient.queryLabelTask(task.getProcessInstanceId()).getData();
//					log.error("processInstanceId:"+task.getProcessInstanceId());
//					log.error("taskId:"+task.getId());
				if(labelTask.getId()!=null){
					flow.setTemplateId(labelTask.getTemplateId());
					flow.setPersonId(labelTask.getPersonId());
					flow.setPersonName(labelTask.getPersonName());
					flow.setSubTaskId(labelTask.getId());
					if (null != flow.getCompositionId()) {
						Role role = flowMapper.getRoleByTemplateComposition(flow.getTemplateId(), Long.valueOf(flow.getCompositionId()));
						flow.setRoleId(role.getId());
					}
					return flow;
				}
			} else if ("质检流程".equals(categoryName)) {
				QualityInspectionTask qualityInspectionTask = qualityInspectionTaskClient.queryQualityInspectionTask(task.getProcessInstanceId()).getData();
				if(qualityInspectionTask.getId()!=null){
					flow.setTemplateId(qualityInspectionTask.getTemplateId());
					flow.setPersonId(qualityInspectionTask.getPersonId());
					flow.setPersonName(qualityInspectionTask.getPersonName());
					flow.setSubTaskId(qualityInspectionTask.getId());
					flow.setInspectionTaskId(qualityInspectionTask.getInspectionTaskId());
					flow.setLabelTaskId(qualityInspectionTask.getLabelTaskId());
					flow.setAnnotationTaskId(qualityInspectionTask.getTaskId());
					return flow;
				}
			}else if ("合并专家流程".equals(categoryName)) {
				MergeExpertTask mergeExpertTask = mergeExpertTaskClient.queryMergeExpertTask(task.getProcessInstanceId()).getData();
				if (mergeExpertTask.getId() != null) {
					flow.setPersonId(mergeExpertTask.getPersonId());
					flow.setPersonName(mergeExpertTask.getPersonName());
					flow.setSubTaskId(mergeExpertTask.getId());
					flow.setMergeExpertTaskId(mergeExpertTask.getMergeTaskId());
					flow.setLabelTaskId(mergeExpertTask.getLabelTaskId());
					flow.setAnnotationTaskId(mergeExpertTask.getTaskId());
					return flow;
				}
			}
		}
		return null;
	}

	/**
	 * 构建流程
	 *
	 * @param taskQuery 任务查询类
	 * @param status    状态
	 */
	private SingleFlow claimOne2(TaskQuery taskQuery, String status, String categoryName, Long compositionId) {
		List<Task> taskList = taskQuery.list();
		for (Task task : taskList) {
			SingleFlow flow = new SingleFlow();
			BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
			UserTask userTask = (UserTask)bpmnModel.getFlowElement(task.getTaskDefinitionKey());
			Map<String, List<ExtensionElement>> extensionElements = userTask.getExtensionElements();
			List<ExtensionElement> extCompId = extensionElements.get(ProcessConstant.COMPOSITION_ID);
//			if (!Func.hasEmpty(compositionId, extCompId)) {
//				Long extCompIdNum = Long.valueOf(extCompId.get(0).getElementText());
//				boolean t1 = extCompIdNum!=null;
//				boolean t2 = extCompIdNum.longValue() == -1;
//				boolean t3 = !AuthUtil.getTenantId().equals("000000");
//				if (extCompIdNum!=null && extCompIdNum.longValue() == -1 && !AuthUtil.getTenantId().equals("000000")) {	//为老模版建的老任务，老数据兼容
//					flow.setCompositionId(compositionId);
//				}else {
//					if (!compositionId.equals(extCompIdNum))
//						continue;
//					flow.setCompositionId(Long.valueOf(extCompId.get(0).getElementText()));
//				}
//			}
			flow.setCompositionId(Long.valueOf(extCompId.get(0).getElementText()));
//			Map<String, Object> processVariables = task.getProcessVariables();	// .includeProcessVariables()有几率取不到一部分任务 flowable6.4.2
			Map<String, Object> processVariables = runtimeService.getVariables(task.getProcessInstanceId());
//			if (processVariables.containsKey(task.getName()+"-"+AuthUtil.getUserId()+"-done")) {
//				continue;
//			}
			List<ExtensionElement> extCompType = extensionElements.get(ProcessConstant.COMPOSITION_TYPE);
			if (Func.isNotEmpty(extCompType))
				flow.setCompositionType(Integer.valueOf(extCompType.get(0).getElementText()));
			List<ExtensionElement> extField = extensionElements.get(ProcessConstant.COMPOSITION_FIELD);
			if (Func.isNotEmpty(extField))
				flow.setCompositionField(extField.get(0).getElementText());
			List<ExtensionElement> inspectionType = extensionElements.get(ProcessConstant.INSPECTION_TYPE);
			if (Func.isNotEmpty(inspectionType))
				flow.setInspectionType(Integer.valueOf(inspectionType.get(0).getElementText()));

			flow.setTaskId(task.getId());
			flow.setTaskDefinitionKey(task.getTaskDefinitionKey());
			flow.setTaskName(task.getName());
			flow.setAssignee(task.getAssignee());
			flow.setCreateTime(task.getCreateTime());
			flow.setClaimTime(task.getClaimTime());
			flow.setExecutionId(task.getExecutionId());
			flow.setVariables(processVariables);
			flow.setPriority(task.getPriority());
			ProcessDefinition processDefinition = FlowCache.getProcessDefinition(task.getProcessDefinitionId());
			flow.setCategory(processDefinition.getCategory());
			flow.setCategoryName(FlowCache.getCategoryName(processDefinition.getCategory()));
			flow.setProcessDefinitionId(processDefinition.getId());
			flow.setProcessDefinitionName(processDefinition.getName());
			flow.setProcessDefinitionKey(processDefinition.getKey());
			flow.setProcessDefinitionVersion(processDefinition.getVersion());
			flow.setProcessInstanceId(task.getProcessInstanceId());


			if ("标注流程".equals(categoryName)) {
				LabelTask labelTask = labelTaskClient.queryLabelTask(task.getProcessInstanceId()).getData();
//					log.error("processInstanceId:"+task.getProcessInstanceId());
//					log.error("taskId:"+task.getId());
				if(labelTask.getId()!=null){
					flow.setTemplateId(labelTask.getTemplateId());
					flow.setPersonId(labelTask.getPersonId());
					flow.setPersonName(labelTask.getPersonName());
					flow.setSubTaskId(labelTask.getId());
					if (null != flow.getCompositionId()) {
						Role role = flowMapper.getRoleByTemplateComposition(flow.getTemplateId(), Long.valueOf(flow.getCompositionId()));
						flow.setRoleId(role.getId());
					}
					return flow;
				}
			} else if ("质检流程".equals(categoryName)) {
				QualityInspectionTask qualityInspectionTask = qualityInspectionTaskClient.queryQualityInspectionTask(task.getProcessInstanceId()).getData();
				if(qualityInspectionTask.getId()!=null){
					flow.setTemplateId(qualityInspectionTask.getTemplateId());
					flow.setPersonId(qualityInspectionTask.getPersonId());
					flow.setPersonName(qualityInspectionTask.getPersonName());
					flow.setSubTaskId(qualityInspectionTask.getId());
					flow.setInspectionTaskId(qualityInspectionTask.getInspectionTaskId());
					flow.setLabelTaskId(qualityInspectionTask.getLabelTaskId());
					flow.setAnnotationTaskId(qualityInspectionTask.getTaskId());
					return flow;
				}
			} else if ("合并专家流程".equals(categoryName)) {
				MergeExpertTask mergeExpertTask = mergeExpertTaskClient.queryMergeExpertTask(task.getProcessInstanceId()).getData();
				if (mergeExpertTask.getId() != null) {
					flow.setPersonId(mergeExpertTask.getPersonId());
					flow.setPersonName(mergeExpertTask.getPersonName());
					flow.setSubTaskId(mergeExpertTask.getId());
					flow.setMergeExpertTaskId(mergeExpertTask.getMergeTaskId());
					flow.setLabelTaskId(mergeExpertTask.getLabelTaskId());
					flow.setAnnotationTaskId(mergeExpertTask.getTaskId());
					return flow;
				}
			}
		}
		return null;
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

	@Override
	public List<CompositionClaimCountVO> getCompositionClaimCountByRoleAlias(List<String> roleAlias, Long userId) {
		List<String> compositionFilterList = flowMapper.getCompositionsByRoleAlias(roleAlias);
		compositionFilterList = compositionFilterList.stream().map(composition -> composition+"-"+userId+"-done").collect(Collectors.toList());
		return flowMapper.getCompositionClaimCountByRoleAlias(roleAlias, compositionFilterList);
	}

	@Override
	public String claimOneByCompositionId(List<String> roleAlias, Long userId, Long compositionId) {
		List<String> compositionFilterList = flowMapper.getCompositionsByRoleAlias(roleAlias);
		compositionFilterList = compositionFilterList.stream().map(composition -> composition+"-"+userId+"-done").collect(Collectors.toList());
		return flowMapper.claimOneByCompositionId(roleAlias, compositionId, compositionFilterList);
	}
}
