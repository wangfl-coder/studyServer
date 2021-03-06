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
package org.springblade.system.user.feign;


import org.springblade.core.launch.constant.AppConstant;
import org.springblade.core.tool.api.R;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.entity.UserInfo;
import org.springblade.system.user.entity.UserOauth;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * User Feign接口类
 *
 * @author Chill
 */
@FeignClient(
	value = AppConstant.APPLICATION_USER_NAME
)
public interface IUserClient {

	String API_PREFIX = "/client";
	String USER_INFO = API_PREFIX + "/user-info";
	String USER_INFO_BY_TYPE = API_PREFIX + "/user-info-by-type";
	String USER_INFO_BY_ID = API_PREFIX + "/user-info-by-id";
	String USER_INFO_BY_ACCOUNT = API_PREFIX + "/user-info-by-account";
	String GET_USER_INFO_BY_ACCOUNT = API_PREFIX + "/get-user-info-by-account";
	String GET_USER_INFO_BY_MOBILE = API_PREFIX + "/get-user-info-by-mobile";
	String GET_USER_INFO_BY_EMAIL = API_PREFIX + "/get-user-info-by-email";
	String USER_AUTH_INFO = API_PREFIX + "/user-auth-info";
	String SAVE_USER = API_PREFIX + "/save-user";
	String UPDATE_USER = API_PREFIX + "/update-user";
	String REMOVE_USER = API_PREFIX + "/remove-user";

	/**
	 * 获取用户信息
	 *
	 * @param userId 用户id
	 * @return
	 */
	@GetMapping(USER_INFO_BY_ID)
	R<User> userInfoById(@RequestParam("userId") Long userId);


	/**
	 * 根据账号获取用户信息
	 *
	 * @param tenantId 租户id
	 * @param account  账号
	 * @return
	 */
	@GetMapping(USER_INFO_BY_ACCOUNT)
	R<User> userByAccount(@RequestParam("tenantId") String tenantId, @RequestParam("account") String account);

	/**
	 * 获取用户信息
	 *
	 * @param tenantId 租户ID
	 * @param account  账号
	 * @return
	 */
	@GetMapping(USER_INFO)
	R<UserInfo> userInfo(@RequestParam("tenantId") String tenantId, @RequestParam("account") String account);

	/**
	 * 获取用户信息
	 *
	 * @param tenantId 租户ID
	 * @param account  账号
	 * @param userType 用户平台
	 * @return
	 */
	@GetMapping(USER_INFO_BY_TYPE)
	R<UserInfo> userInfo(@RequestParam("tenantId") String tenantId, @RequestParam("account") String account, @RequestParam("userType") String userType);

	/**
	 * Get user info
	 *
	 * @param tenantId Tenant Id
	 * @param account  account
	 * @param userType 用户平台
	 * @return
	 */
	@GetMapping(GET_USER_INFO_BY_ACCOUNT)
	R<UserInfo> getUserInfoByAccount(@RequestParam("tenantId") String tenantId, @RequestParam("account") String account, @RequestParam("userType") String userType);

	/**
	 * Get user info
	 *
	 * @param tenantId Tenant Id
	 * @param mobile   mobile
	 * @param userType 用户平台
	 * @return
	 */
	@GetMapping(GET_USER_INFO_BY_MOBILE)
	R<UserInfo> getUserInfoByMobile(@RequestParam("tenantId") String tenantId, @RequestParam("mobile") String mobile, @RequestParam("userType") String userType);

	/**
	 * Get user info
	 *
	 * @param tenantId Tenant Id
	 * @param email    email
	 * @param userType 用户平台
	 * @return
	 */
	@GetMapping(GET_USER_INFO_BY_EMAIL)
	R<UserInfo> getUserInfoByEmail(@RequestParam("tenantId") String tenantId, @RequestParam("email") String email, @RequestParam("userType") String userType);

	/**
	 * 获取第三方平台信息
	 *
	 * @param userOauth 第三方授权用户信息
	 * @return UserInfo
	 */
	@PostMapping(USER_AUTH_INFO)
	R<UserInfo> userAuthInfo(@RequestBody UserOauth userOauth);

	/**
	 * 新建用户
	 *
	 * @param user 用户实体
	 * @return
	 */
	@PostMapping(SAVE_USER)
	R<Boolean> saveUser(@RequestBody User user);

	/**
	 * 修改用户
	 *
	 * @param user 用户实体
	 * @return
	 */
	@PostMapping(UPDATE_USER)
	R<Boolean> updateUser(@RequestBody User user);

	/**
	 * 删除用户
	 *
	 * @param tenantIds 租户id集合
	 * @return
	 */
	@PostMapping(REMOVE_USER)
	R<Boolean> removeUser(@RequestParam("tenantIds") String tenantIds);

}
