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
package org.springblade.composition.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springblade.composition.dto.*;
import org.springblade.composition.dto.statistics.*;
import org.springblade.composition.entity.Statistics;
import org.springblade.composition.mapper.StatisticsMapper;
import org.springblade.composition.service.ICompositionService;
import org.springblade.composition.service.IStatisticsService;
import org.springblade.composition.service.ITemplateService;
import org.springblade.composition.vo.statistics.*;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.task.feign.ILabelTaskClient;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;


/**
 * 控制器
 *
 * @author KaiLun
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/statistics")
@Api(value = "统计", tags = "统计")
public class StatisticsController extends BladeController {

	private final ILabelTaskClient labelTaskClient;
	private final IStatisticsService statisticsService;
	private final ICompositionService compositionService;
	private final ITemplateService templateService;
	private final StatisticsMapper statisticsMapper;

	/**
	 * 查询用户在一段时间内标注的各种组合的数量
	 */
	@GetMapping("/user_composition")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "查询用户在一段时间内标注的各种组合的数量", notes = "传入起止时间")
	public R<List<UserCompositionVO>> statisticsUserComposition(String startTime, String endTime, String tenantId, String userId, String taskId, @ApiParam(value="1.标注,2.真题标注") Integer type) {
		List<UserComposition> doneList = statisticsMapper.userCompositionCount(startTime,endTime,tenantId,userId,taskId,type);
		List<UserComposition> wrongList = statisticsMapper.userCompositionWrongCount(startTime,endTime,tenantId,userId,taskId,type);
		List<UserCompositionVO> resList = new ArrayList<>();
		doneList.forEach(done -> {
			UserCompositionVO userCompositionVO = Objects.requireNonNull(BeanUtil.copy(done, UserCompositionVO.class));
			resList.add(userCompositionVO);
		});
		Map<Long, List<UserComposition>> dataPerUserId = wrongList.stream()
			.collect(groupingBy(UserComposition::getUserId));
		for (UserCompositionVO res : resList) {
			if (dataPerUserId.containsKey(res.getUserId())) {
				List<UserComposition> tmpList = dataPerUserId.get(res.getUserId());
				tmpList.forEach(tmp -> {
					if (res.getCompositionId().equals(tmp.getCompositionId()))
						res.setWrong(tmp.getNumber());
				});
				if (res.getWrong() == null)
					res.setWrong(0);
			}else {
				res.setWrong(0);
			}
		};
		return R.data(resList);
	}

	/**
	 * 查询大任务在一段时间内标注的各种组合的数量，或者还可以领取的组合数量
	 */
	@GetMapping("/task_composition")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "查询大任务在一段时间内标注的各种组合的数量，或者还可以领取的组合数量", notes = "传入起止时间,大任务id,查询已完成还是待领取")
	public R<List<TaskCompositionVO>> statisticsTaskComposition(String startTime, String endTime, String taskId, @ApiParam(value="1:查询已完成的,2:查询可以领取的") Integer status, @ApiParam(value="1.标注,2.真题标注") Integer taskType, @ApiParam(value="1.标注,2.真题标注,3.质检") Integer statisticsType ) {
		List<TaskComposition> totalList = statisticsMapper.taskCompositionCount2(startTime,endTime,taskId,null,taskType,statisticsType);
		List<TaskComposition> todoList = statisticsMapper.taskCompositionCount2(startTime,endTime,taskId,2,taskType,statisticsType);
		List<TaskComposition> doneList = statisticsMapper.taskCompositionCount2(startTime,endTime,taskId,1,taskType,statisticsType);
		List<TaskComposition> wrongList = statisticsMapper.taskCompositionWrongCount2(startTime,endTime,taskId,taskType,statisticsType);
		List<TaskCompositionVO> resList = new ArrayList<>();
		totalList.forEach(total -> {
			TaskCompositionVO taskCompositionVO = Objects.requireNonNull(BeanUtil.copy(total, TaskCompositionVO.class));
			resList.add(taskCompositionVO);
		});
		Map<Long, List<TaskComposition>> todoPerCompositionId = todoList.stream()
			.collect(groupingBy(TaskComposition::getCompositionId));
		Map<Long, List<TaskComposition>> donePerCompositionId = doneList.stream()
			.collect(groupingBy(TaskComposition::getCompositionId));
		Map<Long, List<TaskComposition>> wrongPerCompositionId = wrongList.stream()
			.collect(groupingBy(TaskComposition::getCompositionId));
		for (TaskCompositionVO res : resList) {
			if (todoPerCompositionId.containsKey(res.getCompositionId())) {
				TaskComposition tmp = todoPerCompositionId.get(res.getCompositionId()).get(0);
				res.setTodo(tmp.getNumber());
				if (res.getTodo() == null)
					res.setTodo(0);
			}else {
				res.setTodo(0);
			}

			if (donePerCompositionId.containsKey(res.getCompositionId())) {
				TaskComposition tmp = donePerCompositionId.get(res.getCompositionId()).get(0);
				res.setDone(tmp.getNumber());
				if (res.getDone() == null)
					res.setDone(0);
			}else {
				res.setDone(0);
			}

			if (wrongPerCompositionId.containsKey(res.getCompositionId())) {
				TaskComposition tmp = wrongPerCompositionId.get(res.getCompositionId()).get(0);
				res.setWrong(tmp.getNumber());
				if (res.getWrong() == null)
					res.setWrong(0);
			}else {
				res.setWrong(0);
			}

			if (taskType != null && statisticsType != null && taskType == 1 && statisticsType == 3) {
				res.setWrong(-1);
			}
		};
		return R.data(resList);
	}

	/**
	 * 查询租户在一段时间内标注的各种组合的数量
	 */
	@GetMapping("/tenant_composition")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "查询租户在一段时间内标注的各种组合的数量", notes = "传入起止时间")
	public R<List<TenantCompositionVO>> statisticsTenantComposition(String startTime, String endTime, String tenantId, String taskId, @ApiParam(value="1.标注,2.真题标注") Integer type) {
		List<TenantComposition> doneList = statisticsMapper.tenantCompositionCount(startTime,endTime,tenantId,taskId,type);
		List<TenantComposition> wrongList = statisticsMapper.tenantCompositionWrongCount(startTime,endTime,tenantId,taskId,type);
		List<TenantCompositionVO> resList = new ArrayList<>();
		doneList.forEach(done -> {
			TenantCompositionVO tenantCompositionVO = Objects.requireNonNull(BeanUtil.copy(done, TenantCompositionVO.class));
			resList.add(tenantCompositionVO);
		});
		Map<String, List<TenantComposition>> dataPerUserId = wrongList.stream()
			.collect(groupingBy(TenantComposition::getTenantId));
		for (TenantCompositionVO res : resList) {
			if (dataPerUserId.containsKey(res.getTenantId())) {
				List<TenantComposition> tmpList = dataPerUserId.get(res.getTenantId());
				tmpList.forEach(tmp -> {
					if (res.getCompositionId().equals(tmp.getCompositionId()))
						res.setWrong(tmp.getNumber());
				});
				if (res.getWrong() == null)
					res.setWrong(0);
			}else {
				res.setWrong(0);
			}
		};
		return R.data(resList);
	}

	/**
	 * 查询租户在一段时间内标注的任务的数量
	 */
	@GetMapping("/tenant_task")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "查询租户在一段时间内标注的任务的数量", notes = "传入起止时间")
	public R<List<TenantTaskVO>> statisticsTenantTask(String startTime, String endTime, String tenantId, String taskId, @ApiParam(value="1.标注,2.真题标注") Integer type) {
		List<TenantTask> doneList = statisticsMapper.tenantTaskCount(startTime,endTime,tenantId,taskId,type);
		List<TenantTask> wrongList = statisticsMapper.tenantTaskWrongCount(startTime,endTime,tenantId,taskId,type);
		List<TenantTaskVO> resList = new ArrayList<>();
		doneList.forEach(done -> {
			TenantTaskVO tenantTaskVO = Objects.requireNonNull(BeanUtil.copy(done, TenantTaskVO.class));
			resList.add(tenantTaskVO);
		});
		Map<String, List<TenantTask>> dataPerUserId = wrongList.stream()
			.collect(groupingBy(TenantTask::getTenantId));
		for (TenantTaskVO res : resList) {
			if (dataPerUserId.containsKey(res.getTenantId())) {
				List<TenantTask> tmpList = dataPerUserId.get(res.getTenantId());
				tmpList.forEach(tmp -> {
					if (res.getTaskId().equals(tmp.getTaskId()))
						res.setWrong(tmp.getNumber());
				});
				if (res.getWrong() == null)
					res.setWrong(0);
			}else {
				res.setWrong(0);
			}
		};
		return R.data(resList);
	}

	/**
	 * 查询部门在一段时间内标注的各种组合的数量
	 */
	@GetMapping("/dept_composition")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "查询部门在一段时间内标注的各种组合的数量", notes = "传入起止时间")
	public R<List<DeptCompositionVO>> statisticsDeptComposition(String startTime, String endTime, String tenantId, String deptId, @ApiParam(value="1.标注,2.真题标注") Integer type) {
		List<DeptComposition> doneList = statisticsMapper.deptCompositionCount(startTime,endTime,tenantId,deptId,type);
		List<DeptComposition> wrongList = statisticsMapper.deptCompositionWrongCount(startTime,endTime,tenantId,deptId,type);
		List<DeptCompositionVO> resList = new ArrayList<>();
		doneList.forEach(done -> {
			DeptCompositionVO deptCompositionVO = Objects.requireNonNull(BeanUtil.copy(done, DeptCompositionVO.class));
			resList.add(deptCompositionVO);
		});
		Map<String, List<DeptComposition>> dataPerUserId = wrongList.stream()
			.collect(groupingBy(DeptComposition::getDeptId));
		for (DeptCompositionVO res : resList) {
			if (dataPerUserId.containsKey(res.getDeptId())) {
				List<DeptComposition> tmpList = dataPerUserId.get(res.getDeptId());
				tmpList.forEach(tmp -> {
					if (res.getCompositionId().equals(tmp.getCompositionId()))
						res.setWrong(tmp.getNumber());
				});
				if (res.getWrong() == null)
					res.setWrong(0);
			}else {
				res.setWrong(0);
			}
		};
		return R.data(resList);
	}

	/**
	 * 查询部门在一段时间内标注的任务的数量
	 */
	@GetMapping("/dept_task")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "查询部门在一段时间内标注的任务的数量", notes = "传入起止时间")
	public R<List<DeptTaskVO>> statisticsDeptTask(String startTime, String endTime, String tenantId, String deptId, @ApiParam(value="1.标注,2.真题标注") Integer type) {
		List<DeptTask> doneList = statisticsMapper.deptTaskCount(startTime,endTime,tenantId,deptId,type);
		List<DeptTask> wrongList = statisticsMapper.deptTaskWrongCount(startTime,endTime,tenantId,deptId,type);
		List<DeptTaskVO> resList = new ArrayList<>();
		doneList.forEach(done -> {
			DeptTaskVO deptTaskVO = Objects.requireNonNull(BeanUtil.copy(done, DeptTaskVO.class));
			resList.add(deptTaskVO);
		});
		Map<String, List<DeptTask>> dataPerUserId = wrongList.stream()
			.collect(groupingBy(DeptTask::getDeptId));
		for (DeptTaskVO res : resList) {
			if (dataPerUserId.containsKey(res.getDeptId())) {
				List<DeptTask> tmpList = dataPerUserId.get(res.getDeptId());
				tmpList.forEach(tmp -> {
					if (res.getTaskId().equals(tmp.getTaskId()))
						res.setWrong(tmp.getNumber());
				});
				if (res.getWrong() == null)
					res.setWrong(0);
			}else {
				res.setWrong(0);
			}
		};
		return R.data(resList);
	}

	/**
	 * 查询用户在一段时间内质检的数量和速度
	 */
	@GetMapping("/user_inspection")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "查询用户在一段时间内质检的数量和速度", notes = "传入起止时间,用户id")
	public R<List<UserInspectionDTO>> statisticsUserInspection(String startTime, String endTime, String userId) {
		return R.data(statisticsMapper.userInspectionCount(startTime,endTime,userId));
	}

//	@GetMapping(value = "/detail")
//	@ApiOperation(value = "详情")
//	public R<Statistics> detail(@RequestParam("businessId") Long businessId) {
//		LabelTask detail = labelTaskService.getById(businessId);
//		User user = UserCache.getUser(detail.getCreateUser());
////		User user = userClient.userInfoById(detail.getCreateUser()).getData();
////		detail.getFlow().setAssigneeName(user.getName());
//		return R.data(Statistics);
//	}

	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "subTaskId", value = "子任务id", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "templateId", value = "模版id", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "compositionId", value = "组合id", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "userId", value = "用户id", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "status", value = "子任务状态", paramType = "query", dataType = "integer")
	})
	@ApiOperation(value = "分页查询列表", notes = "传入param")
	public R<IPage<Statistics>> list(@ApiIgnore @RequestParam(required = false) Map<String, Object> param, Query query) {
		IPage<Statistics> pages = statisticsService.page(Condition.getPage(query), Condition.getQueryWrapper(param, Statistics.class).orderByDesc("update_time"));
		return R.data(pages);
	}
}
