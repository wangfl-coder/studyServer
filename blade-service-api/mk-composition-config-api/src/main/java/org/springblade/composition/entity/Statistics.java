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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;
import org.springblade.core.tenant.mp.TenantEntity;

/**
 * 实体类
 *
 * @author Chill
 */
@Data
@TableName("mk_statistics")
@ApiModel(value = "Statistics", description = "Statistics对象")
public class Statistics extends TenantEntity {

	private static final long serialVersionUID = 1L;

//	/**
//	 * 更新部门
//	 */
//	@JsonSerialize(using = ToStringSerializer.class)
//	@ApiModelProperty(value = "更新部门")
//	private Long updateDept;

	/**
	 * 子任务id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "子任务id")
	private Long subTaskId;

	/**
	 * 模板id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "模板id")
	private Long templateId;

	/**
	 * 组合id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "组合id")
	private Long compositionId;

	/**
	 * 标注或质检人id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "标注或质检人id")
	private Long userId;

	/**
	 * 类型
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "类型")
	private Integer type;

	/**
	 * 标注或质检所用秒数
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "标注或质检所用秒数")
	private Integer time;

	/**
	 * 标注或质检是否正确
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "标注或质检是否正确")
	private Integer isTrue;

	/**
	 * 备注
	 */
	@ApiModelProperty(value = "备注")
	private String remark;

	/**
	 * 一个任务中组合提交（完成）数量
	 */
	@TableField(exist = false)
	@ApiModelProperty(value = "一个任务中组合提交（完成）数量")
	private Integer compositionSubmitCount;

	/**
	 * 是否正确 大于0为错误 数字代表为什么错 1.质检员 2.真题 3.多人对比
	 */
	@ApiModelProperty(value = "大于0为错误 数字代表为什么错 1.质检员 2.真题 3.多人对比")
	private Integer isWrong;

	/**
	 * 反馈状态 0.未审核 1.审核通过 2.驳回
	 */
	@ApiModelProperty(value = "0.未反馈 1.已提交未处理 2.已提交处理中 3.已提交处理完毕")
	private Integer feedbackStatus;


//	/**
//	 * 积分
//	 */
//	@ApiModelProperty(value = "积分")
//	private Integer points;
}
