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
package org.springblade.composition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springblade.adata.entity.Expert;
import org.springblade.composition.dto.TaskCompositionDTO;
import org.springblade.composition.dto.UserCompositionDTO;
import org.springblade.composition.dto.UserInspectionDTO;
import org.springblade.composition.entity.Statistics;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * Mapper 接口
 *
 * @author KaiLun
 */
public interface StatisticsMapper extends BaseMapper<Statistics> {


	/**
	 * 查询用户在起止时间内完成的各种组合的数量
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	List<UserCompositionDTO> userCompositionCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("userId")String userId, @Param("taskId")String taskId, @Param("type") Integer type);

	/**
	 * 查询一个标注大任务在起止时间完成各种组合的数量或者还可以做的数量
	 * @param env
	 * @param startTime
	 * @param endTime
	 * @param taskId
	 * @param type
	 * @return
	 */
	List<TaskCompositionDTO> taskCompositionCount(@Param("env")String env,@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("taskId")String taskId, @Param("status") Integer status, @Param("type") Integer type);

	/**
	 * 查询一个标注大任务在起止时间完成各种组合的数量或者还可以做的数量
	 * @param startTime
	 * @param endTime
	 * @param taskId
	 * @param type
	 * @return
	 */
	List<TaskCompositionDTO> taskCompositionCount2(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("taskId")String taskId, @Param("status") Integer status, @Param("type") Integer type);

	/**
	 * 查询一个标注大任务在起止时间完成各种组合的错误数量
	 * @param startTime
	 * @param endTime
	 * @param taskId
	 * @param type
	 * @return
	 */
	List<TaskCompositionDTO> taskCompositionWrongCount2(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("taskId")String taskId, @Param("type") Integer type);

	/**
	 * 查询用户在一段时间内质检的数量和速度
	 * @param startTime
	 * @param endTime
	 * @param userId
	 * @return
	 */
	List<UserInspectionDTO> userInspectionCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("userId")String userId);

	/**
	 * 通过标注子任务id获取专家
	 * @param id
	 * @return
	 */
	Expert getExpertByLabelTaskId(Long id);

	/**
	 * 查询用户在起止时间内完成的各种组合的错误数量
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	List<UserCompositionDTO> userCompositionWrongCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("userId")String userId, @Param("taskId")String taskId, @Param("type") Integer type);
}
