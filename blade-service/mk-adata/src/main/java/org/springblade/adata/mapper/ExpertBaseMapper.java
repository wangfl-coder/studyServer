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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springblade.adata.entity.ExpertBase;
import org.springblade.adata.vo.ExpertBaseVO;

import java.util.List;
import java.util.Map;

/**
 * ExpertBaseMapper 接口
 *
 * @author Chill
 */
public interface ExpertBaseMapper extends BaseMapper<ExpertBase> {

	/**
	 * 懒加载部门列表
	 *
	 * @param tenantId
	 * @param parentId
	 * @param param
	 * @return
	 */
	List<ExpertBaseVO> lazyList(String tenantId, Long parentId, Map<String, Object> param);

	/**
	 * 获取树形节点
	 *
	 * @param tenantId
	 * @return
	 */
	List<ExpertBaseVO> tree(String tenantId);

	/**
	 * 懒加载获取树形节点
	 *
	 * @param tenantId
	 * @param parentId
	 * @return
	 */
	List<ExpertBaseVO> lazyTree(String tenantId, Long parentId);

	/**
	 * 获取部门名
	 *
	 * @param ids
	 * @return
	 */
	List<String> getExpertBaseNames(Long[] ids);

}
