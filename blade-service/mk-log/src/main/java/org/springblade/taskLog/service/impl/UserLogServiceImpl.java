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
package org.springblade.taskLog.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.taskLog.entity.UserLog;
import org.springblade.taskLog.mapper.UserLogMapper;
import org.springblade.taskLog.service.IUserLogService;
import org.springblade.system.cache.SysCache;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务实现类
 *
 * @author Chill
 */
@Service
@AllArgsConstructor
public class UserLogServiceImpl extends BaseServiceImpl<UserLogMapper, UserLog> implements IUserLogService {
	private static final String GUEST_NAME = "guest";



	@Override
	public IPage<UserLog> selectUserPage(IPage<UserLog> page, UserLog user, Long deptId, String tenantId) {
		List<Long> deptIdList = SysCache.getDeptChildIds(deptId);
		return page.setRecords(baseMapper.selectUserLogPage(page, user, deptIdList, tenantId));
	}

	@Override
	public UserLog getUserLogByAccount(String tenantId, String account) {
		return baseMapper.selectOne(Wrappers.<UserLog>lambdaQuery().eq(UserLog::getTenantId, tenantId).eq(UserLog::getAccount, account).eq(UserLog::getIsDeleted, BladeConstant.DB_NOT_DELETED));
	}

	@Override
	public UserLog getUserLogByEmail(String tenantId, String email) {
		return baseMapper.selectOne(Wrappers.<UserLog>lambdaQuery().eq(UserLog::getTenantId, tenantId).eq(UserLog::getEmail, email).eq(UserLog::getIsDeleted, BladeConstant.DB_NOT_DELETED));
	}

	@Override
	public UserLog getUserLogByMobile(String tenantId, String mobile) {
		return baseMapper.selectOne(Wrappers.<UserLog>lambdaQuery().eq(UserLog::getTenantId, tenantId).eq(UserLog::getMobile, mobile).eq(UserLog::getIsDeleted, BladeConstant.DB_NOT_DELETED));
	}

}
