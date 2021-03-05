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

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.mp.base.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 实体类
 *
 * @author Chill
 */
@Data
@TableName("mk_adata_expert_extend")
@ApiModel(value = "Expert扩展对象", description = "Expert扩展对象（比aminer多了微软学术和第三方个人主页）")
public class ExpertExtend implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 主键id Aminer库中的原始学者ID
	 */
	@ApiModelProperty(value = "主键id Aminer库中的原始学者ID")
	private String id;

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
	 * 其它来源主页
	 */
	@ApiModelProperty(value = "其它来源主页")
	private String otherHomepage;

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
	 * MAG 微软学术
	 */
	@ApiModelProperty(value = "MAG 微软学术")
	private String mag;

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
	 * 备注
	 */
	@ApiModelProperty(value = "备注")
	private String remark;

	@ApiModelProperty("创建人")
	private String createUser;

	@ApiModelProperty("创建部门")
	private String createDept;
	@DateTimeFormat(
		pattern = "yyyy-MM-dd HH:mm:ss"
	)
	@JsonFormat(
		pattern = "yyyy-MM-dd HH:mm:ss"
	)
	@ApiModelProperty("创建时间")
	private Date createTime;

	@ApiModelProperty("更新人")
	private String updateUser;
	@DateTimeFormat(
		pattern = "yyyy-MM-dd HH:mm:ss"
	)
	@JsonFormat(
		pattern = "yyyy-MM-dd HH:mm:ss"
	)
	@ApiModelProperty("更新时间")
	private Date updateTime;
	@ApiModelProperty("业务状态")
	private Integer status;
	@TableLogic
	@ApiModelProperty("是否已删除")
	private Integer isDeleted;

}
