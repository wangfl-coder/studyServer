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
package org.springblade.taskLog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.tenant.mp.TenantEntity;

/**
 * 实体类
 *
 * @author Chill
 */
@Data
@TableName("mk_log_user")
@EqualsAndHashCode(callSuper = true)
public class UserLog extends TenantEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户编号
	 */
	@ApiModelProperty(value = "用户编号")
	private String code;
	/**
	 * 用户平台
	 */
	@ApiModelProperty(value = "用户平台")
	private Integer userType;
	/**
	 * 账号
	 */
	@ApiModelProperty(value = "账号")
	private String account;
	/**
	 * identifier
	 */
	@ApiModelProperty(value = "身份证")
	private String identifier;
	/**
	 * 昵称
	 */
	@ApiModelProperty(value = "昵称")
	private String name;
	/**
	 * 真名
	 */
	@ApiModelProperty(value = "真名")
	private String realName;
	/**
	 * 头像
	 */
	@ApiModelProperty(value = "头像")
	private String avatar;
	/**
	 * 邮箱
	 */
	@ApiModelProperty(value = "邮箱")
	private String email;
	/**
	 * 手机
	 */
	@ApiModelProperty(value = "手机")
	private String mobile;
	/**
	 * 电话
	 */
	@ApiModelProperty(value = "电话")
	private String phone;
	/**
	 * 事件类型1.因准确率屏蔽
	 */
	@ApiModelProperty(value = "事件类型1.因准确率屏蔽")
	private Integer type;
	/**
	 * 操作员
	 */
	@ApiModelProperty(value = "操作员")
	private String operator;
	/**
	 * 备注
	 */
	@ApiModelProperty(value = "备注")
	private String remark;
	/**
	 * 处理类型 1.已处理2.延后处理3.不予解决
	 */
	@ApiModelProperty(value = "处理类型 1.已处理2.延后处理3.不予解决")
	private Integer treat_type;
	/**
	 * 处理意见
	 */
	@ApiModelProperty(value = "备注")
	private String treat_remark;
}
