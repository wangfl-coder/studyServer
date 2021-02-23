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
package org.springblade.composition.feign;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.entity.Template;
import org.springblade.composition.service.ITemplateService;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.api.ResultCode;
import org.springblade.core.tool.support.Kv;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;


/**
 * Notice Feign
 *
 * @author Chill
 */
@NonDS
@ApiIgnore()
@RestController
@AllArgsConstructor
public class TemplateClient implements ITemplateClient {

	private final ITemplateService service;

	@Override
	@GetMapping(ALL_COMPOSITIONS)
	public R allCompositions(@RequestParam("id") Long templateId) {
		List<Composition> compositionList = service.allCompositions(templateId);
		if (compositionList == null) {
			R.data(ResultCode.FAILURE.getCode(), new JSONObject(),"数据库中未找到");
		}
		return R.data(compositionList);
	}

	/**
	 * 获取模版
	 */
	@Override
	@GetMapping(GET_TEMPLATE_BY_ID)
	public R<Template> getTemplateById(@RequestParam("id") Long templateId) {
		Template template = service.getById(templateId);
		return R.data(template);
	}
}
