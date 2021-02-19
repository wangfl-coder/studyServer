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
package org.springblade.composition.service;

import org.apache.ibatis.annotations.Param;
import org.springblade.adata.entity.Expert;
import org.springblade.composition.dto.TaskCompositionDTO;
import org.springblade.composition.entity.Statistics;
import org.springblade.core.mp.base.BaseService;
import org.springblade.task.feign.ILabelTaskClient;

import java.util.List;

/**
 * 服务类
 *
 * @author KaiLun
 */
public interface IStatisticsService extends BaseService<Statistics> {

	/**
	 * 任务组合完成和待领取数量
	 * @param startTime
	 * @param endTime
	 * @param taskId
	 * @param type
	 * @return
	 */
	List<TaskCompositionDTO> taskCompositionCount(String startTime, String endTime, String taskId, Integer status, Integer type);

	/**
	 * 通过标注子任务id获取专家
	 * @param id
	 * @return
	 */
	Expert getExpertByLabelTaskId(Long id);
}
