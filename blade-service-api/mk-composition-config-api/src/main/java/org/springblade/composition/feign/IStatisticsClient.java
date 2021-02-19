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
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.task.entity.LabelTask;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Notice Feign接口类
 *
 * @author Chill
 */
@FeignClient(value = LauncherConstant.MKAPP_COMPOSITION_CONFIG_NAME)
public interface IStatisticsClient {

	String API_PREFIX = "/client";
	String STATISTICS_INITIALIZE_LABELTASK = API_PREFIX + "/statistics/initialize-labeltask";
	String STATISTICS_INITIALIZE_REALSET_LABELTASK = API_PREFIX + "/statistics/initialize-realset-labeltask";
	String QUERY_BASICINFO_STATUS = API_PREFIX + "/query-basicinfo-status";

	/**
	 * 初始化任务统计
	 */
	@GetMapping(STATISTICS_INITIALIZE_LABELTASK)
	R initializeLabelTask(@RequestParam Long taskId);

	/**
	 * 初始化一条任务的统计
	 */
	@PostMapping(STATISTICS_INITIALIZE_REALSET_LABELTASK)
	R initializeRealSetLabelTask(@RequestBody LabelTask labelTask, @RequestParam Map<String, String> compositionLabelMap);

	/**
	 * 查询基本信息状态
	 */
	@GetMapping(QUERY_BASICINFO_STATUS)
	R<Kv> queryBasicInfoStatus(@RequestParam Long labelTaskId, @RequestParam Long templateId, @RequestParam Long compositionId);
}
