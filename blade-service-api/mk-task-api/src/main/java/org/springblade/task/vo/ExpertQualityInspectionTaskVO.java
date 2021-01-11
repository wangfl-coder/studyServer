package org.springblade.task.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "根据专家id返回子任务实例id", description = "根据专家id返回子任务实例id")
public class ExpertQualityInspectionTaskVO {
	@ApiModelProperty(value = "子任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	@ApiModelProperty(value = "自动生成的专家id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long personId;

	@ApiModelProperty(value = "真正的的专家id")
	private String expertId;

	@ApiModelProperty(value = "子任务流程实例Id")
	private String processInstanceId;

	@ApiModelProperty(value = "模版id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long templateId;

	@ApiModelProperty(value = "专家名字")
	private String personName;

	@ApiModelProperty(value = "质检大任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long inspectionTaskId;

	@ApiModelProperty(value = "标注子任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long labelTaskId;

	@ApiModelProperty(value = "标注大任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long taskId;

}
