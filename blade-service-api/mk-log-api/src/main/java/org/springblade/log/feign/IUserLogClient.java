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
package org.springblade.log.feign;


import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.tool.api.R;
import org.springblade.log.entity.UserLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * UserLog Feign接口类
 *
 * @author Chill
 */
@FeignClient(
	value = LauncherConstant.MKAPP_LOG_NAME
)
public interface IUserLogClient {

	String API_PREFIX = "/client";
	String USERLOG_BY_ID = API_PREFIX + "/userlog-info-by-id";
	String USERLOG_BY_ACCOUNT = API_PREFIX + "/userlog-info-by-account";
	String SAVE_USERLOG = API_PREFIX + "/save-userlog";
	String REMOVE_USERLOG = API_PREFIX + "/remove-userlog";

	/**
	 * 获取用户信息
	 *
	 * @param userId 用户id
	 * @return
	 */
	@GetMapping(USERLOG_BY_ID)
	R<UserLog> userInfoById(@RequestParam("userId") Long userId);

	/**
	 * 根据账号获取用户信息
	 *
	 * @param tenantId 租户id
	 * @param account  账号
	 * @return
	 */
	@GetMapping(USERLOG_BY_ACCOUNT)
	R<UserLog> userByAccount(@RequestParam("tenantId") String tenantId, @RequestParam("account") String account);

	/**
	 * 新建用户
	 *
	 * @param UserLog 用户实体
	 * @return
	 */
	@PostMapping(SAVE_USERLOG)
	R<Boolean> saveUserLog(@RequestBody UserLog UserLog);

	/**
	 * 删除用户
	 *
	 * @param tenantIds 租户id集合
	 * @return
	 */
	@PostMapping(REMOVE_USERLOG)
	R<Boolean> removeUserLog(@RequestParam("tenantIds") String tenantIds);

}
