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
package org.springblade.adata.feign;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.core.mp.support.BladePage;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.adata.service.IExpertService;
import org.springblade.core.tool.api.R;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.entity.UserInfo;
import org.springblade.system.user.entity.UserOauth;
import org.springblade.system.user.enums.UserEnum;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
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
public class ExpertClient implements IExpertClient {

	private final IExpertService service;

	@Override
	@PostMapping(GET_EXPERT)
	public R<Expert> detail(@RequestBody Expert expert) {
		Expert detail = service.getOne(Condition.getQueryWrapper(expert));
		return R.data(detail);
	}

	@Override
	@PostMapping(GET_EXPERT_LIST)
	public List<Long> detail_list(@RequestBody Expert expert) {
		List<Expert> details = service.list(Condition.getQueryWrapper(expert));
		List<Long> ids = new ArrayList<>();
		details.forEach(detail -> ids.add(detail.getId()));
		return ids;
	}

	@Override
	@PostMapping(SAVE_EXPERT)
	public R saveExpert(@RequestBody Expert expert) {
		return R.status(service.saveOrUpdate(expert));
	}

	@Override
	@GetMapping(SAVE_EXPERT_BASE)
	public R importExpertBase(@RequestParam(value = "ebId") String expertBaseId, @RequestParam(value = "taskId") Long taskId) {
		Boolean res = service.importExpertBase(expertBaseId, taskId);
		return R.status(res);
	}

}
