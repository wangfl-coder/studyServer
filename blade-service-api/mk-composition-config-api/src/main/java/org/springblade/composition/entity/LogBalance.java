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
package org.springblade.composition.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.mp.base.BaseEntity;

/**
 * 余额日志实体类
 *
 * @author BladeX
 * @since 2021-03-04
 */
@Data
@TableName("mk_log_balance")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "LogBalance对象", description = "余额日志")
public class LogBalance extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	* 用户id
	*/
		@ApiModelProperty(value = "用户id")
		private Long userId;
	/**
	* 类型 1.提现 2.充值
	*/
		@ApiModelProperty(value = "类型 1.提现 2.充值")
		private Integer type;
	/**
	* 金额变动 负数为支付 正数为充值
	*/
		@ApiModelProperty(value = "金额变动 负数为支付 正数为充值")
		private Float amount;
	/**
	* 余额
	*/
		@ApiModelProperty(value = "余额")
		private Float amountLog;
	/**
	* 备注
	*/
		@ApiModelProperty(value = "备注")
		private String remark;


}
