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
import org.springblade.adata.entity.ExpertMerge;
import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
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
public interface IExpertMergeClient {

	String API_PREFIX = "/client_expert_merge";
	String GET_EXPERT = API_PREFIX + "/mk-adata/expert";
	String GET_EXPERTS_BY_TASKID = API_PREFIX + "/mk-adata/experts-by-taskid";
	String GET_EXPERTS_ID = API_PREFIX + "/mk-adata/experts-id";
	String SAVE_EXPERT = API_PREFIX + "/mk-adata/save-expert";
	String REMOVE_EXPERT = API_PREFIX + "/mk-adata/remove-expert";
	String SAVE_EXPERT_BASE = API_PREFIX + "/mk-adata/expert-base-import";
	String IS_INFO_COMPLETE = API_PREFIX + "/mk-adata/is-info-complete";
	String FETCH_EXPERT_DETAIL = API_PREFIX + "/mk-adata/fetch-expert-detail";

	/**
	 * 获取学者信息
	 *
	 * @param expert 学者实体
	 * @return
	 */
	@PostMapping(GET_EXPERT)
	R<ExpertMerge> detail(@RequestBody ExpertMerge expert);

	/**
	 * 通过任务id获取学者列表
	 *
	 * @param taskId 任务id
	 * @return
	 */
	@GetMapping(GET_EXPERTS_BY_TASKID)
	R<List<ExpertMerge>> getExpertsByTaskId(@RequestParam Long taskId);

	/**
	 * 通过任务id获取学者id列表
	 *
	 * @param taskId 任务id
	 * @return
	 */
	@GetMapping(GET_EXPERTS_ID)
	R<List<String>> getExpertsId(@RequestParam Long taskId);

	/**
	 * 保存学者信息
	 *
	 * @param expert 学者实体
	 * @return
	 */
	@PostMapping(SAVE_EXPERT)
	R saveExpert(@RequestBody ExpertMerge expert);

	/**
	 * 导入智库中所有学者
	 */
	@GetMapping(SAVE_EXPERT_BASE)
	R importExpertBase(@RequestParam("ebId") String expertBaseId, @RequestParam("taskId") Long taskId);

	/**
	 * 学者信息是否完整
	 */
	@GetMapping(IS_INFO_COMPLETE)
	R<Kv> isInfoComplete(@RequestParam("expertId") Long expertId, @RequestParam("templateId") Long templateId);

	/**
	 * 获取Aminer学者信息
	 */
	@GetMapping(FETCH_EXPERT_DETAIL)
	R<ExpertMerge> fetchExpertDetail(@RequestParam("tenantId") String tenantId, @RequestParam("expertId") String expertId, @RequestParam("taskId") Long taskId);
}
