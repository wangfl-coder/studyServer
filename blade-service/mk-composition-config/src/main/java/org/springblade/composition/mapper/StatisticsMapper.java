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

import com.baomidou.mybatisplus.annotation.SqlParser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springblade.adata.entity.Expert;
import org.springblade.composition.dto.UserInspectionDTO;
import org.springblade.composition.dto.statistics.*;
import org.springblade.composition.entity.Statistics;

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
	List<UserComposition> userCompositionCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("tenantId")String tenantId, @Param("userId")String userId, @Param("taskId")String taskId, @Param("type") Integer type);

	/**
	 * 查询用户在起止时间内完成的各种组合的错误数量
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	List<UserComposition> userCompositionWrongCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("tenantId")String tenantId, @Param("userId")String userId, @Param("taskId")String taskId, @Param("type") Integer type);

	/**
	 * 查询一个标注大任务在起止时间完成各种组合的数量或者还可以做的数量
	 * @param startTime
	 * @param endTime
	 * @param taskId
	 * @param type
	 * @return
	 */
	List<TaskComposition> taskCompositionCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("taskId")String taskId, @Param("status") Integer status, @Param("type") Integer type);

	/**
	 * 查询一个标注大任务在起止时间完成各种组合的数量或者还可以做的数量
	 * @param startTime
	 * @param endTime
	 * @param taskId
	 * @param taskType
	 * @param statisticsType
	 * @return
	 */
	List<TaskComposition> taskCompositionCount2(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("taskId")String taskId, @Param("status") Integer status, @Param("taskType") Integer taskType, @Param("statisticsType") Integer statisticsType);

	/**
	 * 查询一个标注大任务在起止时间完成各种组合的错误数量
	 * @param startTime
	 * @param endTime
	 * @param taskId
	 * @param taskType
	 * @param statisticsType
	 * @return
	 */
	List<TaskComposition> taskCompositionWrongCount2(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("taskId")String taskId, @Param("taskType") Integer taskType, @Param("statisticsType") Integer statisticsType);

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
	 * 查询租户在起止时间内完成的各种组合的数量
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@SqlParser(filter=true)
	List<TenantComposition> tenantCompositionCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("tenantId")String tenantId, @Param("taskId")String taskId, @Param("type") Integer type);

	/**
	 * 查询租户在起止时间内完成的各种组合的错误数量
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@SqlParser(filter=true)
	List<TenantComposition> tenantCompositionWrongCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("tenantId")String tenantId, @Param("taskId")String taskId, @Param("type") Integer type);

	/**
	 * 查询租户在起止时间内完成的各种任务的数量
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@SqlParser(filter=true)
	List<TenantTask> tenantTaskCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("tenantId")String tenantId, @Param("taskId")String taskId, @Param("type") Integer type);

	/**
	 * 查询租户在起止时间内完成的各种任务的错误数量
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@SqlParser(filter=true)
	List<TenantTask> tenantTaskWrongCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("tenantId")String tenantId, @Param("taskId")String taskId, @Param("type") Integer type);

	/**
	 * 查询部门在起止时间内完成的各种组合的数量
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@SqlParser(filter=true)
	List<DeptComposition> deptCompositionCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("tenantId")String tenantId, @Param("deptId")String deptId, @Param("type") Integer type);

	/**
	 * 查询部门在起止时间内完成的各种组合的错误数量
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@SqlParser(filter=true)
	List<DeptComposition> deptCompositionWrongCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("tenantId")String tenantId, @Param("deptId")String deptId, @Param("type") Integer type);

	/**
	 * 查询部门在起止时间内完成的各种任务的数量
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@SqlParser(filter=true)
	List<DeptTask> deptTaskCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("tenantId")String tenantId, @Param("deptId")String deptId, @Param("type") Integer type);

	/**
	 * 查询部门在起止时间内完成的各种任务的错误数量
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@SqlParser(filter=true)
	List<DeptTask> deptTaskWrongCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("tenantId")String tenantId, @Param("deptId")String deptId, @Param("type") Integer type);
}
