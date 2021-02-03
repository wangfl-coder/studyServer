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
package org.springblade.adata.wrapper;

import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.RealSetExpert;
import org.springblade.adata.vo.ExpertVO;
import org.springblade.adata.vo.RealSetExpertVO;
import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.utils.BeanUtil;

import java.util.Objects;

/**
 * Expert包装类,返回视图层所需的字段
 *
 * @author Chill
 */
public class RealSetExpertWrapper extends BaseEntityWrapper<RealSetExpert, RealSetExpertVO> {

	public static RealSetExpertWrapper build() {
		return new RealSetExpertWrapper();
	}

	@Override
	public RealSetExpertVO entityVO(RealSetExpert expert) {
		RealSetExpertVO expertVO = Objects.requireNonNull(BeanUtil.copy(expert, RealSetExpertVO.class));
//		String dictValue = DictCache.getValue(DictEnum.NOTICE, noticeVO.getCategory());
//		noticeVO.setCategoryName(dictValue);
		return expertVO;
	}

}
