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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.RealSetExpert;
import org.springblade.adata.service.IExpertService;
import org.springblade.adata.service.IRealSetExpertService;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
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
public class RealSetExpertClient implements IRealSetExpertClient {

	private final IRealSetExpertService service;

	@Override
	@PostMapping(GET_EXPERT)
	public R<RealSetExpert> detail(@RequestBody RealSetExpert expert) {
		RealSetExpert detail = service.getOne(Condition.getQueryWrapper(expert));
		return R.data(detail);
	}

	@Override
	@GetMapping(GET_EXPERTS_BY_TASKID)
	public R<List<RealSetExpert>> getExpertsByTaskId(@RequestParam Long taskId) {
		List<RealSetExpert> experts = service.list(Wrappers.<RealSetExpert>query().lambda().eq(RealSetExpert::getTaskId, taskId));
		return R.data(experts);
	}

	@Override
	@GetMapping(GET_EXPERTS_ID)
	public R<List<String>> getExpertsId(@RequestParam Long taskId) {
		List<String> experts = service.getExpertsId(taskId);
		return R.data(experts);
	}

	@Override
	@PostMapping(SAVE_EXPERT)
	public R saveExpert(@RequestBody RealSetExpert expert) {
		return R.status(service.saveOrUpdate(expert));
	}

	@Override
	@GetMapping(SAVE_EXPERT_BASE)
	public R importExpertBase(@RequestParam(value = "ebId") String expertBaseId, @RequestParam(value = "taskId") Long taskId) {
		Boolean res = service.importExpertBase(expertBaseId, taskId);
		return R.status(res);
	}


}
