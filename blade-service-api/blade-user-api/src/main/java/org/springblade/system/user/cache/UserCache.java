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
package org.springblade.system.user.cache;

import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.feign.IUserClient;

/**
 * 系统缓存
 *
 * @author Chill
 */
public class UserCache {
	private static final String USER_CACHE = "blade:user";
	private static final String USER_CACHE_ID_ = "user:id";


	private static IUserClient userClient;

	static {
		userClient = SpringUtil.getBean(IUserClient.class);
	}

	/**
	 * 获取用户名
	 *
	 * @param userId 用户id
	 * @return
	 */
	public static User getUser(Integer userId) {
		User user = CacheUtil.get(USER_CACHE, USER_CACHE_ID_ + userId, User.class);
		if (Func.isEmpty(user)) {
			R<User> result = userClient.userInfoById(userId);
			if (result.isSuccess()) {
				user = result.getData();
				if (Func.isNotEmpty(user)) {
					CacheUtil.put(USER_CACHE, USER_CACHE_ID_ + userId, user);
				}
			}
		}
		return user;
	}

}