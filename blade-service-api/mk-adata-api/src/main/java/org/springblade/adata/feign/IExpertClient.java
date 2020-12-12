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

import org.springblade.adata.entity.Expert;
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
public interface IExpertClient {

	String API_PREFIX = "/client";
	String GET_EXPERT = API_PREFIX + "/mk-adata/expert";
	String GET_EXPERT_LIST = API_PREFIX + "/mk-adata/expert-list";
	String SAVE_EXPERT = API_PREFIX + "/mk-adata/save-expert";
	String REMOVE_EXPERT = API_PREFIX + "/mk-adata/remove-expert";
	String SAVE_EXPERT_BASE = API_PREFIX + "/mk-adata/expert-base-import";

	/**
	 * 获取学者信息
	 *
	 * @param expert 学者实体
	 * @return
	 */
	@PostMapping(GET_EXPERT)
	R<Expert> detail(@RequestBody Expert expert);

	/**
	 * 获取学者列表信息
	 *
	 * @param expert 学者实体
	 * @return
	 */
	@PostMapping(GET_EXPERT_LIST)
	List<Long> detail_list(@RequestBody Expert expert);

	/**
	 * 保存学者信息
	 *
	 * @param expert 学者实体
	 * @return
	 */
	@PostMapping(SAVE_EXPERT)
	R saveExpert(@RequestBody Expert expert);

	/**
	 * 导入智库中所有学者
	 */
	@GetMapping(SAVE_EXPERT_BASE)
	R importExpertBase(@RequestParam(value = "ebId") String expertBaseId, @RequestParam(value = "taskId") Long taskId);
}
