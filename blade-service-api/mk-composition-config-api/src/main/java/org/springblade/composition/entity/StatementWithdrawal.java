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

import java.time.LocalDateTime;

/**
 * 提现单实体类
 *
 * @author BladeX
 * @since 2021-03-04
 */
@Data
@TableName("mk_statement_withdrawal")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "StatementWithdrawal对象", description = "提现单")
public class StatementWithdrawal extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	* 订单ID
	*/
		@ApiModelProperty(value = "订单ID")
		private Long orderId;
	/**
	* 用户ID
	*/
		@ApiModelProperty(value = "用户ID")
		private Long userId;
	/**
	* 支付时间
	*/
		@ApiModelProperty(value = "支付时间")
		private LocalDateTime payTime;
	/**
	* 充值类型 1.购买 2.充值
	*/
		@ApiModelProperty(value = "充值类型 1.购买 2.充值")
		private Boolean type;
	/**
	* 银行名
	*/
		@ApiModelProperty(value = "银行名")
		private String accountBank;
	/**
	* 银行账户名
	*/
		@ApiModelProperty(value = "银行账户名")
		private String accountName;
	/**
	* 提款银行账户号
	*/
		@ApiModelProperty(value = "提款银行账户号")
		private String withdrawalAccount;
	/**
	* 支付ID
	*/
		@ApiModelProperty(value = "支付ID")
		private Long withdrawalId;
	/**
	* 支付状态 1.未支付 2.已支付
	*/
		@ApiModelProperty(value = "支付状态 1.未支付 2.已支付")
		private Boolean payStatus;
	/**
	* 内容
	*/
		@ApiModelProperty(value = "内容")
		private String content;
	/**
	* 处理意见
	*/
		@ApiModelProperty(value = "处理意见")
		private String handlingIdea;
	/**
	* 处理时间
	*/
		@ApiModelProperty(value = "处理时间")
		private Long handlingTime;
	/**
	* 支付通道
	*/
		@ApiModelProperty(value = "支付通道")
		private String channel;
	/**
	* 银行账号
	*/
		@ApiModelProperty(value = "银行账号")
		private String bankAccount;
	/**
	* 金额
	*/
		@ApiModelProperty(value = "金额")
		private Float amount;
	/**
	* 备注
	*/
		@ApiModelProperty(value = "备注")
		private String remark;


}
