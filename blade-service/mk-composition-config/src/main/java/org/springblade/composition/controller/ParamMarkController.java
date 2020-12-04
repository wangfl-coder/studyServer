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
package org.springblade.composition.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springblade.composition.entity.ParamMark;
import org.springblade.composition.service.IParamMarkService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Map;

/**
 * 控制器
 *
 * @author KaiLun
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/param-mark")
@Api(value = "标注配置参数管理", tags = "标注配置")
public class ParamMarkController extends BladeController {

	private final IParamMarkService paramMarkService;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入param")
	public R<ParamMark> detail(ParamMark param) {
		ParamMark detail = paramMarkService.getOne(Condition.getQueryWrapper(param));
		return R.data(detail);
	}

	/**
	 * 分页
	 * 暂时没有分页需求
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "paramName", value = "参数名称", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "paramKey", value = "参数键名", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "paramValue", value = "参数键值", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入param")
	public R<IPage<ParamMark>> list(@ApiIgnore @RequestParam Map<String, Object> param, Query query) {
		IPage<ParamMark> pages = paramMarkService.page(Condition.getPage(query), Condition.getQueryWrapper(param, ParamMark.class));
		return R.data(pages);
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "新增或修改", notes = "传入param")
	public R submit(@Valid @RequestBody ParamMark param) {
//		CacheUtil.clear(PARAM_CACHE);
//		CacheUtil.clear(PARAM_CACHE, Boolean.FALSE);
		return R.status(paramMarkService.saveOrUpdate(param));
	}


	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "逻辑删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
//		CacheUtil.clear(PARAM_CACHE);
//		CacheUtil.clear(PARAM_CACHE, Boolean.FALSE);
		return R.status(paramMarkService.deleteLogic(Func.toLongList(ids)));
	}


}
