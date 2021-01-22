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
package org.springblade.adata.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.excel.ExpertExcel;
import org.springblade.adata.vo.UserRemarkVO;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.system.user.entity.User;


import java.util.List;
import java.util.Map;

/**
 * 服务类
 *
 * @author Chill
 */
public interface IExpertService extends BaseService<Expert> {

	/**
	 * 详情
	 * @param id
	 * @return
	 */
	String fetchDetail(String id);

	/**
	 * 列表
	 * @param params
	 * @param query
	 * @return
	 */
	String fetchList(Map<String, Object> params, Query query);

	/**
	 * 导入
	 * @param id
	 * @return
	 */
	Boolean importDetail(String id, Long taskId);



	/**
	 * 导入智库下所有学者
	 * @param id
	 * @return
	 */
	Boolean importExpertBase(String id, Long taskId);


	/**
	 * 学者信息是否完整
	 * @param expertId
	 * @return
	 */
	Kv isInfoComplete(Long expertId, Long templateId);

	/**
	 * 根据更新人id查询备注人姓名
	 */
	User queryNameById(Long userId);

	/**
	 * 根据专家id从标注小任务表查流程实例id
	 * @param personId 标注大任务id
	 * @return
	 */
	List<UserRemarkVO> userRemark(Long personId);

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
	List<ExpertExcel> exportExpert(@Param("ew") Wrapper<Expert> queryWrapper);

	/**
	 * 导入专家数据
	 *
	 * @param data
	 * @param isCovered
	 * @return
	 */
	void importExpert(List<ExpertExcel> data, Boolean isCovered);
}
