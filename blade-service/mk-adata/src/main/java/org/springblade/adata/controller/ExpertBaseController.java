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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.adata.entity.ExpertBase;
import org.springblade.adata.magic.MagicRequest;
import org.springblade.adata.service.IExpertBaseService;
import org.springblade.adata.vo.ExpertBaseVO;
import org.springblade.adata.wrapper.ExpertBaseWrapper;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.Func;
import org.springblade.system.cache.DictCache;
import org.springblade.system.enums.DictEnum;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static org.springblade.core.cache.constant.CacheConstant.SYS_CACHE;

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
	@GetMapping("/remote-detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入智库id")
	public R<String> detail(String id) {
		return expertBaseService.detail(id);
	}

	/**
	 * 列表
	 */
	@GetMapping("/remote-list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "category", value = "公告类型", paramType = "query", dataType = "integer"),
		@ApiImplicitParam(name = "title", value = "公告标题", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入expertBase")
	public R<String> list(@ApiIgnore @RequestParam Map<String, Object> params, Query query) {
		return expertBaseService.list(params, query);
	}

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入expertBase")
	public R<ExpertBaseVO> detail(ExpertBase expertBase) {
		ExpertBase detail = expertBaseService.getOne(Condition.getQueryWrapper(expertBase));
		return R.data(ExpertBaseWrapper.build().entityVO(detail));
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "expertBaseName", value = "部门名称", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "fullName", value = "部门全称", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "列表", notes = "传入expertBase")
	public R<List<ExpertBaseVO>> list(@ApiIgnore @RequestParam Map<String, Object> expertBase, BladeUser bladeUser) {
		QueryWrapper<ExpertBase> queryWrapper = Condition.getQueryWrapper(expertBase, ExpertBase.class);
		List<ExpertBase> list = expertBaseService.list((!bladeUser.getTenantId().equals(BladeConstant.ADMIN_TENANT_ID)) ? queryWrapper.lambda().eq(ExpertBase::getTenantId, bladeUser.getTenantId()) : queryWrapper);
		return R.data(ExpertBaseWrapper.build().listNodeVO(list));
	}

	/**
	 * 懒加载列表
	 */
	@GetMapping("/lazy-list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "expertBaseName", value = "部门名称", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "fullName", value = "部门全称", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "懒加载列表", notes = "传入expertBase")
	public R<List<ExpertBaseVO>> lazyList(@ApiIgnore @RequestParam Map<String, Object> expertBase, Long parentId, BladeUser bladeUser) {
		List<ExpertBaseVO> list = expertBaseService.lazyList(bladeUser.getTenantId(), parentId, expertBase);
		return R.data(ExpertBaseWrapper.build().listNodeLazyVO(list));
	}

	/**
	 * 获取部门树形结构
	 *
	 * @return
	 */
	@GetMapping("/tree")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "树形结构", notes = "树形结构")
	public R<List<ExpertBaseVO>> tree(String tenantId, BladeUser bladeUser) {
		List<ExpertBaseVO> tree = expertBaseService.tree(Func.toStrWithEmpty(tenantId, bladeUser.getTenantId()));
		return R.data(tree);
	}

	/**
	 * 懒加载获取部门树形结构
	 */
	@GetMapping("/lazy-tree")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "懒加载树形结构", notes = "树形结构")
	public R<List<ExpertBaseVO>> lazyTree(String tenantId, Long parentId, BladeUser bladeUser) {
		List<ExpertBaseVO> tree = expertBaseService.lazyTree(Func.toStrWithEmpty(tenantId, bladeUser.getTenantId()), parentId);
		return R.data(tree);
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "新增或修改", notes = "传入expertBase")
	public R submit(@Valid @RequestBody ExpertBase expertBase) {
		if (expertBaseService.submit(expertBase)) {
			CacheUtil.clear(SYS_CACHE);
			// 返回懒加载树更新节点所需字段
			Kv kv = Kv.create().set("id", String.valueOf(expertBase.getId())).set("tenantId", expertBase.getTenantId())
				.set("expertBaseCategoryName", DictCache.getValue(DictEnum.ORG_CATEGORY, expertBase.getExpertBaseCategory()));
			return R.data(kv);
		}
		return R.fail("操作失败");
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		CacheUtil.clear(SYS_CACHE);
		return R.status(expertBaseService.removeExpertBase(ids));
	}

}
