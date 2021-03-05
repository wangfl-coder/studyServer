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
 * 收款单实体类
 *
 * @author BladeX
 * @since 2021-03-04
 */
@Data
@TableName("mk_statement_deposit")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "StatementDeposit对象", description = "收款单")
public class StatementDeposit extends BaseEntity {

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
	* 金额
	*/
		@ApiModelProperty(value = "金额")
		private Float amount;
	/**
	* 支付时间
	*/
		@ApiModelProperty(value = "支付时间")
		private LocalDateTime paymentTime;
	/**
	* 充值类型 1.购买 2.充值
	*/
		@ApiModelProperty(value = "充值类型 1.购买 2.充值")
		private Boolean type;
	/**
	* 支付ID
	*/
		@ApiModelProperty(value = "支付ID")
		private Long paymentId;
	/**
	* 支付状态 1.未支付 2.已支付
	*/
		@ApiModelProperty(value = "支付状态 1.未支付 2.已支付")
		private Boolean payStatus;
	/**
	* 备注
	*/
		@ApiModelProperty(value = "备注")
		private String remark;


}
