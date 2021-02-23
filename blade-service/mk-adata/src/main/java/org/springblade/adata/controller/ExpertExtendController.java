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
package org.springblade.adata.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.AminerUser;
import org.springblade.adata.entity.ExpertExtend;
import org.springblade.adata.magic.ExpertExtendRequest;
import org.springblade.adata.magic.MagicRequest;
import org.springblade.adata.service.IExpertExtendService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.constant.SecureConstant;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.core.tool.utils.WebUtil;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Map;
import java.util.Objects;

import static org.springblade.core.cache.constant.CacheConstant.PARAM_CACHE;

/**
 * 控制器
 *
 * @author Chill
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/expert-extend")
@Api(value = "学者扩展接口", tags = "学者扩展接口")
public class ExpertExtendController extends BladeController {

	private final IExpertExtendService expertExtendService;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入param")
	public R detail(ExpertExtend expertExtend) {
		AminerUser user = ExpertExtendRequest.getUser();
		if (user == null)
			return R.data(401, new JSONObject(), "鉴权失败");
		ExpertExtend detail = (ExpertExtend) expertExtendService.getOne(Condition.getQueryWrapper(expertExtend));
		return R.data(detail);
	}

	/**
	 * 分页
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "paramName", value = "参数名称", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "paramKey", value = "参数键名", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "paramValue", value = "参数键值", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入param")
	public R<IPage<ExpertExtend>> list(@ApiIgnore @RequestParam Map<String, Object> param, Query query) {
		IPage<ExpertExtend> pages = expertExtendService.page(Condition.getPage(query), Condition.getQueryWrapper(param, ExpertExtend.class));
		return R.data(pages);
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "新增或修改", notes = "传入ExpertExtend")
	public R submit(@Valid @RequestBody ExpertExtend expertExtend) {
//		CacheUtil.clear(PARAM_CACHE);
//		CacheUtil.clear(PARAM_CACHE, Boolean.FALSE);
		AminerUser user = ExpertExtendRequest.getUser();
		if (user == null)
			return R.data(401, new JSONObject(), "鉴权失败");
		boolean res = expertExtendService.saveOrUpdate(expertExtend);
		return R.status(res);
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
		return R.status(expertExtendService.deleteLogic(Func.toLongList(ids)));
	}



}
