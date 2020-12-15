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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.composition.entity.AnnotationData;

import java.util.List;


/**
 * 视图实体类
 *
 * @author Kailun
 */
@Data
@ApiModel(value = "AnnotationDataVO", description = "AnnotationDataVO")
public class AnnotationDataVO {
	private static final long serialVersionUID = 1L;
	/**
	 * 标注数据列表
	 */
	@ApiModelProperty(value = "标注数据列表")
	private List<AnnotationData> annotationDataList;

	/**
	 * 标注或质检的用时
	 */
	@ApiModelProperty(value = "标注或质检的用时")
	private Integer time;

	/**
	 * 模板id
	 */
	@ApiModelProperty(value = "模板id")
	private Long templateId;

	/**
	 * 组合id
	 */
	@ApiModelProperty(value = "组合id")
	private Long compositionId;

}
