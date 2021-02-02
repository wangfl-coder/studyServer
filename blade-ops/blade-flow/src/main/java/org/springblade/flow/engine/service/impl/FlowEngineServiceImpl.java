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
package org.springblade.flow.engine.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
//import liquibase.pro.packaged.F;
//import liquibase.pro.packaged.S;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.engine.task.Comment;
import org.springblade.composition.dto.TemplateCompositionDTO;
import org.springblade.composition.dto.TemplateDTO;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.FileUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.flow.core.constant.ProcessConstant;
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.enums.FlowModeEnum;
import org.springblade.flow.core.utils.TaskUtil;
import org.springblade.flow.engine.constant.FlowEngineConstant;
import org.springblade.flow.engine.entity.FlowExecution;
import org.springblade.flow.engine.entity.FlowModel;
import org.springblade.flow.engine.entity.FlowProcess;
import org.springblade.flow.engine.mapper.FlowMapper;
import org.springblade.flow.engine.service.FlowEngineService;
import org.springblade.flow.engine.utils.FlowCache;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 工作流服务实现类
 *
 * @author Chill
 */
@Slf4j
@Service
@AllArgsConstructor
public class FlowEngineServiceImpl extends ServiceImpl<FlowMapper, FlowModel> implements FlowEngineService {
	private static final String ALREADY_IN_STATE = "already in state";
	private static final BpmnJsonConverter BPMN_JSON_CONVERTER = new BpmnJsonConverter();
	private static final BpmnXMLConverter BPMN_XML_CONVERTER = new BpmnXMLConverter();
	private final ObjectMapper objectMapper;
	private final RepositoryService repositoryService;
	private final RuntimeService runtimeService;
	private final HistoryService historyService;
	private final TaskService taskService;

	@Override
	public IPage<FlowModel> selectFlowPage(IPage<FlowModel> page, FlowModel flowModel) {
		return page.setRecords(baseMapper.selectFlowPage(page, flowModel));
	}

	@Override
	public IPage<FlowProcess> selectProcessPage(IPage<FlowProcess> page, String category, Integer mode) {
		ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery().latestVersion().orderByProcessDefinitionKey().asc();
		// 通用流程
		if (mode == FlowModeEnum.COMMON.getMode()) {
			processDefinitionQuery.processDefinitionWithoutTenantId();
		}
		// 定制流程
		else if (!AuthUtil.isAdministrator()) {
			processDefinitionQuery.processDefinitionTenantId(AuthUtil.getTenantId());
		}
		if (StringUtils.isNotEmpty(category)) {
			processDefinitionQuery.processDefinitionCategory(category);
		}
		List<ProcessDefinition> processDefinitionList = processDefinitionQuery.listPage(Func.toInt((page.getCurrent() - 1) * page.getSize()), Func.toInt(page.getSize()));
		List<FlowProcess> flowProcessList = new ArrayList<>();
		processDefinitionList.forEach(processDefinition -> {
			String deploymentId = processDefinition.getDeploymentId();
			Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
			FlowProcess flowProcess = new FlowProcess((ProcessDefinitionEntityImpl) processDefinition);
			flowProcess.setDeploymentTime(deployment.getDeploymentTime());
			flowProcessList.add(flowProcess);
		});
		page.setTotal(processDefinitionQuery.count());
		page.setRecords(flowProcessList);
		return page;
	}

	@Override
	public IPage<FlowExecution> selectFollowPage(IPage<FlowExecution> page, String processInstanceId, String processDefinitionKey) {
		ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();
		if (StringUtil.isNotBlank(processInstanceId)) {
			processInstanceQuery.processInstanceId(processInstanceId);
		}
		if (StringUtil.isNotBlank(processDefinitionKey)) {
			processInstanceQuery.processDefinitionKey(processDefinitionKey);
		}
		List<FlowExecution> flowList = new ArrayList<>();
		List<ProcessInstance> procInsList = processInstanceQuery.listPage(Func.toInt((page.getCurrent() - 1) * page.getSize()), Func.toInt(page.getSize()));
		procInsList.forEach(processInstance -> {
			ExecutionEntityImpl execution = (ExecutionEntityImpl) processInstance;
			FlowExecution flowExecution = new FlowExecution();
			flowExecution.setId(execution.getId());
			flowExecution.setName(execution.getName());
			flowExecution.setStartUserId(execution.getStartUserId());
			User taskUser = UserCache.getUserByTaskUser(execution.getStartUserId());
			if (taskUser != null) {
				flowExecution.setStartUser(taskUser.getName());
			}
			flowExecution.setStartTime(execution.getStartTime());
			flowExecution.setExecutionId(execution.getId());
			flowExecution.setProcessInstanceId(execution.getProcessInstanceId());
			flowExecution.setProcessDefinitionId(execution.getProcessDefinitionId());
			flowExecution.setProcessDefinitionKey(execution.getProcessDefinitionKey());
			flowExecution.setSuspensionState(execution.getSuspensionState());
			ProcessDefinition processDefinition = FlowCache.getProcessDefinition(execution.getProcessDefinitionId());
			flowExecution.setCategory(processDefinition.getCategory());
			flowExecution.setCategoryName(FlowCache.getCategoryName(processDefinition.getCategory()));
			flowList.add(flowExecution);
		});
		page.setTotal(processInstanceQuery.count());
		page.setRecords(flowList);
		return page;
	}

