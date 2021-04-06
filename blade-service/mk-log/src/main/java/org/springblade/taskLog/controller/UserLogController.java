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
package org.springblade.taskLog.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.annotation.PreAuth;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.constant.RoleConstant;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.taskLog.entity.UserLog;
import org.springblade.taskLog.service.IUserLogService;
import org.springblade.taskLog.vo.UserLogVO;
import org.springblade.taskLog.wrapper.UserLogWrapper;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Map;

/**
 * 控制器
 *
 * @author Chill
 */
@NonDS
@RestController
@RequestMapping("/user-log")
@AllArgsConstructor
public class UserLogController {

	private final IUserLogService userLogService;

	/**
	 * 查询单条
	 */
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "查看详情", notes = "传入id")
	@GetMapping("/detail")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<UserLogVO> detail(UserLog user) {
		UserLog detail = userLogService.getOne(Condition.getQueryWrapper(user));
		return R.data(UserLogWrapper.build().entityVO(detail));
	}

	/**
	 * 查询单条
	 */
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "查看详情", notes = "传入id")
	@GetMapping("/info")
	public R<UserLogVO> info(BladeUser user) {
		UserLog detail = userLogService.getById(user.getUserId());
		return R.data(UserLogWrapper.build().entityVO(detail));
	}

	/**
	 * 用户列表
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "account", value = "账号名", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "realName", value = "姓名", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "列表", notes = "传入account和realName")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<IPage<UserLogVO>> list(@ApiIgnore @RequestParam Map<String, Object> user, Query query, BladeUser bladeUser) {
		QueryWrapper<UserLog> queryWrapper = Condition.getQueryWrapper(user, UserLog.class);
		IPage<UserLog> pages = userLogService.page(Condition.getPage(query), (!bladeUser.getTenantId().equals(BladeConstant.ADMIN_TENANT_ID)) ? queryWrapper.lambda().eq(UserLog::getTenantId, bladeUser.getTenantId()) : queryWrapper);
		return R.data(UserLogWrapper.build().pageVO(pages));
	}

	/**
	 * 自定义用户列表
	 */
	@GetMapping("/page")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "account", value = "账号名", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "realName", value = "姓名", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "列表", notes = "传入account和realName")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<IPage<UserLogVO>> page(@ApiIgnore UserLog user, Query query, Long deptId, BladeUser bladeUser) {
		IPage<UserLog> pages = userLogService.selectUserPage(Condition.getPage(query), user, deptId, (bladeUser.getTenantId().equals(BladeConstant.ADMIN_TENANT_ID) ? StringPool.EMPTY : bladeUser.getTenantId()));
		return R.data(UserLogWrapper.build().pageVO(pages));
	}

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入UserLog")
	public R save(@Valid @RequestBody UserLog taskLog) {
		return R.status(userLogService.save(taskLog));
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "修改", notes = "传入UserLog")
	public R update(@Valid @RequestBody UserLog userLog) {
//		CacheUtil.clear(USER_CACHE);
		return R.status(userLogService.updateById(userLog));
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增或修改", notes = "传入UserLog")
	public R submit(@Valid @RequestBody UserLog user) {
//		CacheUtil.clear(USER_CACHE);
		return R.status(userLogService.saveOrUpdate(user));
	}


	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "删除", notes = "传入id集合")
	public R remove(@RequestParam String ids) {
//		CacheUtil.clear(USER_CACHE);
		return R.status(userLogService.deleteLogic(Func.toLongList(ids)));
	}

}
