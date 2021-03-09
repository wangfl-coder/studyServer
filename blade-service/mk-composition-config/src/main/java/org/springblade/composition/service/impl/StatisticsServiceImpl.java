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
package org.springblade.composition.service.impl;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springblade.adata.entity.Expert;
import org.springblade.composition.dto.TaskCompositionDTO;
import org.springblade.composition.entity.Statistics;
import org.springblade.composition.mapper.StatisticsMapper;
import org.springblade.composition.service.ILogBalanceService;
import org.springblade.composition.service.ILogPointsService;
import org.springblade.composition.service.IStatisticsService;
import org.springblade.composition.vo.AnnotationDataVO;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.utils.AuthUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务实现类
 *
 * @author KaiLun
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl extends BaseServiceImpl<StatisticsMapper, Statistics> implements IStatisticsService {
	private final StatisticsMapper statisticsMapper;
	private final ILogBalanceService logBalanceService;
	private final ILogPointsService logPointsService;

	@Value("${spring.profiles.active}")
	public String env;
	@Override
	public List<TaskCompositionDTO> taskCompositionCount(String startTime, String endTime, String taskId, Integer status, Integer taskType, Integer statisticsType){

//		return statisticsMapper.taskCompositionCount(env,startTime, endTime,taskId,status,type);
		return statisticsMapper.taskCompositionCount2(startTime,endTime,taskId,null,taskType,statisticsType);
	}

	@Override
	public Expert getExpertByLabelTaskId(Long id){
		return statisticsMapper.getExpertByLabelTaskId(id);
	}


	@Override
	public boolean updateAnnotationStatistics(int type, Long subTaskId, Long templateId, Long compositionId, Integer deltaTime) {
		Statistics statistics_query = new Statistics();
		statistics_query.setSubTaskId(subTaskId);
		statistics_query.setCompositionId(compositionId);
		statistics_query.setUserId(AuthUtil.getUserId());
		statistics_query.setType(type);

		Statistics statistics = getOne(Condition.getQueryWrapper(statistics_query));
		if (statistics != null){
			statistics.setTime(statistics.getTime() + deltaTime);
			statistics.setStatus(2);
			saveOrUpdate(statistics);
		} else {	//有两种情况:
			// 1.组合没有被标;2.被标过但不是当前的人
			boolean wrote = false;
			Statistics stat_his_query = new Statistics();
			stat_his_query.setSubTaskId(subTaskId);
			stat_his_query.setCompositionId(compositionId);
			stat_his_query.setType(type);
			List<Statistics> statistics_his = list(Condition.getQueryWrapper(stat_his_query));
			for(Statistics statistics_history: statistics_his) {
				if (null == statistics_history.getUserId()) {	//	组合没有被标
					statistics_history.setTime(deltaTime);
					statistics_history.setStatus(2);
					statistics_history.setSubTaskId(subTaskId);
					statistics_history.setTemplateId(templateId);
					statistics_history.setCompositionId(compositionId);
					statistics_history.setUserId(AuthUtil.getUserId());
					statistics_history.setType(type);
					wrote = true;
					saveOrUpdate(statistics_history);
//					logBalanceService.save();
//					logPointsService.save();
					break;
				}
			};
			if (!wrote) {    //	被标过但不是当前的人
				Statistics new_stat = new Statistics();
				new_stat.setTime(deltaTime);
				new_stat.setStatus(2);
				new_stat.setSubTaskId(subTaskId);
				new_stat.setTemplateId(templateId);
				new_stat.setCompositionId(compositionId);
				new_stat.setUserId(AuthUtil.getUserId());
				new_stat.setType(type);
				saveOrUpdate(new_stat);
			}
		}
		return true;
	}
}
