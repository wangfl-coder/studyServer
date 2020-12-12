/*
 *      Copyright (c) 2018-2028, Chill Zhuang All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright expert,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  expert, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: Chill 庄骞 (smallchill@163.com)
 */
package org.springblade.adata.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.service.IExpertService;
import org.springblade.adata.vo.ExpertVO;
import org.springblade.adata.wrapper.ExpertWrapper;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Map;

import static org.springblade.core.cache.constant.CacheConstant.PARAM_CACHE;



/**
 * 控制器
 *
 * @author Chill
 */
@Slf4j
@NonDS
@RestController
@RequestMapping("expert")
@AllArgsConstructor
@Api(value = "学者接口", tags = "学者接口")
public class ExpertController extends BladeController {

	private final IExpertService expertService;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入expert")
	public R<ExpertVO> detail(Expert expert) {
		Expert detail = expertService.getOne(Condition.getQueryWrapper(expert));
		return R.data(ExpertWrapper.build().entityVO(detail));
	}

	/**
	 * 分页
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "category", value = "公告类型", paramType = "query", dataType = "integer"),
		@ApiImplicitParam(name = "title", value = "公告标题", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入expert")
	public R<IPage<ExpertVO>> list(@ApiIgnore @RequestParam Map<String, Object> expert, Query query) {
		IPage<Expert> pages = expertService.page(Condition.getPage(query), Condition.getQueryWrapper(expert, Expert.class));
		return R.data(ExpertWrapper.build().pageVO(pages));
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
//	@ApiOperation(value = "分页", notes = "传入expert")
//	public R<IPage<ExpertVO>> page(@ApiIgnore ExpertVO expert, Query query) {
//		IPage<ExpertVO> pages = expertService.selectNoticePage(Condition.getPage(query), expert);
//		return R.data(pages);
//	}

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入expert")
	public R save(@RequestBody Expert expert) {
		return R.status(expertService.save(expert));
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "修改", notes = "传入expert")
	public R update(@RequestBody Expert expert) {
		return R.status(expertService.updateById(expert));
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "新增或修改", notes = "传入expert")
	public R submit(@RequestBody Expert expert) {
		return R.status(expertService.saveOrUpdate(expert));
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "逻辑删除", notes = "传入expert")
	public R remove(@ApiParam(value = "主键集合") @RequestParam String ids) {
		boolean temp = expertService.deleteLogic(Func.toLongList(ids));
		return R.status(temp);
	}

	/**
	 * 详情
	 */
	@GetMapping("/fetch-detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入学者id")
	public R<String> fetchDetail(String id) {
		return R.data(expertService.fetchDetail(id));
	}

	/**
	 * 列表
	 */
	@GetMapping("/fetch-list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "ebId", value = "智库Id", paramType = "query", dataType = "string"),
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入expert")
	public R<String> fetchList(@ApiIgnore @RequestParam Map<String, Object> params, Query query) {
		return R.data(expertService.fetchList(params, query));
	}

	/**
	 * 导入
	 */
	@PostMapping("/import")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "导入", notes = "传入学者id,任务id")
	public R importDetail(String id, Long taskId) {
		return R.status(expertService.importDetail(id, taskId));
	}

	/**
	 * 导入智库下所有学者
	 */
	@PostMapping("/expert_base_import")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "导入", notes = "传入智库id,任务id")
	public R importExpertBase(String id, Long taskId) {
		return R.data(expertService.importExpertBase(id,taskId));
	}
}
