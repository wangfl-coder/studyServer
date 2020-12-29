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
package org.springblade.task.wrapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.node.ForestNodeMerger;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.system.cache.DictCache;
import org.springblade.system.enums.DictEnum;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.vo.UserVO;
import org.springblade.task.entity.Task;
import org.springblade.task.vo.TaskVO;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 包装类,返回视图层所需的字段
 *
 * @author Chill
 */
public class TaskWrapper extends BaseEntityWrapper<Task, TaskVO> {

	public static TaskWrapper build() {
		return new TaskWrapper();
	}

	@Override
	public TaskVO entityVO(Task task) {
		TaskVO taskVO = Objects.requireNonNull(BeanUtil.copy(task, TaskVO.class));
//		Integer userCount = selectCount(Wrappers.<User>query().lambda().eq(User::getTenantId, tenantId).eq(User::getAccount, user.getAccount()));
//		Integer userCount = baseMapper.selectCount(Wrappers.<User>query().lambda().eq(User::getTenantId, tenantId).eq(User::getAccount, user.getAccount()));
		taskVO.setTotal(233);
		taskVO.setCompleted(42);
		return taskVO;
//		UserVO userVO = Objects.requireNonNull(BeanUtil.copy(user, UserVO.class));
//		Tenant tenant = SysCache.getTenant(user.getTenantId());
//		List<String> TaskName = SysCache.getTaskNames(user.getTaskId());
//		List<String> deptName = SysCache.getDeptNames(user.getDeptId());
//		List<String> postName = SysCache.getPostNames(user.getPostId());
//		userVO.setTenantName(tenant.getTenantName());
//		userVO.setTaskName(Func.join(TaskName));
//		userVO.setDeptName(Func.join(deptName));
//		userVO.setPostName(Func.join(postName));
//		userVO.setSexName(DictCache.getValue(DictEnum.SEX, user.getSex()));
//		userVO.setUserTypeName(DictCache.getValue(DictEnum.USER_TYPE, user.getUserType()));
//		return userVO;
	}
//
//	public List<TaskVO> listVO(List<Task> list) {
//		List<TaskVO> collect = list.stream().map(this::entityVO).collect(Collectors.toList());
//		return collect;
//	}

}
