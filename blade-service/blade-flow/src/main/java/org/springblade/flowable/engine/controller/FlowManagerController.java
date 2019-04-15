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
package org.springblade.flowable.engine.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.flowable.engine.entity.FlowProcess;
import org.springblade.flowable.engine.service.FlowService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程管理接口
 *
 * @author Chill
 */
@RestController
@RequestMapping("manager")
@AllArgsConstructor
public class FlowManagerController {

	private FlowService flowService;

	/**
	 * 分页
	 */
	@GetMapping("/list")
	@ApiOperation(value = "分页", notes = "传入notice", position = 1)
	public R<IPage<FlowProcess>> list(@ApiParam("流程类型") String category, Query query) {
		IPage<FlowProcess> pages = flowService.selectManagerPage(Condition.getPage(query), category);
		return R.data(pages);
	}

}
