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
package org.springblade.adata.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.mp.base.BaseEntity;

/**
 * 实体类
 *
 * @author Chill
 */
@Data
@TableName("mk_real_set_composition")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "RealSetComposition对象", description = "RealSetComposition对象")
public class RealSetComposition extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 任务id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "任务id")
	private Long taskId;

	/**
	 * 专家id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "专家id")
	private Long expertId;

	/**
	 * 组合id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "组合id")
	private Long CompositionId;

	/**
	 * 是否领取标注
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "是否领取标注")
	private int annotation;


	/**
	 * 备注
	 */
	@ApiModelProperty(value = "备注")
	private String remark;

}
