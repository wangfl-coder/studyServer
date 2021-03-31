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
package org.springblade.mq.rabbit.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.mp.base.BaseEntity;
import org.springblade.core.tenant.mp.TenantEntity;

/**
 * 实体类
 *
 * @author mk
 */
@Data
@TableName("mk_preprocess_pure")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "PurePerson对象", description = "PurePerson对象")
public class PurePerson extends TenantEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * error_names
	 */
	@ApiModelProperty(value = "error_names")
	private String errorNames;
	/**
	 * need_repair_papers
	 */
	@ApiModelProperty(value = "need_repair_papers")
	private String needRepairPapers;
	/**
	 * papers_to_person
	 */
	@ApiModelProperty(value = "papers_to_person")
	private String papersToPerson;
	/**
	 * remove_direct
	 */
	@ApiModelProperty(value = "remove_direct")
	private String removeDirect;
	/**
	 * remove_from_person
	 */
	@ApiModelProperty(value = "remove_from_person")
	private String removeFromPerson;
	/**
	 * 备注
	 */
	@ApiModelProperty(value = "备注")
	private String remark;

	/**
	 * 任务id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "任务id")
	private Long taskId;

	/**
	 * 合并子任务Id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "合并子任务Id")
	private Long mergeTaskId;

	/**
	 * Aminer库中的原始学者ID
	 */
	@ApiModelProperty(value = "Aminer库中的原始学者ID")
	private String expertId;

	/**
	 * 专家ID
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "专家id")
	private Long personId;
}
