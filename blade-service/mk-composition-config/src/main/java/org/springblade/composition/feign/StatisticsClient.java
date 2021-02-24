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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.composition.entity.AnnotationData;
import org.springblade.composition.entity.AnnotationDataErrata;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.entity.Statistics;
import org.springblade.composition.service.*;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.api.ResultCode;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.feign.ILabelTaskClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
	private final AnnotationDataErrataService annotationDataErrataService;
	private final IExpertClient expertClient;


	@Override
	@GetMapping(STATISTICS_INITIALIZE_LABELTASK)
	@Transactional(rollbackFor = Exception.class)
	public R initializeLabelTask(Long taskId) {
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
					statistics.setType(1);
					statisticsService.save(statistics);
				});
			});
		}
		return R.success("初始化Statistics表成功");
	}

	@Override
	@PostMapping(STATISTICS_INITIALIZE_REALSET_LABELTASK)
	@Transactional(rollbackFor = Exception.class)
	public R initializeRealSetLabelTask(LabelTask labelTask, Map<String, String> compositionLabelMap) {
		Long templateId = labelTask.getTemplateId();
		List<Composition> compositionList = templateService.allCompositions(templateId);

		compositionList.forEach(composition ->{
			if (composition.getAnnotationType() == 2) {
				Statistics statistics = new Statistics();
				String compositionId = composition.getId().toString();
				String subTaskId = compositionLabelMap.get(compositionId);
				statistics.setSubTaskId(Long.valueOf(subTaskId));
				statistics.setCompositionId(composition.getId());
				statistics.setTemplateId(templateId);
				statistics.setType(2);
				boolean res = statisticsService.save(statistics);
				int k = 0;
			}
		});

		return R.success("初始化Statistics表成功");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public R<Kv> queryBasicInfoStatus(Long labelTaskId, Long templateId, Long compositionId) {
//		Statistics statistics_query = new Statistics();
//		statistics_query.setSubTaskId(labelTaskId);
//		statistics_query.setCompositionId(compositionId);
//		statistics_query.setType(1);
		LambdaQueryWrapper<Statistics> queryWrapper = Wrappers.lambdaQuery();
		queryWrapper.eq(Statistics::getSubTaskId, labelTaskId)
			.eq(Statistics::getCompositionId, compositionId)
			.in(Statistics::getType, 1, 3); //普通和质检加起来总数
		int count = 0;
		Kv kv = Kv.create();
		List<Statistics> statisticsList = statisticsService.list(queryWrapper);
		if (statisticsList != null){
			count = statisticsList.size();
			kv.put("biCounter", statisticsList.size());	//标注了几次，初始化就是1
			if (count == 4) {
				kv.put("biNotfound", 0);
				kv.put("biSame", 0);
				return R.data(kv);
			}
		}
		List<Composition> compositionList = templateService.allCompositions(templateId);
		Composition composition = compositionService.getById(compositionId);
		List<AnnotationData> annotationDataList = annotationDataService.list(Wrappers.<AnnotationData>query().lambda()
			.eq(AnnotationData::getSubTaskId, labelTaskId)
			.eq(AnnotationData::getCompositionId, compositionId)
		);
		Map<String, List<AnnotationData>> dataPerField = annotationDataList.stream()
			.collect(groupingBy(AnnotationData::getField));
		HashMap<String, Integer> notFound = new HashMap<>();
		HashMap<String, Integer> notFoundCount = new HashMap<>();
		boolean allNotFound = false;
		int allNotFoundNum = 0;
		for (Map.Entry<String,List<AnnotationData>> entry : dataPerField.entrySet()) {
			int notFoundNum = 0;
			if (entry.getKey().equals("titles") || entry.getKey().equals("titlesDesc")) {
				if (entry.getKey().equals("titles")) {
					List<AnnotationData> list = entry.getValue();
					for (int i = 0; i < list.size(); i++) {
						AnnotationData annotationData = list.get(i);
						if (Func.isBlank(annotationData.getValue())) {
							notFoundNum++;
						}
					}
					notFoundCount.put("titles", notFoundNum);
					if (0 < notFoundNum && notFoundNum < count)        //全找到的不感兴趣
						notFound.put("titles", notFoundNum);
					else if (notFoundNum == count) {
						allNotFound = true;
						allNotFoundNum++;
					}
					notFoundNum = 0;
					int titleNotFound = 0;
					for (int i = 0; i < list.size(); i++) {
						AnnotationData annotationData = list.get(i);
						if ("-1".equals(annotationData.getValue())) {
							titleNotFound++;
						}
					}
					List<AnnotationData> desclist = dataPerField.get("titlesDesc");
					if (desclist != null) {
						int descFound = desclist.size();
						notFoundNum = titleNotFound - descFound;
						notFoundCount.put("titlesDesc", notFoundNum);
						if (0 < notFoundNum && notFoundNum < count)        //全找到的不感兴趣
							notFound.put("titlesDesc", notFoundNum);
						else if (notFoundNum == count) {
							allNotFound = true;
							allNotFoundNum++;
						}
					}
				}
			}else {
				for (AnnotationData annotationData : entry.getValue()) {
					if (Func.isBlank(annotationData.getValue())) {
						notFoundNum++;
					}
				}
				notFoundCount.put(entry.getKey(), notFoundNum);
				if (0 < notFoundNum && notFoundNum < count)        //全找到的不感兴趣
					notFound.put(entry.getKey(), notFoundNum);
				else if (notFoundNum == count) {
					allNotFound = true;
					allNotFoundNum++;
				}
			}
		}
		if (notFound.size() > 0) {
			Optional<Map.Entry<String, Integer>> maxEntry = notFound.entrySet()
				.stream()
				.max(Comparator.comparing(Map.Entry::getValue));
			kv.put("biNotfound", maxEntry.get().getValue());
		} else if (allNotFound == true) {
			kv.put("biNotfound", count);
		} else {
			kv.put("biNotfound", 0);
		}

		Expert expert = statisticsService.getExpertByLabelTaskId(labelTaskId);
		HashMap<String, Integer> sameCount = new HashMap<>();
		HashMap<String, String> sameValue = new HashMap<>();
		for (Map.Entry<String,List<AnnotationData>> entry : dataPerField.entrySet()) {
			int sameNum = 0;
			if (entry.getKey().equals("avatar")) {
				continue;
			} else if (entry.getKey().equals("titles") || entry.getKey().equals("titlesDesc")) {
				if (entry.getKey().equals("titles")) {
					//职称字段先两两对比，在-1时需要对比职称其它字段中的内容
					List<AnnotationData> list = entry.getValue();
					for (int i = 0; i < list.size(); i++) {
						for (int j = i + 1; j < list.size(); j++) {
							if (Func.isNoneBlank(list.get(i).getValue(), list.get(j).getValue())) {
								if (list.get(i).getValue().equals(list.get(j).getValue())) {
									if (!"-1".equals(list.get(i).getValue())) {
										sameNum++;
										if (sameNum >= 1) {
											BeanUtil.setProperty(expert, "titles", list.get(i).getValue());
											BeanUtil.setProperty(expert, "titlesDesc", "");
											sameValue.put(entry.getKey(), list.get(i).getValue());
										}
									}else {
										List<AnnotationData> titlesDescList = dataPerField.get("titlesDesc");
										String left = titlesDescList.get(i).getValue();
										String right = titlesDescList.get(j).getValue();
										if (left.equals(right)) {
											sameNum++;
											if (sameNum >= 1) {
												BeanUtil.setProperty(expert, "titles", -1);
												BeanUtil.setProperty(expert, "titlesDesc", titlesDescList.get(i).getValue());
												sameValue.put(entry.getKey(), list.get(i).getValue());
											}
										}
									}
								}
							}
						}
					}
					if (sameNum > 0) {
						sameCount.put("titles", sameNum);
						sameCount.put("titlesDesc", sameNum);
					}
				}
			} else {
				//普通字段两两对比
				List<AnnotationData> list = entry.getValue();
				for (int i = 0; i < list.size(); i++) {
					for (int j = i + 1; j < list.size(); j++) {
						if (Func.isNoneBlank(list.get(i).getValue(), list.get(j).getValue())) {
							if (list.get(i).getValue().equals(list.get(j).getValue())) {
								sameNum++;
								if (sameNum >= 1) {
									BeanUtil.setProperty(expert, entry.getKey(), list.get(i).getValue());
									sameValue.put(entry.getKey(), list.get(i).getValue());
								}
							}
						}
					}
				}
				sameCount.put(entry.getKey(), sameNum);
			}
		}
		expertClient.saveExpert(expert);
		if (sameCount.size() > 0) {
			//只看最小的
			Optional<Map.Entry<String, Integer>> minEntry = sameCount.entrySet()
				.stream()
				.min(Comparator.comparing(Map.Entry::getValue));
			kv.put("biSame", minEntry.get().getValue());
			if ((int)kv.get("biCounter") == 2 && (int)kv.get("biNotfound") == 2 && (int)kv.get("biSame") == 1) {
				//在count为2，same count为1时，因为有两个空所以要找第三人
				kv.put("biSame", 0);
			}
			if ((int)kv.get("biCounter") == 3 && (int)kv.get("biNotfound") == 2 && (int)kv.get("biSame") == 1) {
				//在count为3，same count为1时，因为有两个空所以要去质检
				kv.put("biSame", 0);
			}
			if ((int)kv.get("biCounter") == 3 && (int)kv.get("biNotfound") == 3 && (int)kv.get("biSame") == 0) {
				//在count为3，same count为0时，此时有两种情况，在biNotfound也为3时，有三者不同和都没有值的情况，三者不同去质检，都没有值不去
				if (allNotFoundNum < dataPerField.size()) {
					boolean threeDiff = false;	//在count3 notfound3会跳过质检，这时寻找有没有3个都不一样的，有就要去质检
					for (Map.Entry<String,Integer> entry : sameCount.entrySet()) {
						if (entry.getValue() == 0) {
							Integer notFoundNum = notFoundCount.get(entry.getKey());
							if (null != notFoundNum) {
								if (notFoundNum == 0)
									threeDiff = true;
							}
						}
					}
					if (threeDiff)
						kv.put("biNotfound", 0); //在count3 notfound3会跳过质检，这时不能跳过
				}
			}
		} else {
			if ((int)kv.get("biCounter") == 3 && (int)kv.get("biNotfound") == 3) {
				//在count为3，same count为0时，此时有两种情况，在biNotfound也为3时，有三者不同和都没有值的情况，三者不同去质检，都没有值不去
				if (allNotFoundNum < dataPerField.size()) {
					kv.put("biNotfound", 0); //不能跳过
				}
				kv.put("biSame", 0);
			}else {
				kv.put("biSame", 0);
			}
		}

		if (3 == (int)kv.get("biCounter")) {	// 3人中2人一致的情况，将剩下的计入错误日志
			sameCount.entrySet().forEach(entry -> {
				if (1 == entry.getValue()) {
					statisticsList.forEach(statistics -> {
						AnnotationData annoData = annotationDataList.stream().filter(elem -> {
							if (statistics.getSubTaskId().equals(elem.getSubTaskId())
								&& statistics.getCompositionId().equals(elem.getCompositionId())
								&& elem.getCreateUser().equals(statistics.getUserId())
								&& entry.getKey().equals(elem.getField())
								&& Objects.equals(sameValue.get(entry.getKey()), elem.getValue())
							) {
								return true;
							}
							return false;
						}).findAny().orElse(null);
						if (annoData == null) {
							AnnotationDataErrata annotationDataErrata = new AnnotationDataErrata();
							annotationDataErrata.setTenantId(statistics.getTenantId());
							annotationDataErrata.setSubTaskId(statistics.getSubTaskId());
							annotationDataErrata.setCompositionId(statistics.getCompositionId());
							annotationDataErrata.setLabelerId(statistics.getUserId());
							annotationDataErrata.setExpertId(expert.getId());
							annotationDataErrata.setField(entry.getKey());
							annotationDataErrata.setValue(sameValue.get(entry.getKey()));
							annotationDataErrata.setType(2); // 标注类型 个人信息
							annotationDataErrata.setSource(3); // 来源多人对比
							AnnotationData errData = annotationDataList.stream().filter(elem -> {
								if (statistics.getSubTaskId().equals(elem.getSubTaskId())
									&& statistics.getCompositionId().equals(elem.getCompositionId())
									&& elem.getCreateUser().equals(statistics.getUserId())
									&& entry.getKey().equals(elem.getField())
								) {
									return true;
								}
								return false;
							}).findAny().orElse(null);
							if (errData != null) {
								annotationDataErrata.setAnnotationDataId(errData.getId());
							}
							annotationDataErrataService.saveOrUpdate(annotationDataErrata);
						}
					});

				}
			});
		}

		return R.data(kv);
	}
}
