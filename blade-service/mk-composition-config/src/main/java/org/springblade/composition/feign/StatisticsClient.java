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
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.api.ResultCode;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.flow.core.constant.ProcessConstant;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.enums.TaskTypeEnum;
import org.springblade.task.feign.ILabelTaskClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
		R<List<LabelTask>> labelTaskListResult = labelTaskClient.queryLabelTaskAll(taskId, TaskTypeEnum.LABEL.getNum());
		if (labelTaskListResult.isSuccess()){
			List<LabelTask> labelTaskList = labelTaskListResult.getData();
			// 默认至少有一个要标注的人
			Long templateId = labelTaskList.get(0).getTemplateId();
			List<Composition> compositionList = templateService.allCompositions(templateId);
			labelTaskList.forEach(labelTask -> {
				compositionList.forEach(composition ->{
					if (composition.getAnnotationType() != 3) {		//除补充信息外全部初始化1条
						Statistics statistics = new Statistics();
						statistics.setSubTaskId(labelTask.getId());
						statistics.setCompositionId(composition.getId());
						statistics.setTemplateId(templateId);
						statistics.setType(1);
						statistics.setStatus(1);
						statisticsService.save(statistics);
					}

					//现在每条基本信息标注任务一开始有两个人做，额外初始化1条
					if (composition.getAnnotationType() == 2) {
						Statistics statistics2 = new Statistics();
						statistics2.setSubTaskId(labelTask.getId());
						statistics2.setCompositionId(composition.getId());
						statistics2.setTemplateId(templateId);
						statistics2.setType(1);
						statistics2.setStatus(1);
						statisticsService.save(statistics2);
					}
				});
			});
		}
		return R.success("初始化Statistics表成功");
	}

	@Override
	@PostMapping(STATISTICS_INITIALIZE_SINGLE_LABELTASK)
	public R initializeSingleLabelTask(LabelTask labelTask) {
		// 默认至少有一个要标注的人
		Long templateId = labelTask.getTemplateId();
		List<Composition> compositionList = templateService.allCompositions(templateId);

		compositionList.forEach(composition ->{
			Statistics statistics = new Statistics();
			statistics.setSubTaskId(labelTask.getId());
			statistics.setCompositionId(composition.getId());
			statistics.setTemplateId(templateId);
			statistics.setType(1);
			statistics.setStatus(1);
			statisticsService.save(statistics);
		});
		return R.success("初始化Statistics表成功");
	}

	@Override
	@PostMapping(STATISTICS_INITIALIZE_REALSET_LABELTASK)
	public R initializeRealSetLabelTask(LabelTask labelTask, Map<String, String> compositionLabelMap) {
		Long templateId = labelTask.getTemplateId();
		List<Composition> compositionList = templateService.allCompositions(templateId);

		compositionList.forEach(composition ->{
			if (composition.getAnnotationType() == 2) {
				Statistics statistics = new Statistics();
				statistics.setTenantId(labelTask.getTenantId());
				String compositionId = composition.getId().toString();
				String subTaskId = compositionLabelMap.get(compositionId);
				statistics.setSubTaskId(Long.valueOf(subTaskId));
				statistics.setCompositionId(composition.getId());
				statistics.setTemplateId(templateId);
				statistics.setType(2);
				statistics.setStatus(1);
				boolean res = statisticsService.save(statistics);
				int k = 0;
			}
		});

		return R.success("初始化Statistics表成功");
	}

	@Override
	@PostMapping(STATISTICS_INITIALIZE_SINGLE_COMPOSITIONTASK)
	public R initializeSingleCompositionTask(Integer type, Long subTaskId, Long templateId, Long compositionId) {

			Statistics statistics = new Statistics();
			statistics.setSubTaskId(subTaskId);
			statistics.setCompositionId(compositionId);
			statistics.setTemplateId(templateId);
			statistics.setType(type);
			statistics.setStatus(1);
			statisticsService.save(statistics);
		return R.success("初始化Statistics表成功");
	}

	@Override
	@GetMapping(MARK_AS_COMPLETE)
	public R markAsComplete(Integer type, Long subTaskId, Long compositionId) {
		boolean res = statisticsService.update(Wrappers.<Statistics>update().lambda().set(Statistics::getStatus, 2)
			.eq(Statistics::getType, type)
			.eq(Statistics::getSubTaskId, subTaskId)
			.eq(Statistics::getCompositionId, compositionId)
			.eq(Statistics::getUserId, AuthUtil.getUserId())
		);
		return R.status(res);
	}

	@Override
	@GetMapping(IF_NEED_TO_REMOVE_BASICINFO_STATISTICS)
	public R ifNeedToRemoveBasicInfoStatistics(Long labelTaskId, Long templateId, Long compositionId) {
		List<Composition> compositions = templateService.allCompositions(templateId);
		List<String> homepageFields = new ArrayList<>();
		AtomicInteger homepageExists = new AtomicInteger(0);
		compositions.forEach(composition -> {
			if (1 == composition.getAnnotationType()) {
				List<AnnotationData> annotationDataList = annotationDataService.list(Wrappers.<AnnotationData>query().lambda()
					.eq(AnnotationData::getSubTaskId, labelTaskId)
					.eq(AnnotationData::getCompositionId, compositionId)
				);
				annotationDataList.forEach(annotationData -> {
					if (StringUtil.isNotBlank(annotationData.getValue())){
						homepageExists.getAndIncrement();
					}
				});
			}
		});
		if (homepageExists.get() == 0) {
			boolean res = statisticsService.update(Wrappers.<Statistics>update().lambda().set(Statistics::getIsDeleted, 1)
				.eq(Statistics::getTime, 0)
				.eq(Statistics::getSubTaskId, labelTaskId)
			);
		}
		return R.status(true);
	}

	@Override
	public R<Boolean> ifNeedToUpdateStatisticIswrong(Long compositionId, Long subTaskId, Long userId) {
		LambdaQueryWrapper<Statistics> queryWrapper = Wrappers.lambdaQuery();
		queryWrapper.eq(Statistics::getCompositionId,compositionId)
			.eq(Statistics::getSubTaskId,subTaskId)
			.eq(Statistics::getUserId,userId);
		Statistics statistics = statisticsService.getOne(queryWrapper);
		if (statistics != null){
			statistics.setIsWrong(0);
			Boolean is_success = statisticsService.saveOrUpdate(statistics);
			//return R.success("修改成功");
			return R.data(is_success);
		}
		return R.data(false);
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
			.eq(Statistics::getStatus, 2)	//被标注过的
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
			} else if (entry.getKey().equals("phone")
				|| entry.getKey().equals("fax")
				|| entry.getKey().equals("email")
				|| entry.getKey().equals("affiliation")
				|| entry.getKey().equals("affiliationZh")) {	//可能有多个值的字段，转成数组两两比较
				List<AnnotationData> list = entry.getValue();
				for (int i = 0; i < list.size(); i++) {
					for (int j = i + 1; j < list.size(); j++) {
						if (Func.isNoneBlank(list.get(i).getValue(), list.get(j).getValue())) {
							List<String> left = Arrays.asList(StringUtil.splitTrim(list.get(i).getValue(), "%_%"));
							List<String> right = Arrays.asList(StringUtil.splitTrim(list.get(j).getValue(), "%_%"));
							Collections.sort(left);
							Collections.sort(right);
							if (left.equals(right)) {
								sameNum++;
								sameValue.put(entry.getKey(), list.get(i).getValue());
							}
						}
					}
				}
				sameCount.put(entry.getKey(), sameNum);
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
									} else if ("-1".equals(list.get(i).getValue()) && "-1".equals(list.get(j).getValue())) {
										List<AnnotationData> titlesDescList = dataPerField.get("titlesDesc");
										int finalI = i;
										AnnotationData left = titlesDescList.stream()
											.filter(elem -> elem.getUpdateUser().equals(list.get(finalI).getUpdateUser()))
											.findAny()
											.orElse(null);
										int finalJ = j;
										AnnotationData right = titlesDescList.stream()
											.filter(elem -> elem.getUpdateUser().equals(list.get(finalJ).getUpdateUser()))
											.findAny()
											.orElse(null);
										if (left != null && right != null && left.equals(right)) {
											sameNum++;
											if (sameNum >= 1) {
												BeanUtil.setProperty(expert, "titles", -1);
												BeanUtil.setProperty(expert, "titlesDesc", left.getValue());
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
			if ((int)kv.get("biCounter") == 3 && (int)kv.get("biNotfound") == 2 && (int)kv.get("biSame") == 3) {
				//两个头像为空，一个有会导致在count为3，same count为3，notfound为2，因为有两个空所以要去质检
				kv.put("biSame", 0);
			}
			if ((int)kv.get("biCounter") == 3 && (int)kv.get("biNotfound") == 3 && (int)kv.get("biSame") == 0) {
				//在count为3，same count为0时，此时有两种情况，在biNotfound也为3时，有三者不同和都没有值的情况，三者不同去质检，都没有值不去
				if (allNotFoundNum < dataPerField.size()) {
					boolean threeDiff = false;	//在count3 notfound3会跳过质检，这时寻找有没有3个都不一样的，有就要去质检
					boolean threeSame = true;
					for (Map.Entry<String,Integer> entry : sameCount.entrySet()) {
						if (entry.getValue() == 0) {
							Integer notFoundNum = notFoundCount.get(entry.getKey());
							if (null != notFoundNum) {
								if (notFoundNum == 0)
									threeDiff = true;
							}
							if (null != notFoundNum && notFoundNum != 3 ) {
								threeSame = false;
							}
						}
						if (entry.getValue() == 3) {
							Integer notFoundNum = notFoundCount.get(entry.getKey());
							if (null != notFoundNum && notFoundNum != 0)
								threeSame = false;
						}
					}
					if (threeDiff)
						kv.put("biNotfound", 0); //在count3 notfound3会跳过质检，这时不能跳过
					if (threeSame)
						kv.put("biSame", 1);
				}
			}
			if ((int)kv.get("biCounter") == 3 && (int)kv.get("biNotfound") == 1 && (int)kv.get("biSame") == 0) {
				//在count为3，same count为0时，此时有两种情况，在biNotfound为1时，未找到的字段另外两个有不同和都相同情况，两者不同去质检，都相同不去
				boolean twoSame = true;	//在count3 same0会去质检，这时寻找notfound字段另外两个有没有都一样的，有就跳过质检
				boolean allDiff = true;
				for (Map.Entry<String,Integer> entry : sameCount.entrySet()) {
					if (entry.getValue() == 1) {
						allDiff = false;
						Set<String> keySet = sameValue.keySet();
						if (!keySet.contains(entry.getKey()))
							twoSame = false;
					}
				}
				for (Map.Entry<String,Integer> entry : notFoundCount.entrySet()) {
					if (entry.getValue() == 1) {
						Integer cnt = sameCount.get(entry.getKey());
						if (cnt!=null && cnt.intValue() == 0){
							allDiff = true;
						}
					}
				}
				if (twoSame && !allDiff)
					kv.put("biSame", 1); //在count3 same0会去质检，这时可以跳过
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

		Map<String, Integer> sameCountFiltered = sameCount.entrySet()
			.stream()
			.filter(map -> sameValue.get(map.getKey()) != null)
			.collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));

		if (3 == (int)kv.get("biCounter")) {	// 3人中2人一致的情况，将剩下的计入错误日志
			sameCountFiltered.entrySet().forEach(entry -> {
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
							boolean temp2 = statisticsService.update(
								Wrappers.<Statistics>update().lambda().set(Statistics::getIsWrong, 3)
									.eq(Statistics::getSubTaskId, statistics.getSubTaskId())
									.eq(Statistics::getCompositionId, statistics.getCompositionId())
									.eq(Statistics::getUserId, statistics.getUserId())
							);
							statisticsService.calcReliabilityRate(statistics.getUserId());
						}
					});

				}
			});
		}

		if ((int)kv.get("biCounter") == 2 && (int)kv.get("biSame") == 0) {
			//准备给第三人，建个统计
			Statistics statistics_query = new Statistics();
			statistics_query.setSubTaskId(labelTaskId);
			statistics_query.setCompositionId(compositionId);
			statistics_query.setTemplateId(templateId);
			statistics_query.setType(1);
			statistics_query.setStatus(1);

			Statistics statistics = statisticsService.getOne(Condition.getQueryWrapper(statistics_query));
			if (statistics == null) {
				Statistics stat_new = new Statistics();
				stat_new.setSubTaskId(labelTaskId);
				stat_new.setCompositionId(compositionId);
				stat_new.setTemplateId(templateId);
				stat_new.setType(1);
				stat_new.setStatus(1);
				statisticsService.save(stat_new);
			}
		}

		if ((int)kv.get("biCounter") == 3 && (int)kv.get("biSame") == 0) {
			//准备去质检，建个统计
			Statistics statistics_query = new Statistics();
			statistics_query.setSubTaskId(labelTaskId);
			statistics_query.setCompositionId(compositionId);
			statistics_query.setTemplateId(templateId);
			statistics_query.setType(3);
			statistics_query.setStatus(1);

			Statistics statistics = statisticsService.getOne(Condition.getQueryWrapper(statistics_query));
			if (statistics == null){
				Statistics stat_new = new Statistics();
				stat_new.setSubTaskId(labelTaskId);
				stat_new.setCompositionId(compositionId);
				stat_new.setTemplateId(templateId);
				stat_new.setType(3);
				stat_new.setStatus(1);
				statisticsService.save(stat_new);
			}
		}

		return R.data(kv);
	}
}
