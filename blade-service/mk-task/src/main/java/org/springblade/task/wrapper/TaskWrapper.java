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


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.task.cache.TaskCache;
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

	public TaskVO entityVO(Task task) {
		TaskVO taskVO = Objects.requireNonNull(BeanUtil.copy(task, TaskVO.class));
		return taskVO;
	}

//	public TaskVO setCompletedVO(TaskVO task) {
//		TaskVO taskVO = Objects.requireNonNull(BeanUtil.copy(task, TaskVO.class));
//		Integer count = TaskCache.getSubTaskCount(task.getId());
//		taskVO.setTotal(233);
//		taskVO.setCompleted(42);
//		return taskVO;
//	}
//
//	public List<TaskVO> enhanceListVO(List<TaskVO> list) {
//		return (List)list.stream().map(this::setCompletedVO).collect(Collectors.toList());
//	}
//
//	public IPage<TaskVO> toPageVO(IPage<TaskVO> pages, List<TaskVO> records) {
////		List<TaskVO> records = this.enhanceListVO(pages.getRecords());
//		IPage<TaskVO> pageVo = new Page(pages.getCurrent(), pages.getSize(), pages.getTotal());
//		pageVo.setRecords(records);
//		return pageVo;
//	}

}
