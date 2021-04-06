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
package org.springblade.taskLog.cache;

import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.core.tool.utils.StringPool;
import org.springblade.taskLog.entity.UserLog;
import org.springblade.taskLog.feign.IUserLogClient;

/**
 * 系统缓存
 *
 * @author Chill
 */
public class UserLogCache {
	private static final String USERLOG_CACHE = "mk:userlog";
	private static final String USERLOG_CACHE_ID = "userlog:id:";
	private static final String USERLOG_CACHE_ACCOUNT = "userlog:account:";

	private static IUserLogClient userClient;

	private static IUserLogClient getUserClient() {
		if (userClient == null) {
			userClient = SpringUtil.getBean(IUserLogClient.class);
		}
		return userClient;
	}

	/**
	 * 根据任务用户id获取用户信息
	 *
	 * @param taskUserId 任务用户id
	 * @return
	 */
//	public static UserLog getUserByTaskUser(String taskUserId) {
//		Long userId = Func.toLong(StringUtil.removePrefix(taskUserId, TASK_USR_PREFIX));
//		return getUser(userId);
//	}

	/**
	 * 获取用户
	 *
	 * @param userId 用户id
	 * @return
	 */
	public static UserLog getUserLog(Long userId) {
		return CacheUtil.get(USERLOG_CACHE, USERLOG_CACHE_ID, userId, () -> {
			R<UserLog> result = getUserClient().userInfoById(userId);
			return result.getData();
		});
	}

	/**
	 * 获取用户
	 *
	 * @param tenantId 租户id
	 * @param account  账号名
	 * @return
	 */
	public static UserLog getUserLog(String tenantId, String account) {
		return CacheUtil.get(USERLOG_CACHE, USERLOG_CACHE_ACCOUNT, tenantId + StringPool.DASH + account, () -> {
			R<UserLog> result = getUserClient().userByAccount(tenantId, account);
			return result.getData();
		});
	}

}
