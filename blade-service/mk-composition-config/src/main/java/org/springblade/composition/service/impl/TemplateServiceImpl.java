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


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.entity.Template;
import org.springblade.composition.entity.TemplateComposition;
import org.springblade.composition.mapper.TemplateCompositionMapper;
import org.springblade.composition.mapper.TemplateMapper;
import org.springblade.composition.service.ICompositionService;
import org.springblade.composition.service.ITemplateCompositionService;
import org.springblade.composition.service.ITemplateService;

import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.api.ResultCode;
import org.springblade.core.tool.utils.Func;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;


/**
 * 服务实现类
 *
 * @author KaiLun
 */
@Service
@Validated
@AllArgsConstructor
public class TemplateServiceImpl extends BaseServiceImpl<TemplateMapper, Template> implements ITemplateService {
	private final ITemplateCompositionService templateCompositionService;
	private final ICompositionService compositionService;
	private final TemplateCompositionMapper templateCompositionMapper;
	/**
	 * 构建模板下的组合
	 * @param templateCompositions templateComposition集合
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean compose(@NotEmpty List<TemplateComposition> templateCompositions) {
		// 删除模板下原来的组合
		List<Long> templateIds = new ArrayList<>();
		templateCompositions.forEach(templateComposition -> {
			templateIds.add(templateComposition.getTemplateId());
		});
		templateCompositionService.remove(Wrappers.<TemplateComposition>update().lambda().in(TemplateComposition::getTemplateId, templateIds));
		// 新增组合
		templateCompositionService.saveBatch(templateCompositions);
		return true;
	}

	/**
	 * 删除模板，相应的组合也删除
	 * @param ids
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean remove(String ids) {
		// 删除模板下原来的组合
		List<Long> templateIds = Func.toLongList(ids);
		templateCompositionService.remove(Wrappers.<TemplateComposition>update().lambda().in(TemplateComposition::getTemplateId, templateIds));
		removeByIds(templateIds);
		return true;
	}


	@Override
	public List<Composition> allCompositions(Long templateId) {
		// TemplateComposition templateComposition = new TemplateComposition();
		// templateComposition.setTemplateId(templateId);
		// List<TemplateComposition> templateCompositionList = templateCompositionService.list(Condition.getQueryWrapper(templateComposition));
		// if (templateCompositionList.isEmpty()){
		// 	return null;
		// }
		// List<Long> compositionIdList = new ArrayList<>();
		// templateCompositionList.forEach(templateComposition1 -> compositionIdList.add(templateComposition1.getCompositionId()));
		// List<Composition> compositionList = compositionService.listByIds(compositionIdList);
		// if (compositionList.isEmpty()){
		// 	return null;
		// }
		// return compositionList;
		return templateCompositionMapper.allCompositions(templateId);
	}
}
