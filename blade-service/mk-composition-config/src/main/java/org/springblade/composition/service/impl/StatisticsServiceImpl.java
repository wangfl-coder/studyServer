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
import org.springblade.composition.service.IStatisticsService;
import org.springblade.core.mp.base.BaseServiceImpl;
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
	@Value("${spring.profiles.active}")
	public String env;
	@Override
	public List<TaskCompositionDTO> taskCompositionCount(String startTime, String endTime, String taskId, Integer type){

		return statisticsMapper.taskCompositionCount(env,startTime, endTime,taskId,type);
	}

	@Override
	public Expert getExpertByLabelTaskId(Long id){
		return statisticsMapper.getExpertByLabelTaskId(id);
	}
}
