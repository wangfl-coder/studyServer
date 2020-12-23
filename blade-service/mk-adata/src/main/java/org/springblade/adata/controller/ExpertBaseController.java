/*
 *      Copyright (c) 2018-2028, Chill Zhuang All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright expertBase,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  expertBase, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: Chill 庄骞 (smallchill@163.com)
 */
package org.springblade.adata.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.adata.entity.ExpertBase;
import org.springblade.adata.magic.MagicRequest;
import org.springblade.adata.service.IExpertBaseService;
import org.springblade.adata.vo.ExpertBaseVO;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

/**
 * 控制器
 *
 * @author Chill
 */
@Slf4j
@NonDS
@RestController
@RequestMapping("/expertbase")
@AllArgsConstructor
@Api(value = "智库接口", tags = "智库接口")
public class ExpertBaseController extends BladeController {

	private final IExpertBaseService expertBaseService;
//
//	private final IExpertBaseClient expertBaseClient;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入智库id")
	public R<String> detail(String id) {
		return expertBaseService.detail(id);
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "category", value = "公告类型", paramType = "query", dataType = "integer"),
		@ApiImplicitParam(name = "title", value = "公告标题", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入expertBase")
	public R<String> list(@ApiIgnore @RequestParam Map<String, Object> params, Query query) {
		return expertBaseService.list(params, query);
	}

//	/**
//	 * 多表联合查询自定义分页
//	 */
//	@GetMapping("/page")
//	@ApiImplicitParams({
//		@ApiImplicitParam(name = "category", value = "公告类型", paramType = "query", dataType = "integer"),
//		@ApiImplicitParam(name = "title", value = "公告标题", paramType = "query", dataType = "string")
//	})
//	@ApiOperationSupport(order = 3)
//	@ApiOperation(value = "分页", notes = "传入expertBase")
//	public R<IPage<ExpertBaseVO>> page(@ApiIgnore ExpertBaseVO expertBase, Query query) {
//		IPage<ExpertBaseVO> pages = expertBaseService.selectExpertBasePage(Condition.getPage(query), expertBase);
//		return R.data(pages);
//	}

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入expertBase")
	public R save(@RequestBody ExpertBase expertBase) {
		return R.status(expertBaseService.save(expertBase));
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "修改", notes = "传入expertBase")
	public R update(@RequestBody ExpertBase expertBase) {
		return R.status(expertBaseService.updateById(expertBase));
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "新增或修改", notes = "传入expertBase")
	public R submit(@RequestBody ExpertBase expertBase) {
		return R.status(expertBaseService.saveOrUpdate(expertBase));
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "逻辑删除", notes = "传入expertBase")
	public R remove(@ApiParam(value = "主键集合") @RequestParam String ids) {
		boolean temp = expertBaseService.deleteLogic(Func.toLongList(ids));
		return R.status(temp);
	}

//	/**
//	 * 远程调用分页接口
//	 */
//	@GetMapping("/top")
//	@ApiOperationSupport(order = 8)
//	@ApiOperation(value = "分页远程调用", notes = "传入current,size")
//	public R<BladePage<ExpertBase>> top(@ApiParam(value = "当前页") @RequestParam Integer current, @ApiParam(value = "每页显示条数") @RequestParam Integer size) {
//		BladePage<ExpertBase> page = expertBaseClient.top(current, size);
//		return R.data(page);
//	}

}
