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
import org.springblade.composition.entity.LogBalance;
import org.springblade.composition.service.ILogBalanceService;
import org.springblade.composition.vo.LogBalanceVO;
import org.springblade.composition.wrapper.LogBalanceWrapper;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 余额日志 控制器
 *
 * @author BladeX
 * @since 2021-03-04
 */
@RestController
@AllArgsConstructor
@RequestMapping("/logbalance")
@Api(value = "余额日志", tags = "余额日志接口")
public class LogBalanceController extends BladeController {

	private final ILogBalanceService logBalanceService;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入logBalance")
	public R<LogBalanceVO> detail(LogBalance logBalance) {
		LogBalance detail = logBalanceService.getOne(Condition.getQueryWrapper(logBalance));
		return R.data(LogBalanceWrapper.build().entityVO(detail));
	}

	/**
	 * 分页 余额日志
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入logBalance")
	public R<IPage<LogBalanceVO>> list(LogBalance logBalance, Query query) {
		IPage<LogBalance> pages = logBalanceService.page(Condition.getPage(query), Condition.getQueryWrapper(logBalance));
		return R.data(LogBalanceWrapper.build().pageVO(pages));
	}


	/**
	 * 自定义分页 余额日志
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "分页", notes = "传入logBalance")
	public R<IPage<LogBalanceVO>> page(LogBalanceVO logBalance, Query query) {
		IPage<LogBalanceVO> pages = logBalanceService.selectLogBalancePage(Condition.getPage(query), logBalance);
		return R.data(pages);
	}

//	/**
//	 * 新增 余额日志
//	 */
//	@PostMapping("/save")
//	@ApiOperationSupport(order = 4)
//	@ApiOperation(value = "新增", notes = "传入logBalance")
//	public R save(@Valid @RequestBody LogBalance logBalance) {
//		return R.status(logBalanceService.save(logBalance));
//	}
//
//	/**
//	 * 修改 余额日志
//	 */
//	@PostMapping("/update")
//	@ApiOperationSupport(order = 5)
//	@ApiOperation(value = "修改", notes = "传入logBalance")
//	public R update(@Valid @RequestBody LogBalance logBalance) {
//		return R.status(logBalanceService.updateById(logBalance));
//	}
//
//	/**
//	 * 新增或修改 余额日志
//	 */
//	@PostMapping("/submit")
//	@ApiOperationSupport(order = 6)
//	@ApiOperation(value = "新增或修改", notes = "传入logBalance")
//	public R submit(@Valid @RequestBody LogBalance logBalance) {
//		return R.status(logBalanceService.saveOrUpdate(logBalance));
//	}
//
//
//	/**
//	 * 删除 余额日志
//	 */
//	@PostMapping("/remove")
//	@ApiOperationSupport(order = 7)
//	@ApiOperation(value = "逻辑删除", notes = "传入ids")
//	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
//		return R.status(logBalanceService.deleteLogic(Func.toLongList(ids)));
//	}


}
