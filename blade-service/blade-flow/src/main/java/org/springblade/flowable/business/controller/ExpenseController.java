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
package org.springblade.flowable.business.controller;

import lombok.AllArgsConstructor;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 报销控制器
 *
 * @author Chill
 */
@Controller
@AllArgsConstructor
@RequestMapping(value = "expense")
public class ExpenseController {

	private RuntimeService runtimeService;

	private TaskService taskService;

	private RepositoryService repositoryService;

	private ProcessEngine processEngine;

	/**
	 * 添加报销
	 *
	 * @param userId    用户Id
	 * @param money     报销金额
	 * @param descption 描述
	 */
	@RequestMapping(value = "add")
	@ResponseBody
	public String addExpense(String userId, Integer money, String descption) {
		//启动流程
		HashMap<String, Object> map = new HashMap<>();
		map.put("taskUser", userId);
		map.put("money", money);
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Expense", map);
		return "提交成功.流程Id为：" + processInstance.getId();
	}

	/**
	 * 获取审批管理列表
	 */
	@RequestMapping(value = "/list")
	@ResponseBody
	public Object list(String userId) {
		List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc().list();
		for (Task task : tasks) {
			System.out.println(task.toString());
		}
		return tasks.toArray().toString();
	}

	/**
	 * 批准
	 *
	 * @param taskId 任务ID
	 */
	@RequestMapping(value = "apply")
	@ResponseBody
	public String apply(String taskId) {
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		if (task == null) {
			throw new RuntimeException("流程不存在");
		}
		//通过审核
		HashMap<String, Object> map = new HashMap<>();
		map.put("outcome", "通过");
		taskService.complete(taskId, map);
		return "processed ok!";
	}

	/**
	 * 拒绝
	 */
	@ResponseBody
	@RequestMapping(value = "reject")
	public String reject(String taskId) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("outcome", "驳回");
		taskService.complete(taskId, map);
		return "reject";
	}

	/**
	 * 生成流程图
	 *
	 * @param processId 任务ID
	 */
	@RequestMapping(value = "processDiagram")
	public void genProcessDiagram(HttpServletResponse httpServletResponse, String processId) throws Exception {
		ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();

		//流程走完的不显示图
		if (pi == null) {
			return;
		}
		Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
		//使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
		String instanceId = task.getProcessInstanceId();
		List<Execution> executions = runtimeService
			.createExecutionQuery()
			.processInstanceId(instanceId)
			.list();

		//得到正在执行的Activity的Id
		List<String> activityIds = new ArrayList<>();
		List<String> flows = new ArrayList<>();
		for (Execution exe : executions) {
			List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
			activityIds.addAll(ids);
		}

		//获取流程图
		BpmnModel bpmnModel = repositoryService.getBpmnModel(pi.getProcessDefinitionId());
		ProcessEngineConfiguration engconf = processEngine.getProcessEngineConfiguration();
		ProcessDiagramGenerator diagramGenerator = engconf.getProcessDiagramGenerator();
		InputStream in = diagramGenerator.generateDiagram(bpmnModel, "png", activityIds, flows, engconf.getActivityFontName(), engconf.getLabelFontName(), engconf.getAnnotationFontName(), engconf.getClassLoader(), 1.0, true);
		OutputStream out = null;
		byte[] buf = new byte[1024];
		int legth = 0;
		try {
			out = httpServletResponse.getOutputStream();
			while ((legth = in.read(buf)) != -1) {
				out.write(buf, 0, legth);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}
}