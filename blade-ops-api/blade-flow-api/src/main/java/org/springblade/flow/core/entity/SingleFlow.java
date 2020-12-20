package org.springblade.flow.core.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;



@Data
public class SingleFlow extends BladeFlow{

	@ApiModelProperty(value = "专家id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long personId;
	@ApiModelProperty(value = "专家名字")
	private String personName;
	@ApiModelProperty(value = "模版id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long templateId;
	@ApiModelProperty(value = "子任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long subTaskId;
	@ApiModelProperty(value = "标注任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long annotationTaskId;
	@ApiModelProperty(value = "质检任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long inspectionTaskId;
	@ApiModelProperty(value = "标注子任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long labelTaskId;

}
