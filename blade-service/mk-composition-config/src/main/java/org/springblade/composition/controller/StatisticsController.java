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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.composition.dto.TaskCompositionDTO;
import org.springblade.composition.dto.UserCompositionDTO;
import org.springblade.composition.dto.UserInspectionDTO;
import org.springblade.composition.entity.AnnotationData;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.entity.Statistics;
import org.springblade.composition.mapper.StatisticsMapper;
import org.springblade.composition.service.IAnnotationDataService;
import org.springblade.composition.service.ICompositionService;
import org.springblade.composition.service.IStatisticsService;
import org.springblade.composition.service.ITemplateService;
import org.springblade.composition.vo.AnnotationDataVO;
import org.springblade.composition.vo.TaskCompositionVO;
import org.springblade.composition.vo.TaskProgressVO;
import org.springblade.composition.vo.UserCompositionVO;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.feign.ILabelTaskClient;
import org.springblade.task.vo.ExpertLabelTaskVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
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
	public R<List<UserCompositionVO>> statisticsUserComposition(String startTime, String endTime, String userId, String taskId, @ApiParam(value="1.标注,2.真题标注") Integer type) {
		List<UserCompositionDTO> doneList = statisticsMapper.userCompositionCount(startTime,endTime,userId,taskId,type);
		List<UserCompositionDTO> wrongList = statisticsMapper.userCompositionWrongCount(startTime,endTime,userId,taskId,type);
		List<UserCompositionVO> resList = new ArrayList<>();
		doneList.forEach(done -> {
			UserCompositionVO userCompositionVO = Objects.requireNonNull(BeanUtil.copy(done, UserCompositionVO.class));
			resList.add(userCompositionVO);
		});
		Map<Long, List<UserCompositionDTO>> dataPerUserId = wrongList.stream()
			.collect(groupingBy(UserCompositionDTO::getUserId));
		for (UserCompositionVO res : resList) {
			if (dataPerUserId.containsKey(res.getUserId())) {
				List<UserCompositionDTO> tmpList = dataPerUserId.get(res.getUserId());
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
		List<TaskCompositionDTO> totalList = statisticsMapper.taskCompositionCount2(startTime,endTime,taskId,null,taskType,statisticsType);
		List<TaskCompositionDTO> todoList = statisticsMapper.taskCompositionCount2(startTime,endTime,taskId,2,taskType,statisticsType);
		List<TaskCompositionDTO> doneList = statisticsMapper.taskCompositionCount2(startTime,endTime,taskId,1,taskType,statisticsType);
		List<TaskCompositionDTO> wrongList = statisticsMapper.taskCompositionWrongCount2(startTime,endTime,taskId,taskType);
		List<TaskCompositionVO> resList = new ArrayList<>();
		totalList.forEach(total -> {
			TaskCompositionVO taskCompositionVO = Objects.requireNonNull(BeanUtil.copy(total, TaskCompositionVO.class));
			resList.add(taskCompositionVO);
		});
		Map<Long, List<TaskCompositionDTO>> todoPerCompositionId = todoList.stream()
			.collect(groupingBy(TaskCompositionDTO::getCompositionId));
		Map<Long, List<TaskCompositionDTO>> donePerCompositionId = doneList.stream()
			.collect(groupingBy(TaskCompositionDTO::getCompositionId));
		Map<Long, List<TaskCompositionDTO>> wrongPerCompositionId = wrongList.stream()
			.collect(groupingBy(TaskCompositionDTO::getCompositionId));
		for (TaskCompositionVO res : resList) {
			if (todoPerCompositionId.containsKey(res.getCompositionId())) {
				TaskCompositionDTO tmp = todoPerCompositionId.get(res.getCompositionId()).get(0);
				res.setTodo(tmp.getNumber());
				if (res.getTodo() == null)
					res.setTodo(0);
			}else {
				res.setTodo(0);
			}

			if (donePerCompositionId.containsKey(res.getCompositionId())) {
				TaskCompositionDTO tmp = donePerCompositionId.get(res.getCompositionId()).get(0);
				res.setDone(tmp.getNumber());
				if (res.getDone() == null)
					res.setDone(0);
			}else {
				res.setDone(0);
			}

			if (wrongPerCompositionId.containsKey(res.getCompositionId())) {
				TaskCompositionDTO tmp = wrongPerCompositionId.get(res.getCompositionId()).get(0);
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
