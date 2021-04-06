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
package org.springblade.taskLog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.core.mp.base.BaseEntity;

/**
 * 实体类
 *
 * @author BladeX
 * @since 2021-03-23
 */
@Data
@TableName("mk_log_task")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "TaskLog对象", description = "TaskLog对象")
public class TaskLog extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	* 操作方式
	*/
		@ApiModelProperty(value = "操作方式")
		private Integer action;
	/**
	* 任务ID
	*/
		@ApiModelProperty(value = "任务ID")
		private Integer taskId;
	/**
	* 操作描述
	*/
		@ApiModelProperty(value = "操作描述")
		private String actionLog;


}