	@Override
	public List<BladeFlow> historyFlowList(String processInstanceId, String startActivityId, String endActivityId) {
		List<BladeFlow> flowList = new LinkedList<>();
		List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().orderByHistoricActivityInstanceEndTime().asc().list();
		boolean start = false;
		Map<String, Integer> activityMap = new HashMap<>(16);
		for (int i = 0; i < historicActivityInstanceList.size(); i++) {
			HistoricActivityInstance historicActivityInstance = historicActivityInstanceList.get(i);
			// 过滤开始节点前的节点
			if (StringUtil.isNotBlank(startActivityId) && startActivityId.equals(historicActivityInstance.getActivityId())) {
				start = true;
			}
			if (StringUtil.isNotBlank(startActivityId) && !start) {
				continue;
			}
			// 显示开始节点和结束节点，并且执行人不为空的任务
			if (StringUtils.isNotBlank(historicActivityInstance.getAssignee())
				|| FlowEngineConstant.START_EVENT.equals(historicActivityInstance.getActivityType())
				|| FlowEngineConstant.END_EVENT.equals(historicActivityInstance.getActivityType())) {
				// 给节点增加序号
				Integer activityNum = activityMap.get(historicActivityInstance.getActivityId());
				if (activityNum == null) {
					activityMap.put(historicActivityInstance.getActivityId(), activityMap.size());
				}
				BladeFlow flow = new BladeFlow();
				flow.setHistoryActivityId(historicActivityInstance.getActivityId());
				flow.setHistoryActivityName(historicActivityInstance.getActivityName());
				flow.setCreateTime(historicActivityInstance.getStartTime());
				flow.setEndTime(historicActivityInstance.getEndTime());
				String durationTime = DateUtil.secondToTime(Func.toLong(historicActivityInstance.getDurationInMillis(), 0L) / 1000);
				flow.setHistoryActivityDurationTime(durationTime);
				// 获取流程发起人名称
				if (FlowEngineConstant.START_EVENT.equals(historicActivityInstance.getActivityType())) {
					List<HistoricProcessInstance> processInstanceList = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).orderByProcessInstanceStartTime().asc().list();
					if (processInstanceList.size() > 0) {
						if (StringUtil.isNotBlank(processInstanceList.get(0).getStartUserId())) {
							String taskUser = processInstanceList.get(0).getStartUserId();
							User user = UserCache.getUser(TaskUtil.getUserId(taskUser));
							if (user != null) {
								flow.setAssignee(historicActivityInstance.getAssignee());
								flow.setAssigneeName(user.getName());
							}
						}
					}
				}
				// 获取任务执行人名称
				if (StringUtil.isNotBlank(historicActivityInstance.getAssignee())) {
					User user = UserCache.getUser(TaskUtil.getUserId(historicActivityInstance.getAssignee()));
					if (user != null) {
						flow.setAssignee(historicActivityInstance.getAssignee());
						flow.setAssigneeName(user.getName());
					}
				}
				// 获取意见评论内容
				if (StringUtil.isNotBlank(historicActivityInstance.getTaskId())) {
					List<Comment> commentList = taskService.getTaskComments(historicActivityInstance.getTaskId());
					if (commentList.size() > 0) {
						flow.setComment(commentList.get(0).getFullMessage());
					}
				}
				flowList.add(flow);
			}
			// 过滤结束节点后的节点
			if (StringUtils.isNotBlank(endActivityId) && endActivityId.equals(historicActivityInstance.getActivityId())) {
				boolean temp = false;
				Integer activityNum = activityMap.get(historicActivityInstance.getActivityId());
				// 该活动节点，后续节点是否在结束节点之前，在后续节点中是否存在
				for (int j = i + 1; j < historicActivityInstanceList.size(); j++) {
					HistoricActivityInstance hi = historicActivityInstanceList.get(j);
					Integer activityNumA = activityMap.get(hi.getActivityId());
					boolean numberTemp = activityNumA != null && activityNumA < activityNum;
					boolean equalsTemp = StringUtils.equals(hi.getActivityId(), historicActivityInstance.getActivityId());
					if (numberTemp || equalsTemp) {
						temp = true;
					}
				}
				if (!temp) {
					break;
				}
			}
		}
		return flowList;
	}

	@Override
	public String changeState(String state, String processId) {
		try {
			if (state.equals(FlowEngineConstant.ACTIVE)) {
				repositoryService.activateProcessDefinitionById(processId, true, null);
				return StringUtil.format("激活ID为 [{}] 的流程成功", processId);
			} else if (state.equals(FlowEngineConstant.SUSPEND)) {
				repositoryService.suspendProcessDefinitionById(processId, true, null);
				return StringUtil.format("挂起ID为 [{}] 的流程成功", processId);
			} else {
				return "暂无流程变更";
			}
		} catch (Exception e) {
			if (e.getMessage().contains(ALREADY_IN_STATE)) {
				return StringUtil.format("ID为 [{}] 的流程已是此状态，无需操作", processId);
			}
			return e.getMessage();
		}
	}

	@Override
	public boolean deleteDeployment(String deploymentIds) {
		Func.toStrList(deploymentIds).forEach(deploymentId -> repositoryService.deleteDeployment(deploymentId, true));
		return true;
	}

	@Override
	public boolean deployUpload(List<MultipartFile> files, String category, List<String> tenantIdList) {
		files.forEach(file -> {
			try {
				String fileName = file.getOriginalFilename();
				InputStream fileInputStream = file.getInputStream();
				byte[] bytes = FileUtil.copyToByteArray(fileInputStream);
				if (Func.isNotEmpty(tenantIdList)) {
					tenantIdList.forEach(tenantId -> {
						Deployment deployment = repositoryService.createDeployment().addBytes(fileName, bytes).tenantId(tenantId).deploy();
						deploy(deployment, category);
					});
				} else {
					Deployment deployment = repositoryService.createDeployment().addBytes(fileName, bytes).deploy();
					deploy(deployment, category);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		return true;
	}

	@Override
	public boolean deployModel(String modelId, String category, List<String> tenantIdList) {
		FlowModel model = this.getById(modelId);
		if (model == null) {
			throw new ServiceException("No model found with the given id: " + modelId);
		}
		byte[] bytes = getBpmnXML(model);
		String processName = model.getName();
		if (!StringUtil.endsWithIgnoreCase(processName, FlowEngineConstant.SUFFIX)) {
			processName += FlowEngineConstant.SUFFIX;
		}
		String finalProcessName = processName;
		if (Func.isNotEmpty(tenantIdList)) {
			tenantIdList.forEach(tenantId -> {
				Deployment deployment = repositoryService.createDeployment().addBytes(finalProcessName, bytes).name(model.getName()).key(model.getModelKey()).tenantId(tenantId).deploy();
				deploy(deployment, category);
			});
		} else {
			Deployment deployment = repositoryService.createDeployment().addBytes(finalProcessName, bytes).name(model.getName()).key(model.getModelKey()).deploy();
			deploy(deployment, category);
		}
		return true;
	}

	@Override
	public String deployModelByTemplate(String modelId, String category, List<String> tenantIdList, TemplateDTO templateDTO) {
		FlowModel model = this.getById(modelId);
		if (model == null) {
			throw new ServiceException("No model found with the given id: " + modelId);
		}
		BpmnModel bpmnModel = getBpmnModel(model);

		// filter
		String[] filterArr = {"biFlow*", "biPassFlow*", "basicInfoTask*"};
		Process process = bpmnModel.getProcesses().get(0);
		List<FlowElement> flowElementList = (List<FlowElement>) process.getFlowElements();
		flowElementList.removeIf(flowElement -> StringUtil.simpleMatch(filterArr, flowElement.getId()) );

		Map<String,FlowElement> flowElementMap = process.getFlowElementMap();
		Map<String,FlowElement> filteredElementMap = flowElementMap.entrySet().stream()
			.filter(flowElement -> !StringUtil.simpleMatch(filterArr, flowElement.getKey()) )
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// add
		List<TemplateCompositionDTO> templateCompositions = templateDTO.getTemplateCompositions();
		final AtomicInteger counter = new AtomicInteger(1);
		setTaskRoleNameByComposition(flowElementList, filteredElementMap, "homepageTask", null, 1, templateCompositions);
		templateCompositions.forEach(templateComposition -> {
		if (2 == templateComposition.getCompositionType()) {	//基本信息标注
				SequenceFlow incomingFlow = new SequenceFlow();
				incomingFlow.setSourceRef("distributeTaskGateway");
				incomingFlow.setTargetRef("comp-"+templateComposition.getCompositionId());
				incomingFlow.setId("biFlow"+counter.get());
				incomingFlow.setParentContainer(process);
				List<SequenceFlow> incomingFlows = new ArrayList<>();
				incomingFlows.add(incomingFlow);

				SequenceFlow outgoingFlow = new SequenceFlow();
				outgoingFlow.setConditionExpression("${pass}");
				outgoingFlow.setSourceRef("comp-"+templateComposition.getCompositionId());
				outgoingFlow.setTargetRef("collectTaskGateway");
				outgoingFlow.setName("完成");
				outgoingFlow.setId("biPassFlow"+counter.get());
				outgoingFlow.setParentContainer(process);
				List<SequenceFlow> outgoingFlows = new ArrayList<>();
				outgoingFlows.add(outgoingFlow);

				UserTask basicInfoTask = new UserTask();
				basicInfoTask.setId("comp-"+templateComposition.getCompositionId());
				basicInfoTask.setName(templateComposition.getCompositionName());
				List<String> candidateGroups = new ArrayList<>();
				candidateGroups.add(templateComposition.getLabelRoleName());
				basicInfoTask.setCandidateGroups(candidateGroups);
				basicInfoTask.setIncomingFlows(incomingFlows);
				basicInfoTask.setOutgoingFlows(outgoingFlows);

				List<CustomProperty> customPropertyList = buildCustomPropertyListWithComposition(templateComposition, false);
				basicInfoTask.setCustomProperties(customPropertyList);

				flowElementList.add(incomingFlow);
				flowElementList.add(outgoingFlow);
				flowElementList.add(basicInfoTask);
				filteredElementMap.put(incomingFlow.getId(), incomingFlow);
				filteredElementMap.put(outgoingFlow.getId(), outgoingFlow);
				filteredElementMap.put(basicInfoTask.getId(), basicInfoTask);
				counter.getAndIncrement();
			}
		});

		flowElementList.stream().forEach(flowElement -> {
			if (flowElement.getId().equals("complementInfoTask")) {
				UserTask userTask =  (UserTask)flowElement;
				List<String> candidateGroups = new ArrayList<>();
				candidateGroups.add(templateDTO.getMoreMessageRoleName());
				userTask.setCandidateGroups(candidateGroups);
			}
		});
		UserTask userTask =  (UserTask)filteredElementMap.get("complementInfoTask");
		List<String> candidateGroups = new ArrayList<>();
		candidateGroups.add(templateDTO.getMoreMessageRoleName());
		userTask.setCandidateGroups(candidateGroups);
		filteredElementMap.put("complementInfoTask", userTask);

		process.setFlowElementMap(filteredElementMap);
		byte[] bytes = getBpmnXML(bpmnModel);
		String processName = model.getName();
		if (!StringUtil.endsWithIgnoreCase(processName, FlowEngineConstant.SUFFIX)) {
			processName += FlowEngineConstant.SUFFIX;
		}
		String finalProcessName = processName;
		if (Func.isNotEmpty(tenantIdList)) {
			tenantIdList.forEach(tenantId -> {
				Deployment deployment = repositoryService.createDeployment().addBytes(finalProcessName, bytes).name(model.getName()).key(model.getModelKey()).tenantId(tenantId).deploy();
				deployTemplate(deployment, category);
			});
		} else {
			Deployment deployment = repositoryService.createDeployment().addBytes(finalProcessName, bytes).name(model.getName()).key(model.getModelKey()).deploy();
			return deployTemplate(deployment, category);
		}
		return null;
	}

	@Override
	public String deployModelByTemplateV2(String modelId, String category, List<String> tenantIdList, TemplateDTO templateDTO) {
		FlowModel model = this.getById(modelId);
		if (model == null) {
			throw new ServiceException("No model found with the given id: " + modelId);
		}
		BpmnModel bpmnModel = getBpmnModel(model);

		// filter
		String[] filterArr = {
			"biFlow*", "biPassFlow*",
			"exclusiveGateway*",  "exclusiveCollectGateway*", "biBreakFlow*",
			"labelBiTask*", "labelBiFlow*", "labelBiPassFlow*",
			"inspectBiTask*", "inspectBiFlow*", "inspectBiPassFlow*"
		};
		Process process = bpmnModel.getProcesses().get(0);
		List<FlowElement> flowElementList = (List<FlowElement>) process.getFlowElements();
		flowElementList.removeIf(flowElement -> StringUtil.simpleMatch(filterArr, flowElement.getId()) );

		Map<String,FlowElement> flowElementMap = process.getFlowElementMap();
		Map<String,FlowElement> filteredElementMap = flowElementMap.entrySet().stream()
			.filter(flowElement -> !StringUtil.simpleMatch(filterArr, flowElement.getKey()) )
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		// add
		List<TemplateCompositionDTO> templateCompositions = templateDTO.getTemplateCompositions();
		final AtomicInteger counter = new AtomicInteger(1);
		List<ValuedDataObject> dataObjects = process.getDataObjects();
		ValuedDataObject homepageDataObject = null;
		for (ValuedDataObject dataObject: dataObjects ) {
			if (dataObject.getName().equals("hasHomepage"))
				homepageDataObject = dataObject;
		}
		boolean hasHomepage = setTaskRoleNameByComposition(flowElementList, filteredElementMap, "labelerHomepageTask", "inspectorHomepageTask", 1, templateCompositions);
		if (hasHomepage)
			homepageDataObject.setValue(true);
		else
			homepageDataObject.setValue(false);

		ValuedDataObject eduWorkDataObject = null;
		for (ValuedDataObject dataObject: dataObjects ) {
			if (dataObject.getName().equals("hasEduWork"))
				eduWorkDataObject = dataObject;
		}
		setTaskRoleNameByComposition(flowElementList, filteredElementMap, "labelEduWorkTaskJunior", "inspectEduWorkTask", 4, templateCompositions);
		boolean hasEduWork = setTaskRoleNameByComposition(flowElementList, filteredElementMap, "labelEduWorkTaskSenior", "inspectEduWorkTask", 4, templateCompositions);
		if (hasEduWork)
			eduWorkDataObject.setValue(true);
		else
			eduWorkDataObject.setValue(false);

		ValuedDataObject bioDataObject = null;
		for (ValuedDataObject dataObject: dataObjects ) {
			if (dataObject.getName().equals("hasBio"))
				bioDataObject = dataObject;
		}
		boolean hasBio = setTaskRoleNameByComposition(flowElementList, filteredElementMap, "labelBioTask", "inspectBioTask", 5, templateCompositions);
		if (hasBio)
			bioDataObject.setValue(true);
		else
			bioDataObject.setValue(false);


		templateCompositions.forEach(templateComposition -> {
			if (2 == templateComposition.getCompositionType()) {	//基本信息标注
				HashMap<String, String> dict = new HashMap<>();
				dict.put("id", templateComposition.getCompositionId().toString());

				// biFlow*
				SequenceFlow biFlow = new SequenceFlow();
				biFlow.setId("biFlow"+counter.get());
				biFlow.setSourceRef("distributeTaskGateway");
				biFlow.setTargetRef("exclusiveGateway"+counter.get());
				biFlow.setParentContainer(process);
				List<SequenceFlow> exclusiveGatewayIncomingFlows = new ArrayList<>();
				exclusiveGatewayIncomingFlows.add(biFlow);

				// labelBiFlow*
				SequenceFlow labelBiFlow = new SequenceFlow();
				labelBiFlow.setId("labelBiFlow"+counter.get());
				labelBiFlow.setSourceRef("exclusiveGateway"+counter.get());
				labelBiFlow.setTargetRef("labelBiTask"+counter.get());
				labelBiFlow.setParentContainer(process);
				labelBiFlow.setName("不到二人;二人不同;二人都没找到");
				String labelBiFlowConditionStr = StringUtil.format("${biCounter{id} < 2 || (biCounter{id} == 2 && biSame{id} == 0) || (biCounter{id} == 2 && biNotfound{id} == 2)}", dict);
				labelBiFlow.setConditionExpression(labelBiFlowConditionStr);
//				labelBiFlow.setConditionExpression("${biCounter1 < 2 || (biCounter1 == 2 && biSame1 == 0) || (biCounter1 == 2 && biNotfound1 == 2)}");
				List<SequenceFlow> labelBiTaskIncomingFlows = new ArrayList<>();
				labelBiTaskIncomingFlows.add(labelBiFlow);
				List<SequenceFlow> exclusiveGatewayOutgoingFlows = new ArrayList<>();
				exclusiveGatewayOutgoingFlows.add(labelBiFlow);

				// labelBiPassFlow*
				SequenceFlow labelBiPassFlow = new SequenceFlow();
				labelBiPassFlow.setId("labelBiPassFlow"+counter.get());
				labelBiPassFlow.setSourceRef("labelBiTask"+counter.get());
				labelBiPassFlow.setTargetRef("exclusiveGateway"+counter.get());
				labelBiPassFlow.setParentContainer(process);
				labelBiPassFlow.setName("基本信息标注完成1");
				labelBiPassFlow.setConditionExpression("${pass}");
//				List<SequenceFlow> exclusiveGatewayIncomingFlows = new ArrayList<>();
				exclusiveGatewayIncomingFlows.add(labelBiPassFlow);
				List<SequenceFlow> labelBiTaskOutgoingFlows = new ArrayList<>();
				labelBiTaskOutgoingFlows.add(labelBiPassFlow);

				// inspectBiFlow*
				SequenceFlow inspectBiFlow = new SequenceFlow();
				inspectBiFlow.setId("inspectBiFlow"+counter.get());
				inspectBiFlow.setSourceRef("exclusiveGateway"+counter.get());
				inspectBiFlow.setTargetRef("inspectBiTask"+counter.get());
				inspectBiFlow.setParentContainer(process);
				inspectBiFlow.setName("三人都不同去质检");
				String inspectBiFlowConditionStr = StringUtil.format("${biCounter{id} == 3 && biSame{id} == 0}", dict);
				inspectBiFlow.setConditionExpression(inspectBiFlowConditionStr);
//				inspectBiFlow.setConditionExpression("${biCounter1 == 3 && biSame1 == 0}");
				List<SequenceFlow> inspectBiTaskIncomingFlows = new ArrayList<>();
				inspectBiTaskIncomingFlows.add(inspectBiFlow);
//				List<SequenceFlow> exclusiveGatewayOutgoingFlows = new ArrayList<>();
				exclusiveGatewayOutgoingFlows.add(inspectBiFlow);

				// inspectBiPassFlow*
				SequenceFlow inspectBiPassFlow = new SequenceFlow();
				inspectBiPassFlow.setId("inspectBiPassFlow"+counter.get());
				inspectBiPassFlow.setSourceRef("inspectBiTask"+counter.get());
				inspectBiPassFlow.setTargetRef("exclusiveCollectGateway"+counter.get());
				inspectBiPassFlow.setParentContainer(process);
				inspectBiPassFlow.setName("个人信息质检完成1");
				inspectBiPassFlow.setConditionExpression("${pass}");
				List<SequenceFlow> exclusiveCollectGatewayIncomingFlows = new ArrayList<>();
				exclusiveGatewayIncomingFlows.add(inspectBiPassFlow);
				List<SequenceFlow> inspectBiTaskOutgoingFlows = new ArrayList<>();
				inspectBiTaskOutgoingFlows.add(inspectBiPassFlow);

				// biBreakFlow*
				SequenceFlow biBreakFlow = new SequenceFlow();
				biBreakFlow.setId("biBreakFlow"+counter.get());
				biBreakFlow.setSourceRef("exclusiveGateway"+counter.get());
				biBreakFlow.setTargetRef("exclusiveCollectGateway"+counter.get());
				biBreakFlow.setParentContainer(process);
				biBreakFlow.setName("二人相同;三人中二人相同;三人都没找到");
				String biBreakFlowConditionStr = StringUtil.format("${(biCounter{id} == 2 && biSame{id} == 1) || (biCounter{id} == 3 && biSame{id} == 1) || (biCounter{id} == 3 && biNotfound{id} == 3)}", dict);
				biBreakFlow.setConditionExpression(biBreakFlowConditionStr);
//				biBreakFlow.setConditionExpression("${(biCounter1 == 2 && biSame1 == 1) || (biCounter1 == 3 && biSame1 == 1) || (biCounter1 == 3 && biNotfound1 == 3)}");
//				List<SequenceFlow> exclusiveCollectGatewayIncomingFlows = new ArrayList<>();
				exclusiveCollectGatewayIncomingFlows.add(biBreakFlow);
//				List<SequenceFlow> exclusiveGatewayOutgoingFlows = new ArrayList<>();
				exclusiveGatewayOutgoingFlows.add(biBreakFlow);

				// biPassFlow*
				SequenceFlow biPassFlow = new SequenceFlow();
				biPassFlow.setId("biPassFlow"+counter.get());
				biPassFlow.setSourceRef("exclusiveCollectGateway"+counter.get());
				biPassFlow.setTargetRef("collectTaskGateway");
				biPassFlow.setParentContainer(process);
				List<SequenceFlow> exclusiveCollectGatewayOutgoingFlows = new ArrayList<>();
				exclusiveCollectGatewayOutgoingFlows.add(biPassFlow);

				Gateway exclusiveGateway = new ExclusiveGateway();
				exclusiveGateway.setId("exclusiveGateway"+counter.get());
				exclusiveGateway.setIncomingFlows(exclusiveGatewayIncomingFlows);
				exclusiveGateway.setOutgoingFlows(exclusiveGatewayOutgoingFlows);

				UserTask labelBiTask = new UserTask();
				labelBiTask.setId("labelBiTask"+counter.get());
				labelBiTask.setName(templateComposition.getCompositionName());
				List<String> labelCandidateGroups = new ArrayList<>();
				labelCandidateGroups.add(templateComposition.getLabelRoleName());
				labelBiTask.setCandidateGroups(labelCandidateGroups);
				labelBiTask.setIncomingFlows(labelBiTaskIncomingFlows);
				labelBiTask.setOutgoingFlows(labelBiTaskOutgoingFlows);
				List<CustomProperty> labelPropertyList = buildCustomPropertyListWithComposition(templateComposition, false);
				labelBiTask.setCustomProperties(labelPropertyList);

				UserTask inspectBiTask = new UserTask();
				inspectBiTask.setId("inspectBiTask"+counter.get());
				inspectBiTask.setName(templateComposition.getCompositionName());
				List<String> inspectionCandidateGroups = new ArrayList<>();
				inspectionCandidateGroups.add(templateComposition.getInspectionRoleName());
				inspectBiTask.setCandidateGroups(inspectionCandidateGroups);
				inspectBiTask.setIncomingFlows(inspectBiTaskIncomingFlows);
				inspectBiTask.setOutgoingFlows(inspectBiTaskOutgoingFlows);
				List<CustomProperty> inspectPropertyList = buildCustomPropertyListWithComposition(templateComposition, true);
				inspectBiTask.setCustomProperties(inspectPropertyList);

				Gateway exclusiveCollectGateway = new ExclusiveGateway();
				exclusiveCollectGateway.setId("exclusiveCollectGateway"+counter.get());
				exclusiveCollectGateway.setIncomingFlows(exclusiveCollectGatewayIncomingFlows);
				exclusiveCollectGateway.setOutgoingFlows(exclusiveCollectGatewayOutgoingFlows);

				flowElementList.add(biFlow);
				flowElementList.add(biPassFlow);
				flowElementList.add(exclusiveGateway);
				flowElementList.add(exclusiveCollectGateway);
				flowElementList.add(biBreakFlow);
				flowElementList.add(labelBiTask);
				flowElementList.add(labelBiFlow);
				flowElementList.add(labelBiPassFlow);
				flowElementList.add(inspectBiTask);
				flowElementList.add(inspectBiFlow);
				flowElementList.add(inspectBiPassFlow);
				filteredElementMap.put(biFlow.getId(), biFlow);
				filteredElementMap.put(biPassFlow.getId(), biPassFlow);
				filteredElementMap.put(exclusiveGateway.getId(), exclusiveGateway);
				filteredElementMap.put(exclusiveCollectGateway.getId(), exclusiveCollectGateway);
				filteredElementMap.put(biBreakFlow.getId(), biBreakFlow);
				filteredElementMap.put(labelBiTask.getId(), labelBiTask);
				filteredElementMap.put(labelBiFlow.getId(), labelBiFlow);
				filteredElementMap.put(labelBiPassFlow.getId(), labelBiPassFlow);
				filteredElementMap.put(inspectBiTask.getId(), inspectBiTask);
				filteredElementMap.put(inspectBiFlow.getId(), inspectBiFlow);
				filteredElementMap.put(inspectBiPassFlow.getId(), inspectBiPassFlow);
				counter.getAndIncrement();
			}
		});

		// 补充信息任务设置角色
		flowElementList.stream().forEach(flowElement -> {
			if (flowElement.getId().equals("complementInfoTask")) {
				UserTask userTask =  (UserTask)flowElement;
				List<String> candidateGroups = new ArrayList<>();
				candidateGroups.add(templateDTO.getMoreMessageRoleName());
				userTask.setCandidateGroups(candidateGroups);
				List<CustomProperty> customPropertyList = new ArrayList<>();
				CustomProperty compositionIdProperty = new CustomProperty();
				compositionIdProperty.setName(ProcessConstant.COMPOSITION_ID);
				compositionIdProperty.setSimpleValue("-1");
				customPropertyList.add(compositionIdProperty);
				userTask.setCustomProperties(customPropertyList);
			}
		});
		UserTask userTask =  (UserTask)filteredElementMap.get("complementInfoTask");
		List<String> candidateGroups = new ArrayList<>();
		candidateGroups.add(templateDTO.getMoreMessageRoleName());
		userTask.setCandidateGroups(candidateGroups);
		filteredElementMap.put("complementInfoTask", userTask);

		process.setFlowElementMap(filteredElementMap);
		byte[] bytes = getBpmnXML(bpmnModel);
		String processName = model.getName();
		if (!StringUtil.endsWithIgnoreCase(processName, FlowEngineConstant.SUFFIX)) {
			processName += FlowEngineConstant.SUFFIX;
		}
		String finalProcessName = processName;
		if (Func.isNotEmpty(tenantIdList)) {
			tenantIdList.forEach(tenantId -> {
				Deployment deployment = repositoryService.createDeployment().addBytes(finalProcessName, bytes).name(model.getName()).key(model.getModelKey()).tenantId(tenantId).deploy();
				deployTemplate(deployment, category);
			});
		} else {
			Deployment deployment = repositoryService.createDeployment().addBytes(finalProcessName, bytes).name(model.getName()).key(model.getModelKey()).deploy();
			return deployTemplate(deployment, category);
		}
		return null;
	}

	@Override
	public Map<String, String> deployRealSetModelByTemplate(String modelId, String category, List<String> tenantIdList, TemplateDTO templateDTO) {
		FlowModel model = this.getById(modelId);
		if (model == null) {
			throw new ServiceException("No model found with the given id: " + modelId);
		}
		BpmnModel bpmnModel = getBpmnModel(model);

		// filter
		String[] filterArr = {"biFlow*", "biPassFlow*", "basicInfoTask*"};
		Process process = bpmnModel.getProcesses().get(0);
		List<FlowElement> flowElementList = (List<FlowElement>) process.getFlowElements();
		flowElementList.removeIf(flowElement -> StringUtil.simpleMatch(filterArr, flowElement.getId()) );

		Map<String,FlowElement> flowElementMap = process.getFlowElementMap();

		Map<String,String> processDefinitionMap = new HashMap<>();
		List<TemplateCompositionDTO> templateCompositions = templateDTO.getTemplateCompositions();
		templateCompositions.forEach(templateComposition -> {
			if (2 == templateComposition.getCompositionType()) {    //基本信息标注
				flowElementList.stream().forEach(flowElement -> {
					if (flowElement.getId().equals("labelTask")) {
						UserTask userTask = (UserTask) flowElement;
						userTask.setName(templateComposition.getCompositionName());
						List<String> candidateGroups = new ArrayList<>();
						candidateGroups.add(templateComposition.getLabelRoleName());
						userTask.setCandidateGroups(candidateGroups);
						List<CustomProperty> labelPropertyList = buildCustomPropertyListWithComposition(templateComposition, false);
						userTask.setCustomProperties(labelPropertyList);
					}
				});
				UserTask userTask = (UserTask) flowElementMap.get("labelTask");
				userTask.setName(templateComposition.getCompositionName());
				List<String> candidateGroups = new ArrayList<>();
				candidateGroups.add(templateComposition.getLabelRoleName());
				userTask.setCandidateGroups(candidateGroups);
				List<CustomProperty> labelPropertyList = buildCustomPropertyListWithComposition(templateComposition, false);
				userTask.setCustomProperties(labelPropertyList);
				flowElementMap.put("labelTask", userTask);

				process.setFlowElementMap(flowElementMap);
				byte[] bytes = getBpmnXML(bpmnModel);
				String processName = model.getName();
				if (!StringUtil.endsWithIgnoreCase(processName, FlowEngineConstant.SUFFIX)) {
					processName += FlowEngineConstant.SUFFIX;
				}
				String finalProcessName = processName;
				String processDefinitionId = "";
				if (Func.isNotEmpty(tenantIdList)) {
					tenantIdList.forEach(tenantId -> {
						Deployment deployment = repositoryService.createDeployment().addBytes(finalProcessName, bytes).name(model.getName()).key(model.getModelKey()).tenantId(tenantId).deploy();
						String tenantProcessDefinitionId = deployTemplate(deployment, category);
					});
				} else {
					Deployment deployment = repositoryService.createDeployment().addBytes(finalProcessName, bytes).name(model.getName()).key(model.getModelKey()).deploy();
					processDefinitionId = deployTemplate(deployment, category);
				}
				processDefinitionMap.put(templateComposition.getCompositionId().toString(), processDefinitionId);
			}
		});
		return processDefinitionMap;
	}

	@Override
	public boolean deleteProcessInstance(String processInstanceId, String deleteReason) {
		runtimeService.deleteProcessInstance(processInstanceId, deleteReason);
		return true;
	}

	private boolean deploy(Deployment deployment, String category) {
		log.debug("流程部署--------deploy:  " + deployment + "  分类---------->" + category);
		List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
		StringBuilder logBuilder = new StringBuilder(500);
		List<Object> logArgs = new ArrayList<>();
		// 设置流程分类
		for (ProcessDefinition processDefinition : list) {
			if (StringUtil.isNotBlank(category)) {
				repositoryService.setProcessDefinitionCategory(processDefinition.getId(), category);
			}
			logBuilder.append("部署成功,流程ID={} \n");
			logArgs.add(processDefinition.getId());
		}
		if (list.size() == 0) {
			throw new ServiceException("部署失败,未找到流程");
		} else {
			log.info(logBuilder.toString(), logArgs.toArray());
			return true;
		}
	}

	private String deployTemplate(Deployment deployment, String category) {
		log.debug("流程部署--------deploy:  " + deployment + "  分类---------->" + category);
		List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
		StringBuilder logBuilder = new StringBuilder(500);
		List<Object> logArgs = new ArrayList<>();
		// 设置流程分类
		for (ProcessDefinition processDefinition : list) {
			if (StringUtil.isNotBlank(category)) {
				repositoryService.setProcessDefinitionCategory(processDefinition.getId(), category);
			}
			logBuilder.append("部署成功,流程ID={} \n");
			logArgs.add(processDefinition.getId());
			return processDefinition.getId();
		}
		if (list.size() == 0) {
			throw new ServiceException("部署失败,未找到流程");
		} else {
			log.info(logBuilder.toString(), logArgs.toArray());
//			return true;
		}
		return null;
	}

	private byte[] getBpmnXML(FlowModel model) {
		BpmnModel bpmnModel = getBpmnModel(model);
		return getBpmnXML(bpmnModel);
	}

	private byte[] getBpmnXML(BpmnModel bpmnModel) {
		for (Process process : bpmnModel.getProcesses()) {
			if (StringUtils.isNotEmpty(process.getId())) {
				char firstCharacter = process.getId().charAt(0);
				if (Character.isDigit(firstCharacter)) {
					process.setId("a" + process.getId());
				}
			}
		}
		return BPMN_XML_CONVERTER.convertToXML(bpmnModel);
	}

	private BpmnModel getBpmnModel(FlowModel model) {
		BpmnModel bpmnModel;
		try {
			Map<String, FlowModel> formMap = new HashMap<>(16);
			Map<String, FlowModel> decisionTableMap = new HashMap<>(16);

			List<FlowModel> referencedModels = baseMapper.findByParentModelId(model.getId());
			for (FlowModel childModel : referencedModels) {
				if (FlowModel.MODEL_TYPE_FORM == childModel.getModelType()) {
					formMap.put(childModel.getId(), childModel);

				} else if (FlowModel.MODEL_TYPE_DECISION_TABLE == childModel.getModelType()) {
					decisionTableMap.put(childModel.getId(), childModel);
				}
			}
			bpmnModel = getBpmnModel(model, formMap, decisionTableMap);
		} catch (Exception e) {
			log.error("Could not generate BPMN 2.0 model for {}", model.getId(), e);
			throw new ServiceException("Could not generate BPMN 2.0 model");
		}
		return bpmnModel;
	}

	private BpmnModel getBpmnModel(FlowModel model, Map<String, FlowModel> formMap, Map<String, FlowModel> decisionTableMap) {
		try {
			ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(model.getModelEditorJson());
			Map<String, String> formKeyMap = new HashMap<>(16);
			for (FlowModel formModel : formMap.values()) {
				formKeyMap.put(formModel.getId(), formModel.getModelKey());
			}
			Map<String, String> decisionTableKeyMap = new HashMap<>(16);
			for (FlowModel decisionTableModel : decisionTableMap.values()) {
				decisionTableKeyMap.put(decisionTableModel.getId(), decisionTableModel.getModelKey());
			}
			return BPMN_JSON_CONVERTER.convertToBpmnModel(editorJsonNode, formKeyMap, decisionTableKeyMap);
		} catch (Exception e) {
			log.error("Could not generate BPMN 2.0 model for {}", model.getId(), e);
			throw new ServiceException("Could not generate BPMN 2.0 model");
		}
	}

	private List<CustomProperty> buildCustomPropertyListWithComposition(TemplateCompositionDTO templateComposition, boolean isInspection) {
		List<CustomProperty> customPropertyList = new ArrayList<>();
		CustomProperty compositionIdProperty = new CustomProperty();
		compositionIdProperty.setName(ProcessConstant.COMPOSITION_ID);
		compositionIdProperty.setSimpleValue(templateComposition.getCompositionId().toString());
		CustomProperty compositionTypeProperty = new CustomProperty();
		compositionTypeProperty.setName(ProcessConstant.COMPOSITION_TYPE);
		compositionTypeProperty.setSimpleValue(String.valueOf(templateComposition.getCompositionType()));
		CustomProperty compositionFieldProperty = new CustomProperty();
		compositionFieldProperty.setName(ProcessConstant.COMPOSITION_FIELD);
		compositionFieldProperty.setSimpleValue(templateComposition.getCompositionField());
		if (isInspection) {
			CustomProperty compositionInspectionTypeProperty = new CustomProperty();
			compositionInspectionTypeProperty.setName(ProcessConstant.INSPECTION_TYPE);
			compositionInspectionTypeProperty.setSimpleValue(String.valueOf(templateComposition.getCompositionType()));
			customPropertyList.add(compositionInspectionTypeProperty);
		}
		customPropertyList.add(compositionIdProperty);
		customPropertyList.add(compositionTypeProperty);
		customPropertyList.add(compositionFieldProperty);
		return customPropertyList;
	}

	private boolean setTaskRoleNameByComposition(List<FlowElement> flowElementList, Map<String,FlowElement> flowElementMap, String labelTaskName, String inspectionTaskName, Integer annotationType, List<TemplateCompositionDTO> templateCompositions) {
		AtomicBoolean hasComposition = new AtomicBoolean(false);
		templateCompositions.forEach(templateComposition -> {
			if (annotationType == templateComposition.getCompositionType()) {    //	主页标注/工作、教育经历标注/中英文简介标注
				hasComposition.set(true);
				flowElementList.stream().forEach(flowElement -> {
					if (flowElement.getId().equals(labelTaskName)) {
						UserTask userTask = (UserTask) flowElement;
						userTask.setName(templateComposition.getCompositionName());
						List<String> candidateGroups = new ArrayList<>();
						candidateGroups.add(templateComposition.getLabelRoleName());
						userTask.setCandidateGroups(candidateGroups);

						List<CustomProperty> customPropertyList = buildCustomPropertyListWithComposition(templateComposition, false);
						userTask.setCustomProperties(customPropertyList);
					}
					if (null != templateComposition.getInspectionRoleName() ) {
						if (flowElement.getId().equals(inspectionTaskName)) {
							UserTask userTask = (UserTask) flowElement;
							userTask.setName(templateComposition.getCompositionName());
							List<String> candidateGroups = new ArrayList<>();
							candidateGroups.add(templateComposition.getInspectionRoleName());
							userTask.setCandidateGroups(candidateGroups);

							List<CustomProperty> customPropertyList = buildCustomPropertyListWithComposition(templateComposition, true);
							userTask.setCustomProperties(customPropertyList);
						}
					}
				});
				UserTask labelTask = (UserTask) flowElementMap.get(labelTaskName);
				labelTask.setName(templateComposition.getCompositionName());
				List<String> labelCandidateGroups = new ArrayList<>();
				labelCandidateGroups.add(templateComposition.getLabelRoleName());
				labelTask.setCandidateGroups(labelCandidateGroups);

				List<CustomProperty> customPropertyList = buildCustomPropertyListWithComposition(templateComposition, false);
				labelTask.setCustomProperties(customPropertyList);

				flowElementMap.remove(labelTaskName);
				flowElementMap.put(labelTask.getId(), labelTask);

				if (null != inspectionTaskName && null != templateComposition.getInspectionRoleName() ) {
					UserTask inspectionTask = (UserTask) flowElementMap.get(inspectionTaskName);
					inspectionTask.setName(templateComposition.getCompositionName());
					List<String> inspectionCandidateGroups = new ArrayList<>();
					inspectionCandidateGroups.add(templateComposition.getInspectionRoleName());
					inspectionTask.setCandidateGroups(inspectionCandidateGroups);

					List<CustomProperty> customPropertyList2 = buildCustomPropertyListWithComposition(templateComposition, true);
					inspectionTask.setCustomProperties(customPropertyList2);

					flowElementMap.remove(inspectionTaskName);
					flowElementMap.put(inspectionTask.getId(), inspectionTask);
				}
			}
		});
		return hasComposition.get();
	}
}
