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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.composition.entity.AnnotationData;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.entity.Statistics;
import org.springblade.composition.service.CompositionService;
import org.springblade.composition.service.IAnnotationDataService;
import org.springblade.composition.service.IStatisticsService;
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
	private final CompositionService compositionService;

	/**
	 * 查询标注数据
	 */
	@GetMapping("/task")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "查询智库任务完成情况", notes = "传入智库任务id")
	public R<TaskProgressVO> statisticsTaskProgress(Long taskId) {
		TaskProgressVO taskProgressVO = new TaskProgressVO();
		// 查询一个智库下有多少任务（人）
		R<List<LabelTask>> labelTaskListResult = labelTaskClient.queryLabelTaskAll(taskId);
		List<Long> labelTaskIds = new ArrayList<>();
		if (labelTaskListResult.isSuccess()){
			List<LabelTask> labelTaskList = labelTaskListResult.getData();
			labelTaskList.forEach(labelTask -> labelTaskIds.add(labelTask.getId()));
			taskProgressVO.setAnnotationTotal(labelTaskList.size());
			// 查询完成的任务
			R<Integer> result = labelTaskClient.queryCompleteTaskCount(taskId);
			if (result.isSuccess()){
				taskProgressVO.setFinishCount(result.getData());
			}
		}
		// 统计每种组合的完成（提交）个数
		List<Composition> compositionList = new ArrayList<>();
		QueryWrapper<Statistics> wrapper = new QueryWrapper<>();
		wrapper.groupBy("composition_id");
		wrapper.select("composition_id,count(*) as composition_submit_count");
		wrapper.in("sub_task_id",labelTaskIds);
		List<Statistics> statisticsList = statisticsService.list(wrapper);
		statisticsList.forEach(statistics -> {
			Composition composition = compositionService.getById(statistics.getCompositionId());
			// 现在因为补充信息没有composition_id，所以需要加一个非空判断。
			if(composition != null) {
				composition.setSubmitCount(statistics.getCompositionSubmitCount());
				compositionList.add(composition);
			} else {
				Composition composition_supplement = new Composition();
				composition_supplement.setSubmitCount(statistics.getCompositionSubmitCount());
				composition_supplement.setName("补充信息");
				compositionList.add(composition_supplement);
			}

		});
		taskProgressVO.setCompositionList(compositionList);
		return R.data(taskProgressVO);
	}


}
