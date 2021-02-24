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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springblade.core.mp.base.BaseEntity;
import org.springblade.core.tenant.mp.TenantEntity;

/**
 * 实体类
 *
 * @author Chill
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("mk_auto_inspection")
@ApiModel(value = "AutoInspection对象", description = "AutoInspection对象")
public class AutoInspection extends TenantEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 子任务id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "任务id")
	private Long TaskId;

	/**
	 * 学者id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "学者id")
	private Long expertId;

	/**
	 * 备注
	 */
	@ApiModelProperty(value = "备注")
	private String remark;

	/**
	 * 标注类型
	 */
	@ApiModelProperty(value = "标注类型")
	private Integer type ;

	/**
	 * 组合id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "子任务id")
	private Long compositionId;

	/**
	 * 标注是否正确
	 */
	@ApiModelProperty(value = "标注是否正确")
	private Integer isCompositionTrue ;

	/**
	 * 标注时间
	 */
	@ApiModelProperty(value = "标注时间")
	private Integer time;

	/**
	 * 时间戳
	 */
	@ApiModelProperty(value = "时间戳")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long realSetId;

}
