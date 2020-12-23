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
import org.springblade.adata.entity.ExpertBase;
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
public interface IExpertBaseClient {

	String API_PREFIX = "/client";
	String EXPERTBASE = API_PREFIX + "/dept";
	String EXPERTBASE_IDS = API_PREFIX + "/dept-ids";
	String EXPERTBASE_NAME = API_PREFIX + "/dept-name";
	String EXPERTBASE_NAMES = API_PREFIX + "/dept-names";
	String EXPERTBASE_CHILD = API_PREFIX + "/dept-child";

	/**
	 * 获取部门
	 *
	 * @param id 主键
	 * @return ExpertBase
	 */
	@GetMapping(EXPERTBASE)
	R<ExpertBase> getExpertBase(@RequestParam("id") Long id);

	/**
	 * 获取部门id
	 *
	 * @param tenantId  租户id
	 * @param deptNames 部门名
	 * @return 部门id
	 */
	@GetMapping(EXPERTBASE_IDS)
	R<String> getExpertBaseIds(@RequestParam("tenantId") String tenantId, @RequestParam("deptNames") String deptNames);

	/**
	 * 获取部门名
	 *
	 * @param id 主键
	 * @return 部门名
	 */
	@GetMapping(EXPERTBASE_NAME)
	R<String> getExpertBaseName(@RequestParam("id") Long id);

	/**
	 * 获取部门名
	 *
	 * @param deptIds 主键
	 * @return
	 */
	@GetMapping(EXPERTBASE_NAMES)
	R<List<String>> getExpertBaseNames(@RequestParam("deptIds") String deptIds);

	/**
	 * 获取子部门ID
	 *
	 * @param deptId
	 * @return
	 */
	@GetMapping(EXPERTBASE_CHILD)
	R<List<ExpertBase>> getExpertBaseChild(@RequestParam("deptId") Long deptId);

}
