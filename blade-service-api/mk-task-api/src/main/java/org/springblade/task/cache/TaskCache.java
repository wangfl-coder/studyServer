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
package org.springblade.task.cache;

import org.springblade.task.entity.Task;
import org.springblade.task.feign.ITaskClient;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.SpringUtil;
import org.springblade.core.tool.utils.StringPool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springblade.core.cache.constant.CacheConstant.SYS_CACHE;

/**
 * 任务
 *
 * @author Chill
 */
public class TaskCache {
	private static final String MK_CACHE = "markingline:";

	private static final String TASK_ID = "task:id:";
	private static final String TASK_NAME = "task:name:";
	private static final String TASK_NAME_ID = "taskName:id:";
	private static final String TASK_NAMES_ID = "taskNames:id:";
	private static final String TASK_CHILD_ID = "taskChild:id:";
	private static final String TASK_CHILDIDS_ID = "taskChildIds:id:";
	private static final String TASK_SUBTASK_COUNT = "task:subtask:count:";

	private static ITaskClient taskClient;

	private static ITaskClient getTaskClient() {
		if (taskClient == null) {
			taskClient = SpringUtil.getBean(ITaskClient.class);
		}
		return taskClient;
	}

//	/**
//	 * 获取子任务数量
//	 *
//	 * @param id 主键
//	 * @return
//	 */
//	public static int getSubTaskCount(Long id) {
//		return CacheUtil.get(MK_CACHE, TASK_SUBTASK_COUNT, id, () -> {
//			R<Integer> result = getTaskClient().getSubTaskCount(id);
//			return result.getData();
//		});
//	}

//	/**
//	 * 获取任务
//	 *
//	 * @param id 主键
//	 * @return
//	 */
//	public static Task getTask(Long id) {
//		return CacheUtil.get(SYS_CACHE, TASK_ID, id, () -> {
//			R<Task> result = getTaskClient().getTask(id);
//			return result.getData();
//		});
//	}
//
//	/**
//	 * 获取任务id
//	 *
//	 * @param tenantId  租户id
//	 * @param taskNames 任务名
//	 * @return
//	 */
//	public static String getTaskIds(String tenantId, String taskNames) {
//		return CacheUtil.get(SYS_CACHE, TASK_NAME, tenantId + StringPool.DASH + taskNames, () -> {
//			R<String> result = getTaskClient().getTaskIds(tenantId, taskNames);
//			return result.getData();
//		});
//	}
//
//	/**
//	 * 获取任务名
//	 *
//	 * @param id 主键
//	 * @return 任务名
//	 */
//	public static String getTaskName(Long id) {
//		return CacheUtil.get(SYS_CACHE, TASK_NAME_ID, id, () -> {
//			R<String> result = getTaskClient().getTaskName(id);
//			return result.getData();
//		});
//	}
//
//
//	/**
//	 * 获取任务名集合
//	 *
//	 * @param taskIds 主键集合
//	 * @return 任务名
//	 */
//	public static List<String> getTaskNames(String taskIds) {
//		return CacheUtil.get(SYS_CACHE, TASK_NAMES_ID, taskIds, () -> {
//			R<List<String>> result = getTaskClient().getTaskNames(taskIds);
//			return result.getData();
//		});
//	}
//
//	/**
//	 * 获取子任务集合
//	 *
//	 * @param taskId 主键
//	 * @return 子任务
//	 */
//	public static List<Task> getTaskChild(Long taskId) {
//		return CacheUtil.get(SYS_CACHE, TASK_CHILD_ID, taskId, () -> {
//			R<List<Task>> result = getTaskClient().getTaskChild(taskId);
//			return result.getData();
//		});
//	}
//
//	/**
//	 * 获取子任务ID集合
//	 *
//	 * @param taskId 主键
//	 * @return 子任务ID
//	 */
//	public static List<Long> getTaskChildIds(Long taskId) {
//		if (taskId == null) {
//			return null;
//		}
//		List<Long> taskIdList = CacheUtil.get(SYS_CACHE, TASK_CHILDIDS_ID, taskId, List.class);
//		if (taskIdList == null) {
//			taskIdList = new ArrayList<>();
//			List<Task> taskChild = getTaskChild(taskId);
//			if (taskChild != null) {
//				List<Long> collect = taskChild.stream().map(Task::getId).collect(Collectors.toList());
//				taskIdList.addAll(collect);
//			}
//			taskIdList.add(taskId);
//			CacheUtil.put(SYS_CACHE, TASK_CHILDIDS_ID, taskId, taskIdList);
//		}
//		return taskIdList;
//	}
}
