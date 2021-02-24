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

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.mp.base.BaseEntity;
import org.springblade.core.tenant.mp.TenantEntity;

import java.io.Serializable;

/**
 * 实体类
 *
 * @author Chill
 */
@Data
@TableName("mk_template")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "Template对象", description = "Template对象")
public class Template extends TenantEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 模板名
	 */
	@ApiModelProperty(value = "模板名")
	private String templateName;

	/**
	 * 备注
	 */
	@ApiModelProperty(value = "备注")
	private String remark;

	/**
	 * 做补充信息的角色名
	 */
	@ApiModelProperty(value = "做补充信息的角色名")
	private String moreMessageRoleName;

	/**
	 * 流程模型ID
	 */
	@ApiModelProperty(value = "流程模型ID")
	private String processDefinitionId;

	/**
	 * 真集流程模型ID
	 */
	@TableField(value="realset_process_definitions")
	@ApiModelProperty(value = "真集流程模型JSON {组合Id: processDefinitionId}")
	private String realSetProcessDefinitions;
}
