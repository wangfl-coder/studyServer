package org.springblade.flow.core.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;



@Data
public class SingleFlow extends BladeFlow{

	@ApiModelProperty(value = "专家id")
	private Long personId;
	@ApiModelProperty(value = "模版id")
	private Long templateId;

}
