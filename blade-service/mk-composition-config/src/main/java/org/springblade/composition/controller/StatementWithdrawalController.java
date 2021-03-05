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
import org.springblade.composition.entity.StatementWithdrawal;
import org.springblade.composition.service.IStatementWithdrawalService;
import org.springblade.composition.vo.StatementWithdrawalVO;
import org.springblade.composition.wrapper.StatementWithdrawalWrapper;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 提现单 控制器
 *
 * @author BladeX
 * @since 2021-03-04
 */
@RestController
@AllArgsConstructor
@RequestMapping("/statementwithdrawal")
@Api(value = "提现单", tags = "提现单接口")
public class StatementWithdrawalController extends BladeController {

	private final IStatementWithdrawalService statementWithdrawalService;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入statementWithdrawal")
	public R<StatementWithdrawalVO> detail(StatementWithdrawal statementWithdrawal) {
		StatementWithdrawal detail = statementWithdrawalService.getOne(Condition.getQueryWrapper(statementWithdrawal));
		return R.data(StatementWithdrawalWrapper.build().entityVO(detail));
	}

	/**
	 * 分页 提现单
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入statementWithdrawal")
	public R<IPage<StatementWithdrawalVO>> list(StatementWithdrawal statementWithdrawal, Query query) {
		IPage<StatementWithdrawal> pages = statementWithdrawalService.page(Condition.getPage(query), Condition.getQueryWrapper(statementWithdrawal));
		return R.data(StatementWithdrawalWrapper.build().pageVO(pages));
	}


	/**
	 * 自定义分页 提现单
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "分页", notes = "传入statementWithdrawal")
	public R<IPage<StatementWithdrawalVO>> page(StatementWithdrawalVO statementWithdrawal, Query query) {
		IPage<StatementWithdrawalVO> pages = statementWithdrawalService.selectStatementWithdrawalPage(Condition.getPage(query), statementWithdrawal);
		return R.data(pages);
	}

	/**
	 * 新增 提现单
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入statementWithdrawal")
	public R save(@Valid @RequestBody StatementWithdrawal statementWithdrawal) {
		return R.status(statementWithdrawalService.save(statementWithdrawal));
	}

	/**
	 * 修改 提现单
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "修改", notes = "传入statementWithdrawal")
	public R update(@Valid @RequestBody StatementWithdrawal statementWithdrawal) {
		return R.status(statementWithdrawalService.updateById(statementWithdrawal));
	}

	/**
	 * 新增或修改 提现单
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "新增或修改", notes = "传入statementWithdrawal")
	public R submit(@Valid @RequestBody StatementWithdrawal statementWithdrawal) {
		return R.status(statementWithdrawalService.saveOrUpdate(statementWithdrawal));
	}


	/**
	 * 删除 提现单
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "逻辑删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		return R.status(statementWithdrawalService.deleteLogic(Func.toLongList(ids)));
	}


}
