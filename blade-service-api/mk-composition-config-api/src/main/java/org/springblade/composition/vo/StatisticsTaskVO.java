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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.composition.entity.Statistics;
import org.springblade.core.tenant.mp.TenantEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 实体类
 *
 * @author Chill
 */
@Data
@ApiModel(value = "StatisticsTaskVO", description = "StatisticsTaskVO对象")
public class StatisticsTaskVO extends Statistics {

	private static final long serialVersionUID = 1L;

	/**
	 * 任务id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "任务id")
	private Long taskId;

	/**
	 * 组合名
	 */
	@ApiModelProperty(value = "组合名")
	private String compositionName;

	/**
	 * Aminer专家id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "Aminer专家id")
	private String expertId;

	/**
	 * 专家id
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "专家id")
	private Long personId;

	/**
	 * 专家名
	 */
	@ApiModelProperty(value = "专家名")
	private String personName;

	@DateTimeFormat(
		pattern = "yyyy-MM-dd HH:mm:ss"
	)
	@JsonFormat(
		pattern = "yyyy-MM-dd HH:mm:ss"
	)
	@ApiModelProperty("标注时间")
	private Date labelTime;

}
