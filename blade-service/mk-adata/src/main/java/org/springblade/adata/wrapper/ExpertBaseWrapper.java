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

import org.springblade.core.mp.support.BaseEntityWrapper;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.node.ForestNodeMerger;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.system.cache.DictCache;
import org.springblade.adata.cache.ExpertBaseCache;
import org.springblade.adata.entity.ExpertBase;
import org.springblade.system.enums.DictEnum;
import org.springblade.adata.vo.ExpertBaseVO;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 包装类,返回视图层所需的字段
 *
 * @author Chill
 */
public class ExpertBaseWrapper extends BaseEntityWrapper<ExpertBase, ExpertBaseVO> {

	public static ExpertBaseWrapper build() {
		return new ExpertBaseWrapper();
	}

	@Override
	public ExpertBaseVO entityVO(ExpertBase expertBase) {
		ExpertBaseVO expertBaseVO = Objects.requireNonNull(BeanUtil.copy(expertBase, ExpertBaseVO.class));
		if (Func.equals(expertBase.getParentId(), BladeConstant.TOP_PARENT_ID)) {
			expertBaseVO.setParentName(BladeConstant.TOP_PARENT_NAME);
		} else {
			ExpertBase parent = ExpertBaseCache.getExpertBase(expertBase.getParentId());
			expertBaseVO.setParentName(parent.getExpertBaseName());
		}
		String category = DictCache.getValue(DictEnum.ORG_CATEGORY, expertBase.getExpertBaseCategory());
		expertBaseVO.setExpertBaseCategoryName(category);
		return expertBaseVO;
	}


	public List<ExpertBaseVO> listNodeVO(List<ExpertBase> list) {
		List<ExpertBaseVO> collect = list.stream().map(expertBase -> {
			ExpertBaseVO expertBaseVO = BeanUtil.copy(expertBase, ExpertBaseVO.class);
			String category = DictCache.getValue(DictEnum.ORG_CATEGORY, expertBase.getExpertBaseCategory());
			Objects.requireNonNull(expertBaseVO).setExpertBaseCategoryName(category);
			return expertBaseVO;
		}).collect(Collectors.toList());
		return ForestNodeMerger.merge(collect);
	}

	public List<ExpertBaseVO> listNodeLazyVO(List<ExpertBaseVO> list) {
		List<ExpertBaseVO> collect = list.stream().peek(expertBase -> {
			String category = DictCache.getValue(DictEnum.ORG_CATEGORY, expertBase.getExpertBaseCategory());
			Objects.requireNonNull(expertBase).setExpertBaseCategoryName(category);
		}).collect(Collectors.toList());
		return ForestNodeMerger.merge(collect);
	}

}
