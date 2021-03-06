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

import org.springblade.adata.entity.RealSetExpert;
import org.springblade.common.constant.LauncherConstant;
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
@FeignClient(
	value = LauncherConstant.MKAPP_ADATA_NAME
)
public interface IRealSetCompositionClient {

	String API_PREFIX = "/client_real_set_composition";
	String SAVE_REAL_SET_COMPOSITION = API_PREFIX + "/mk-adata/real_set_composition";

	/**
	 * 生成任务的真题领取数据
	 * @param templateId
	 * @param taskId
	 * @param realSetExpertList
	 * @return
	 */
	@GetMapping(SAVE_REAL_SET_COMPOSITION)
	R save_real_set_composition(@RequestParam("templateId") Long templateId, @RequestParam("taskId") Long taskId,@RequestParam("realSetExpertList") List<RealSetExpert> realSetExpertList);



}
