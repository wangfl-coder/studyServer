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
package org.springblade.composition.service;

import org.springblade.composition.entity.Composition;
import org.springblade.composition.entity.Template;
import org.springblade.composition.entity.TemplateComposition;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.tool.api.R;

import javax.validation.constraints.NotEmpty;
import java.util.List;


/**
 * 服务类
 *
 * @author Kailun
 */
public interface ITemplateService extends BaseService<Template> {
	/**
	 * 构建模板下的组合
	 *
	 * @param templateCompositions templateComposition集合
	 * @return 是否成功
	 */
	boolean compose(@NotEmpty List<TemplateComposition> templateCompositions);

	/**
	 * 删除模板
	 * @param ids
	 * @return
	 */
	boolean remove(String ids);

	/**
	 * 模板对应的所有组合
	 * @param templateId 模版Id
	 * @return
	 */
	List<Composition> allCompositions(Long templateId);
}
