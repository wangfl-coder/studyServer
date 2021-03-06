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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springblade.composition.entity.LogPoints;
import org.springblade.composition.service.ILogPointsService;
import org.springblade.composition.vo.LogPointsVO;
import org.springblade.composition.wrapper.LogPointsWrapper;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 积分日志 控制器
 *
 * @author BladeX
 * @since 2021-03-04
 */
@RestController
@AllArgsConstructor
@RequestMapping("/logpoints")
@Api(value = "积分日志", tags = "积分日志接口")
public class LogPointsController extends BladeController {

	private final ILogPointsService logPointsService;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入logPoints")
	public R<LogPointsVO> detail(LogPoints logPoints) {
		LogPoints detail = logPointsService.getOne(Condition.getQueryWrapper(logPoints));
		return R.data(LogPointsWrapper.build().entityVO(detail));
	}

	/**
	 * 分页 积分日志
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入logPoints")
	public R<IPage<LogPointsVO>> list(LogPoints logPoints, Query query) {
		IPage<LogPoints> pages = logPointsService.page(Condition.getPage(query), Condition.getQueryWrapper(logPoints));
		return R.data(LogPointsWrapper.build().pageVO(pages));
	}


	/**
	 * 自定义分页 积分日志
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "分页", notes = "传入logPoints")
	public R<IPage<LogPointsVO>> page(LogPointsVO logPoints, Query query) {
		IPage<LogPointsVO> pages = logPointsService.selectLogPointsPage(Condition.getPage(query), logPoints);
		return R.data(pages);
	}

//	/**
//	 * 新增 积分日志
//	 */
//	@PostMapping("/save")
//	@ApiOperationSupport(order = 4)
//	@ApiOperation(value = "新增", notes = "传入logPoints")
//	public R save(@Valid @RequestBody LogPoints logPoints) {
//		return R.status(logPointsService.save(logPoints));
//	}
//
//	/**
//	 * 修改 积分日志
//	 */
//	@PostMapping("/update")
//	@ApiOperationSupport(order = 5)
//	@ApiOperation(value = "修改", notes = "传入logPoints")
//	public R update(@Valid @RequestBody LogPoints logPoints) {
//		return R.status(logPointsService.updateById(logPoints));
//	}
//
//	/**
//	 * 新增或修改 积分日志
//	 */
//	@PostMapping("/submit")
//	@ApiOperationSupport(order = 6)
//	@ApiOperation(value = "新增或修改", notes = "传入logPoints")
//	public R submit(@Valid @RequestBody LogPoints logPoints) {
//		return R.status(logPointsService.saveOrUpdate(logPoints));
//	}
//
//
//	/**
//	 * 删除 积分日志
//	 */
//	@PostMapping("/remove")
//	@ApiOperationSupport(order = 7)
//	@ApiOperation(value = "逻辑删除", notes = "传入ids")
//	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
//		return R.status(logPointsService.deleteLogic(Func.toLongList(ids)));
//	}


}
