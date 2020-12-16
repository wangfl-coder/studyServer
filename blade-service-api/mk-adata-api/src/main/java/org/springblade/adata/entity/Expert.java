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
@TableName("mk_adata_expert")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "Expert对象", description = "Expert对象")
public class Expert extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 任务id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "任务id")
	private Long taskId;

	/**
	 * 姓名
	 */
	@ApiModelProperty(value = "姓名")
	private String name;

	/**
	 * 中文姓名
	 */
	@ApiModelProperty(value = "中文姓名")
	private String nameZh;

	/**
	 * 职称
	 */
	@ApiModelProperty(value = "职称")
	private String titles;

	/**
	 * 职称描述
	 */
	@ApiModelProperty(value = "职称描述")
	private String titlesDesc;

	/**
	 * 联系电话
	 */
	@ApiModelProperty(value = "联系电话")
	private String phone;

	/**
	 * 传真
	 */
	@ApiModelProperty(value = "传真")
	private String fax;

	/**
	 * 电子邮件
	 */
	@ApiModelProperty(value = "电子邮件")
	private String email;

	/**
	 * 英文单位
	 */
	@ApiModelProperty(value = "英文单位")
	private String affiliation;

	/**
	 * 中文单位
	 */
	@ApiModelProperty(value = "中文单位")
	private String affiliationZh;

	/**
	 * 地址
	 */
	@ApiModelProperty(value = "地址")
	private String address;

	/**
	 * 个人主页
	 */
	@ApiModelProperty(value = "个人主页")
	private String homepage;

	/**
	 * 官方主页
	 */
	@ApiModelProperty(value = "官方主页")
	private String hp;

	/**
	 * Google
	 */
	@ApiModelProperty(value = "Google")
	private String gs;

	/**
	 * dblp
	 */
	@ApiModelProperty(value = "dblp")
	private String dblp;

	/**
	 * 性别
	 */
	@ApiModelProperty(value = "性别")
	private String gender;

	/**
	 * 语言
	 */
	@ApiModelProperty(value = "语言")
	private String language;

	/**
	 * 头像
	 */
	@ApiModelProperty(value = "头像")
	private String avatar;

	/**
	 * 教育背景
	 */
	@ApiModelProperty(value = "教育背景")
	private String edu;

	/**
	 * 工作经历
	 */
	@ApiModelProperty(value = "工作经历")
	private String work;

	/**
	 * 英文简介
	 */
	@ApiModelProperty(value = "英文简介")
	private String bio;

	/**
	 * 中文简介
	 */
	@ApiModelProperty(value = "中文简介")
	private String bioZh;

	/**
	 * 备注
	 */
	@ApiModelProperty(value = "备注")
	private String remark;


}
