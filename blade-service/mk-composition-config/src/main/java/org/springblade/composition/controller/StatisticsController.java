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
import org.springblade.composition.vo.TaskProgressVO;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.feign.ILabelTaskClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.*;


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
	public R<List<UserCompositionDTO>> statisticsUserComposition(String startTime, String endTime, String userId, String taskId, @ApiParam(value="1.标注,2.真题标注") Integer type) {
		return R.data(statisticsMapper.userCompositionCount(startTime,endTime,userId,taskId,type));
	}

	/**
	 * 查询大任务在一段时间内标注的各种组合的数量，或者还可以领取的组合数量
	 */
	@GetMapping("/task_composition")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "查询大任务在一段时间内标注的各种组合的数量，或者还可以领取的组合数量", notes = "传入起止时间,大任务id,查询已完成还是待领取")
	public R<List<TaskCompositionDTO>> statisticsTaskComposition(String startTime, String endTime, String taskId, @ApiParam(value="1:查询已完成的,2:查询可以领取的") Integer status, @ApiParam(value="1.标注,2.真题标注") Integer type ) {
		return R.data(statisticsService.taskCompositionCount(startTime,endTime,taskId,status,type));
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

}
