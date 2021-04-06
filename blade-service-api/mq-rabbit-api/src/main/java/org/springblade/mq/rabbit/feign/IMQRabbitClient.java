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
package org.springblade.mq.rabbit.feign;


import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.tool.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * MQRabbit Feign接口类
 *
 * @author Chill
 */
@FeignClient(
	value = LauncherConstant.APPLICATION_RABBIT_NAME
)
public interface IMQRabbitClient {

	String API_PREFIX = "/client";
	String PREPROCESS_PURE_SUP_PERSON = API_PREFIX + "/preprocess-pure-sup-person";


	/**
	 * 预处理学者
	 *
	 * @param idList id列表
	 * @return
	 */
	@PostMapping(PREPROCESS_PURE_SUP_PERSON)
	R<Boolean> preprocessPureSupPerson(@RequestBody List<String> idList);

}
