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
import org.springblade.composition.entity.AnnotationDataErrata;
import org.springblade.composition.vo.AnnotationCompositionErrataVO;

import java.util.List;

/**
 * Mapper 接口
 *
 * @author KaiLun
 */
public interface AnnotationDataErrataMapper extends BaseMapper<AnnotationDataErrata> {

	/**
	 * 获取标注组合勘误列表
	 * @param labelerId
	 * @param compositionName
	 * @param offset
	 * @param pageSize
	 *
	 * @return
	 */
	List<AnnotationCompositionErrataVO> getAnnotationCompositionErrataList(@Param("labelerId")Long labelerId, @Param("compositionName")String compositionName, @Param("startTime")String startTime, @Param("endTime")String endTime, @Param("offset")Integer offset, @Param("pageSize")Integer pageSize);

	/**
	 * 获取标注组合勘误列表
	 * @param labelerId
	 * @param compositionName
	 *
	 * @return
	 */
	List<AnnotationCompositionErrataVO> getAnnotationCompositionErrataAll(@Param("labelerId")Long labelerId, @Param("compositionName")String compositionName, @Param("startTime")String startTime, @Param("endTime")String endTime);
}
