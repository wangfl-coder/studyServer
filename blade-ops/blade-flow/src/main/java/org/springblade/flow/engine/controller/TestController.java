package org.springblade.flow.engine.controller;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.*;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("flowable")
@RestController
public class TestController {

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private HistoryService historyService;

	@Autowired
	private RepositoryService repositoryService;

	@Qualifier("processEngine")
	@Autowired
	private ProcessEngine processEngine;

	@GetMapping("test")
	public String createProcess(){
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("task");

		return "创建流程成功，流程ID为：" + processInstance.getId();
	}

}
