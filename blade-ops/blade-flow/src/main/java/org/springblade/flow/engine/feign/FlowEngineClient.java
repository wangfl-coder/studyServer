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
package org.springblade.flow.engine.feign;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springblade.composition.dto.TemplateDTO;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.feign.IFlowClient;
import org.springblade.flow.core.feign.IFlowEngineClient;
import org.springblade.flow.core.utils.TaskUtil;
import org.springblade.flow.engine.service.FlowEngineService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 流程远程调用实现类
 *
 * @author Chill
 */
@NonDS
@RestController
@RequiredArgsConstructor
public class FlowEngineClient implements IFlowEngineClient {

	private final FlowEngineService flowEngineService;

	/**
	 * 从模版部署流程的参考模型Id
	 */
	@Value("${blade.template-process.model-id}")
	private String modelId;

	/**
	 * 从模版部署流程的流程类型
	 */
	@Value("${blade.template-process.category}")
	private String category;

	@Override
	@PostMapping(DEPLOY_MODEL_BY_TEMPLATE)
	public R<String> deployModelByTemplate(@RequestBody TemplateDTO templateDTO) {
		String processDefinitionId = flowEngineService.deployModelByTemplate(modelId, category, null, templateDTO);
		return R.data(processDefinitionId);
	}
}
