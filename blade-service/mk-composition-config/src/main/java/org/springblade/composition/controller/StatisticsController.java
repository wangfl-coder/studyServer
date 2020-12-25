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
import org.springblade.composition.entity.Statistics;
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

	/**
	 * 查询标注数据
	 */
	@GetMapping("/task")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "查询智库任务完成情况", notes = "传入智库任务id")
	public R<TaskProgressVO> statisticsTaskProgress(Long taskId) {
		TaskProgressVO taskProgressVO = new TaskProgressVO();
		R<List<LabelTask>> labelTaskListResult = labelTaskClient.queryLabelTask(taskId);
		if (labelTaskListResult.isSuccess()){
			List<LabelTask> labelTaskList = labelTaskListResult.getData();
			taskProgressVO.setAnnotationTotal(Long.valueOf(labelTaskList.size()));
			List<Long> labelTaskIds = new ArrayList();
			labelTaskList.forEach(labelTask -> labelTaskIds.add(labelTask.getTaskId()));
			List<Statistics> statisticsList = statisticsService.list(Wrappers.<Statistics>query().in("sub_task_id",labelTaskIds));
			taskProgressVO.setFinishCount(statisticsList.stream().filter(statistics -> statistics.getCompositionId()==-1).count());
			return R.data(taskProgressVO);
		}
		return R.data(taskProgressVO);
	}


}
