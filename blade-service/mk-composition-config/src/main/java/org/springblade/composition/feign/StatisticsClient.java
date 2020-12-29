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
package org.springblade.composition.feign;

import lombok.AllArgsConstructor;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.entity.Statistics;
import org.springblade.composition.service.IStatisticsService;
import org.springblade.composition.service.ITemplateService;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.api.ResultCode;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.feign.ILabelTaskClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;


/**
 * Notice Feign
 *
 * @author Chill
 */
@NonDS
@ApiIgnore()
@RestController
@AllArgsConstructor
public class StatisticsClient implements IStatisticsClient {
	private final ILabelTaskClient labelTaskClient;
	private final ITemplateService templateService;
	private final IStatisticsService statisticsService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R initialize(Long taskId) {
		R<List<LabelTask>> labelTaskListResult = labelTaskClient.queryLabelTaskAll(taskId);
		if (labelTaskListResult.isSuccess()){
			List<LabelTask> labelTaskList = labelTaskListResult.getData();
			// 默认至少有一个要标注的人
			Long templateId = labelTaskList.get(0).getTemplateId();
			List<Composition> compositionList = templateService.allCompositions(templateId);
			labelTaskList.forEach(labelTask -> {
				compositionList.forEach(composition ->{
					Statistics statistics = new Statistics();
					statistics.setSubTaskId(labelTask.getId());
					statistics.setCompositionId(composition.getId());
					statistics.setTemplateId(templateId);
					statisticsService.save(statistics);
				});
				// 初始化补充信息
				Statistics statistics = new Statistics();
				statistics.setSubTaskId(labelTask.getId());
				statistics.setCompositionId(-1L);
				statistics.setTemplateId(templateId);
				statisticsService.save(statistics);
			});
		}
		return R.success("初始化Statistics表成功");
	}
}
