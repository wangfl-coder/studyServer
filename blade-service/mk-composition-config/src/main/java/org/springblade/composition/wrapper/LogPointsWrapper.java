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
package org.springblade.composition.wrapper;

import org.springblade.composition.entity.LogPoints;
import org.springblade.composition.vo.LogPointsVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;

import java.util.Objects;

/**
 * 用户表包装类,返回视图层所需的字段
 *
 * @author BladeX
 * @since 2021-03-04
 */
public class LogPointsWrapper extends BaseEntityWrapper<LogPoints, LogPointsVO>  {

	public static LogPointsWrapper build() {
		return new LogPointsWrapper();
 	}

	@Override
	public LogPointsVO entityVO(LogPoints logPoints) {
		LogPointsVO logPointsVO = Objects.requireNonNull(BeanUtil.copy(logPoints, LogPointsVO.class));

		//User createUser = UserCache.getUser(logPoints.getCreateUser());
		//User updateUser = UserCache.getUser(logPoints.getUpdateUser());
		//logPointsVO.setCreateUserName(createUser.getName());
		//logPointsVO.setUpdateUserName(updateUser.getName());

		return logPointsVO;
	}

}
