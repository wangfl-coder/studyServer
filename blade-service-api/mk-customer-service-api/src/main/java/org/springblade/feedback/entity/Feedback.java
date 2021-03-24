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
package org.springblade.feedback.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.mp.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * 实体类
 *
 * @author BladeX
 * @since 2021-03-22
 */
@Data
@TableName("mk_customer_service")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "Feedback对象", description = "Feedback对象")
public class Feedback extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 任务ID
	 */
	@ApiModelProperty(value = "任务ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long taskId;
	/**
	 * 子任务ID
	 */
	@ApiModelProperty(value = "子任务ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long subTaskId;
	/**
	 * 专家ID
	 */
	@ApiModelProperty(value = "专家ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long personId;
	/**
	 * 组合ID
	 */
	@ApiModelProperty(value = "组合ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long compositionId;
	/**
	 * 申述字段
	 */
	@ApiModelProperty(value = "申述字段")
	private String field;
	/**
	 * 申述内容
	 */
	@ApiModelProperty(value = "申述内容")
	private String description;
	/**
	 * 正确值
	 */
	@ApiModelProperty(value = "正确值")
	private String correctValue;
	/**
	 * 所填值
	 */
	@ApiModelProperty(value = "所填值")
	private String fillValue;
	/**
	 * 证据截图路径
	 */
	@ApiModelProperty(value = "证据截图路径")
	private String picture;
	/**
	 * 审核员
	 */
	@ApiModelProperty(value = "审核员")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long auditUser;
	/**
	 * 审核日期
	 */
	@ApiModelProperty(value = "审核日期")
	private LocalDateTime auditTime;
	/**
	 * 审核意见
	 */
	@ApiModelProperty(value = "审核意见")
	private String auditRemark;
	/**
	 * 截止日期
	 */
	@ApiModelProperty(value = "截止日期")
	private LocalDateTime deadline;


}
