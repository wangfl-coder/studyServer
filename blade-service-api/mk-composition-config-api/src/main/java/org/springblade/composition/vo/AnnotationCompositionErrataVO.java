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
package org.springblade.composition.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.composition.entity.AnnotationData;
import org.springblade.composition.entity.AnnotationDataErrata;

import java.io.Serializable;
import java.util.Date;


/**
 * 视图实体类
 *
 * @author Chill
 */
@Data
@ApiModel(value = "AnnotationCompositionErrataVO对象", description = "标注组合勘误视图对象")
public class AnnotationCompositionErrataVO implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 标注子任务id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "标注子任务id")
	private Long subTaskId;

	/**
	 * 组合id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "组合id")
	private Long compositionId;

	/**
	 * 组合名
	 */
	@ApiModelProperty(value = "组合名")
	private String compositionName;

	/**
	 * 专家id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "专家id")
	private Long expertId;

	/**
	 * 专家名
	 */
	@ApiModelProperty(value = "专家名")
	private String expertName;

	/**
	 * 真集专家名
	 */
	@ApiModelProperty(value = "真集专家名")
	private String realSetExpertName;

	/**
	 * 更新时间
	 */
	@ApiModelProperty(value = "更新时间")
	private Date updateTime;

	/**
	 * 数据来源
	 * 1、基本信息标注
	 * 2、真题标注
	 */
	@ApiModelProperty(value = "数据来源")
	private Integer source;

}
