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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.composition.entity.AnnotationData;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.entity.Statistics;
import org.springblade.composition.service.IAnnotationDataService;
import org.springblade.composition.service.ICompositionService;
import org.springblade.composition.service.IStatisticsService;
import org.springblade.composition.service.ITemplateService;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.api.ResultCode;
import org.springblade.core.tool.support.Kv;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.feign.ILabelTaskClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;


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
	private final ICompositionService compositionService;
	private final IAnnotationDataService annotationDataService;

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

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R<Kv> queryBasicInfoStatus(Long labelTaskId, Long templateId, Long compositionId) {
		Statistics statistics_query = new Statistics();
		statistics_query.setSubTaskId(labelTaskId);
		statistics_query.setCompositionId(compositionId);

		int count = 0;
		Kv kv = Kv.create();
		List<Statistics> res = statisticsService.list(Condition.getQueryWrapper(statistics_query));
		if (res != null){
			count = res.size();
			kv.put("biCounter", res.size());	//标注了几次，初始化就是1
		}
		List<Composition> compositionList = templateService.allCompositions(templateId);
		Composition composition = compositionService.getById(compositionId);
		List<AnnotationData> annotationDataList = annotationDataService.list(Wrappers.<AnnotationData>query().lambda().eq(AnnotationData::getSubTaskId, labelTaskId).eq(AnnotationData::getCompositionId, compositionId));
		Map<String, List<AnnotationData>> dataPerField = annotationDataList.stream()
			.collect(groupingBy(AnnotationData::getField));
		HashMap<String, Integer> notFound = new HashMap<>();
		boolean allNotFound = false;
		for (Map.Entry<String,List<AnnotationData>> entry : dataPerField.entrySet()) {
			int notFoundNum = count - entry.getValue().size();
			if (0 < notFoundNum && notFoundNum < count)		//全找到的不感兴趣
				notFound.put(entry.getKey(), count - entry.getValue().size());
			else if (notFoundNum == count)
				allNotFound = true;
		}
		if (notFound.size() > 0) {
			Optional<Map.Entry<String, Integer>> maxEntry = notFound.entrySet()
				.stream()
				.max(Comparator.comparing(Map.Entry::getValue));
			kv.put("biNotfound", maxEntry.get().getValue());
		} else if (allNotFound) {
			kv.put("biNotfound", count);
		} else {
			kv.put("biNotfound", 0);
		}

		HashMap<String, Integer> same = new HashMap<>();
		for (Map.Entry<String,List<AnnotationData>> entry : dataPerField.entrySet()) {
			int sameNum = 0;
			List<AnnotationData> list = entry.getValue();
			for (int i = 0; i < list.size(); i++) {
				for (int j = i+1; j < list.size(); j++) {
					if(list.get(i).getValue().equals(list.get(j).getValue()))
						sameNum++;
				}
			}
			same.put(entry.getKey(), sameNum);
		}
		//只看最小的
		Optional<Map.Entry<String, Integer>> minEntry = same.entrySet()
			.stream()
			.min(Comparator.comparing(Map.Entry::getValue));
		kv.put("biSame", minEntry.get().getValue());
		return R.data(kv);
	}
}
