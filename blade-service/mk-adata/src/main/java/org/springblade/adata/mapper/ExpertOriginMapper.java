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
package org.springblade.adata.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springblade.adata.entity.ExpertOrigin;
import org.springblade.adata.excel.ExpertExcel;
import org.springblade.adata.vo.UserRemarkVO;

import java.util.List;


/**
 * Mapper 接口
 *
 * @author Chill
 */
public interface ExpertOriginMapper extends BaseMapper<ExpertOrigin> {

	/**
	 * 查询质检完成的需要导出专家信息
	 * @param taskId  标注大任务id
	 * @return
	 */
	List<ExpertOrigin> queryExportExperts(@Param("taskId") Long taskId);

	/**
	 * 根据专家id从标注小任务表查流程实例id
	 * @param personId 标注大任务id
	 * @return
	 */
	List<UserRemarkVO> userRemark(@Param("personId") Long personId);

	/**
	 * 根据专家id从质检小任务表查流程实例id
	 * @param personId 标注大任务id
	 * @return
	 */
	List<UserRemarkVO> userInspectionRemark(@Param("personId") Long personId);

	/**
	 * 获取导出专家数据
	 *
	 * @param queryWrapper
	 * @return
	 */
	List<ExpertExcel> exportExpert(@Param("ew") Wrapper<ExpertOrigin> queryWrapper);

}
