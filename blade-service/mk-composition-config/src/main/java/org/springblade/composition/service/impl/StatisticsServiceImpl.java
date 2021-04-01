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

import lombok.RequiredArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.composition.dto.statistics.TaskComposition;
import org.springblade.composition.entity.Statistics;
import org.springblade.composition.mapper.StatisticsMapper;
import org.springblade.composition.service.ILogBalanceService;
import org.springblade.composition.service.ILogPointsService;
import org.springblade.composition.service.IStatisticsService;
import org.springblade.core.cache.utils.CacheUtil;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.log.entity.UserLog;
import org.springblade.system.cache.DictBizCache;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.enums.UserStatusEnum;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static org.springblade.core.cache.constant.CacheConstant.USER_CACHE;

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
	private final IUserClient userClient;

	@Override
	public List<TaskComposition> taskCompositionCount(String startTime, String endTime, String taskId, Integer status, Integer taskType, Integer statisticsType){

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

	@Override
	public boolean calcReliabilityRate(Long userId) {
		User user = UserCache.getUser(userId);
		if (user.getStatus() == UserStatusEnum.BLOCKED.getNum())
			return true;
		String refreshTime = null;
		if (user.getRefreshTime() != null) {
			refreshTime = user.getRefreshTime().toString();
		}

		Integer amount = Integer.valueOf(DictBizCache.getValue("required_reliability_rate", "start_reliability_rate_amount"));
		Integer iaaTotal = statisticsMapper.userTotalCount(2, user.getId(), refreshTime);
		Integer siTotal = statisticsMapper.userTotalCount(5, user.getId(), refreshTime);
		if (iaaTotal >= amount) {
			Integer iaaWrong = statisticsMapper.userWrongCount(2, user.getId(), refreshTime);
			int iaaRate = (int)((1-(iaaWrong.floatValue()/iaaTotal))*100);
			Integer requiredIAARate = Integer.valueOf(DictBizCache.getValue("required_reliability_rate", "iaa_required_rate"));
			if (iaaRate < requiredIAARate.intValue()) {
				UserLog userLog = Objects.requireNonNull(BeanUtil.copy(user, UserLog.class));
				userLog.setType(1);
				userLog.setRemark(StringUtil.format("用户{}因为多人标注正确率过低：{}%被封停无法再接任务", user.getAccount(), iaaRate));
				user.setStatus(UserStatusEnum.BLOCKED.getNum());
				CacheUtil.clear(USER_CACHE);
				userClient.updateUser(user);
			}
		}
		if (siTotal >= amount) {
			Integer siWrong = statisticsMapper.userWrongCount(5, user.getId(), refreshTime);
			int siRate = (int)((1-(siWrong.floatValue()/siTotal))*100);
			Integer requiredSIRate = Integer.valueOf(DictBizCache.getValue("required_reliability_rate", "sampling_inspection_required_rate"));
			if (siRate < requiredSIRate.intValue()) {
				UserLog userLog = Objects.requireNonNull(BeanUtil.copy(user, UserLog.class));
				userLog.setType(1);
				userLog.setRemark(StringUtil.format("用户{}因为人工抽检正确率过低：{}%被封停无法再接任务", user.getAccount(), siRate));
				user.setStatus(UserStatusEnum.BLOCKED.getNum());
				CacheUtil.clear(USER_CACHE);
				userClient.updateUser(user);
			}
		}










		return true;
	}
}
