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
import org.apache.ibatis.annotations.Param;
import org.springblade.composition.entity.AnnotationDataErrata;
import org.springblade.composition.mapper.AnnotationDataErrataMapper;
import org.springblade.composition.service.AnnotationDataErrataService;
import org.springblade.composition.vo.AnnotationCompositionErrataVO;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 服务实现类
 *
 * @author KaiLun
 */
@Service
@AllArgsConstructor
public class AnnotationDataErrataServiceImpl extends BaseServiceImpl<AnnotationDataErrataMapper, AnnotationDataErrata> implements AnnotationDataErrataService {

	AnnotationDataErrataMapper annotationDataErrataMapper;

	public List<AnnotationCompositionErrataVO> getAnnotationCompositionErrataList(Map<String, Object> annotationDataErrata, Integer offset, Integer pageSize) {
		String labelerIdStr = (String)annotationDataErrata.get("labelerId");
		String compositionName = (String)annotationDataErrata.get("compositionName");
		String startTime = (String)annotationDataErrata.get("startTime");
		String endTime = (String)annotationDataErrata.get("endTime");
		List<AnnotationCompositionErrataVO> list = annotationDataErrataMapper.getAnnotationCompositionErrataList(Long.valueOf(labelerIdStr),
			compositionName,
			startTime,
			endTime,
			offset,
			pageSize);
		list.forEach(annotationCompositionErrataVO -> {
			if (annotationCompositionErrataVO.getExpertName() == null)
				annotationCompositionErrataVO.setExpertName(annotationCompositionErrataVO.getRealSetExpertName());
		});
		return list;
	}

	public List<AnnotationCompositionErrataVO> getAnnotationCompositionErrataAll(Map<String, Object> annotationDataErrata) {
		String labelerIdStr = (String)annotationDataErrata.get("labelerId");
		String compositionName = (String)annotationDataErrata.get("compositionName");
		String startTime = (String)annotationDataErrata.get("startTime");
		String endTime = (String)annotationDataErrata.get("endTime");
		return annotationDataErrataMapper.getAnnotationCompositionErrataAll(Long.valueOf(Long.valueOf(labelerIdStr)), compositionName, startTime, endTime);
	}
}
