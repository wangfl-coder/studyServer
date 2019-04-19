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
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.flowable.engine.entity.FlowModel;
import org.springblade.flowable.engine.service.FlowService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

/**
 * 流程模型控制器
 *
 * @author Chill
 */
@RestController
@RequestMapping("model")
@AllArgsConstructor
public class FlowModelController {

	private FlowService flowService;

	/**
	 * 分页
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "modelKey", value = "模型标识", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "name", value = "模型名称", paramType = "query", dataType = "string")
	})
	@ApiOperation(value = "分页", notes = "传入notice", position = 1)
	public R<IPage<FlowModel>> list(@ApiIgnore @RequestParam Map<String, Object> flow, Query query) {
		IPage<FlowModel> pages = flowService.page(Condition.getPage(query), Condition.getQueryWrapper(flow, FlowModel.class));
		return R.data(pages);
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperation(value = "删除", notes = "传入主键集合", position = 2)
	public R remove(@ApiParam(value = "主键集合") @RequestParam String ids) {
		boolean temp = flowService.removeByIds(Func.toStrList(ids));
		return R.status(temp);
	}

	/**
	 * 部署
	 */
	@PostMapping("/deploy")
	@ApiOperation(value = "部署", notes = "传入模型id和分类", position = 3)
	public R deploy(@ApiParam(value = "模型id") @RequestParam String modelId, @ApiParam(value = "工作流分类") @RequestParam String category) {
		boolean temp = flowService.deployModel(modelId, category);
		return R.status(temp);
	}

}
