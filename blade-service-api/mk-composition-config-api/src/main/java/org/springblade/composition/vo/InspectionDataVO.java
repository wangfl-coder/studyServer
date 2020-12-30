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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.composition.entity.AnnotationData;
import org.springblade.composition.entity.InspectionData;

import java.util.List;


/**
 * 视图实体类
 *
 * @author Kailun
 */
@Data
@ApiModel(value = "AnnotationDataVO", description = "AnnotationDataVO")
public class InspectionDataVO {
	private static final long serialVersionUID = 1L;
	/**
	 * 质检数据列表
	 */
	@ApiModelProperty(value = "质检数据列表")
	private List<InspectionData> inspectionDataList;

	/**
	 * 质检的用时
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "质检的用时")
	private Integer time;


	/**
	 * sub_task_id(质检的子任务）
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "子任务id")
	private Long subTaskId;

	/**
	 * 学者id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "学者id")
	private Long expertId;

	/**
	 * label_task_id(标注的子任务）
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "标注子任务id")
	private Long labelTaskId;

	/**
	 * 质检类型
	 */
	@ApiModelProperty(value = "质检类型")
	private Integer type ;

	/**
	 * 质检图片链接
	 */
	@ApiModelProperty(value = "质检图片链接")
	private String picture;

}
