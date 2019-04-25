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
package org.springblade.desk.controller;

import lombok.AllArgsConstructor;
import org.springblade.common.cache.CacheNames;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tool.api.R;
import org.springblade.desk.entity.ProcessLeave;
import org.springblade.desk.service.ILeaveService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 控制器
 *
 * @author Chill
 */
@ApiIgnore
@RestController
@RequestMapping("/process/leave")
@AllArgsConstructor
public class LeaveController extends BladeController implements CacheNames {

	private ILeaveService leaveService;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	public R<ProcessLeave> detail(ProcessLeave leave) {
		ProcessLeave detail = leaveService.getOne(Condition.getQueryWrapper(leave));
		return R.data(detail);
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/start")
	public R start(@RequestBody ProcessLeave leave) {
		return R.status(leaveService.start(leave));
	}


}
