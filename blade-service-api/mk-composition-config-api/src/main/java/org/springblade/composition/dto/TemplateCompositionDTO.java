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
package org.springblade.composition.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.composition.entity.Template;
import org.springblade.composition.entity.TemplateComposition;
import org.springblade.core.mp.base.BaseEntity;

/**
 * 实体类
 *
 * @author Chill
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "TemplateComposition对象", description = "TemplateComposition对象")
public class TemplateCompositionDTO extends TemplateComposition {

	private static final long serialVersionUID = 1L;

	/**
	 * 组合名
	 */
	@ApiModelProperty(value = "组合名")
	private String compositionName;

	/**
	 * 组合类型：1.主页标注；2.基本信息标注
	 */
	@ApiModelProperty(value = "组合类型")
	private int compositionType;
}
