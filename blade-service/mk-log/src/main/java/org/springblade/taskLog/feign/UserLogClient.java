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
package org.springblade.taskLog.feign;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.taskLog.entity.UserLog;
import org.springblade.taskLog.service.IUserLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户服务Feign实现类
 *
 * @author Chill
 */
@NonDS
@RestController
@AllArgsConstructor
public class UserLogClient implements IUserLogClient {

	private final IUserLogService service;

	@Override
	@GetMapping(USERLOG_BY_ID)
	public R<UserLog> userInfoById(Long userId) {
		return R.data(service.getById(userId));
	}

	@Override
	@GetMapping(USERLOG_BY_ACCOUNT)
	public R<UserLog> userByAccount(String tenantId, String account) {
		return R.data(service.getUserLogByAccount(tenantId, account));
	}

	@Override
	@PostMapping(SAVE_USERLOG)
	public R<Boolean> saveUserLog(@RequestBody UserLog UserLog) {
		return R.data(service.save(UserLog));
	}

	@Override
	@PostMapping(REMOVE_USERLOG)
	public R<Boolean> removeUserLog(String tenantIds) {
		return R.data(service.remove(Wrappers.<UserLog>query().lambda().in(UserLog::getTenantId, Func.toStrList(tenantIds))));
	}

}
