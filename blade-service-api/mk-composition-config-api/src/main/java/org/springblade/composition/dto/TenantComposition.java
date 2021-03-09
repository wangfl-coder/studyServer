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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

/**
 * 对象实体类
 *
 * @author Chill
 */
@Data
@ApiModel(value = "TenantComposition对象", description = "TenantComposition对象")
public class TenantComposition implements Serializable {
	private static final long serialVersionUID = 1L;

	private String tenantId;

	private String tenantName;

	@JsonSerialize(using = ToStringSerializer.class)
	private Long compositionId;

	private String compositionName;

	private Integer number;

	private Integer averageSpeed;
}
