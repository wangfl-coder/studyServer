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
package org.springblade.flow.core.feign;

import org.springblade.composition.dto.TemplateDTO;
import org.springblade.core.launch.constant.AppConstant;
import org.springblade.core.tool.api.R;
import org.springblade.flow.core.entity.BladeFlow;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 工作流远程调用接口.
 *
 * @author Chill
 */
@FeignClient(
	value = AppConstant.APPLICATION_FLOW_NAME
)
public interface IFlowEngineClient {

	String API_PREFIX = "/client";
	String DEPLOY_MODEL_BY_TEMPLATE = API_PREFIX + "/deploy-model-by-template";


	/**
	 * 通过模版部署流程
	 *
	 * @param templateDTO templateDTO
	 * @return R
	 */
	@PostMapping(DEPLOY_MODEL_BY_TEMPLATE)
	R<String> deployModelByTemplate(@RequestBody TemplateDTO templateDTO);
}
