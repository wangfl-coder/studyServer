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

import lombok.AllArgsConstructor;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.entity.Template;
import org.springblade.composition.entity.TemplateComposition;
import org.springblade.composition.service.ICompositionService;
import org.springblade.composition.service.ITemplateService;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.api.ResultCode;
import org.springblade.core.tool.utils.Func;
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
public class CompositionClient implements ICompositionClient {

	private final ICompositionService service;

	/**
	 * 获取模版
	 */
	@Override
	@GetMapping(GET_COMPOSITION_BY_ID)
	public R<Composition> getById(@RequestParam("id") Long compositionId) {
		Composition composition = service.getById(compositionId);
		return R.data(composition);
	}

	/**
	 * 保存组合
	 */
	@Override
	@PostMapping(SUBMIT_COMPOSITION)
	public R<Boolean> submit(@RequestBody Composition composition) {
		boolean res = service.saveOrUpdate(composition);
		return R.data(res);
	}

	/**
	 * 获取补充信息组合Id
	 */
	@Override
	@GetMapping(GET_COMPLEMENT_COMPOSITION_ID)
	public R<Long> getComplementCompositionId(@RequestParam("tenantId") String tenantId) {
		Composition composition = new Composition();
		composition.setTenantId(tenantId);
		composition.setName("补充信息");
		Composition compositionRes = service.getOne(Condition.getQueryWrapper(composition));
		return R.data(compositionRes.getId());
	}
}
