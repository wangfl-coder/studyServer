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


import org.springblade.common.constant.LauncherConstant;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.entity.Template;
import org.springblade.core.tool.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Notice Feign接口类
 *
 * @author Chill
 */
@FeignClient(value = LauncherConstant.MKAPP_COMPOSITION_CONFIG_NAME)
public interface ITemplateClient {

	String API_PREFIX = "/client";
	String ALL_COMPOSITIONS = API_PREFIX + "/mk-composition-config/all-compositions";
	String GET_TEMPLATE_BY_ID = API_PREFIX + "/mk-composition-config/get-template-by-id";

	/**
	 * 学者信息是否完整
	 */
	@GetMapping(ALL_COMPOSITIONS)
	R<List<Composition>> allCompositions(@RequestParam("id") Long templateId);

	/**
	 * 获取模版
	 */
	@GetMapping(GET_TEMPLATE_BY_ID)
	R<Template> getTemplateById(@RequestParam("id") Long templateId);
}
