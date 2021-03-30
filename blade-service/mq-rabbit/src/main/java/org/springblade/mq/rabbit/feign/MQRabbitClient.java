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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.mq.rabbit.constant.RabbitConstant;
import org.springblade.mq.rabbit.IMQRabbitClient;
import org.springblade.mq.rabbit.message.MessageStruct;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

/**
 * 用户服务Feign实现类
 *
 * @author Chill
 */
@NonDS
@RestController
@AllArgsConstructor
public class MQRabbitClient implements IMQRabbitClient {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Override
	@PostMapping(PREPROCESS_PURE_SUP_PERSON)
	public R<Boolean> preprocessPureSupPerson(@RequestBody List<String> idList) {
		idList.forEach(id -> {
			rabbitTemplate.convertAndSend(
				RabbitConstant.DIRECT_MODE_QUEUE_PREPROCESS_PURE_SUP_PERSON,
				new MessageStruct(id));
		});

		return R.status(true);
	}
}
