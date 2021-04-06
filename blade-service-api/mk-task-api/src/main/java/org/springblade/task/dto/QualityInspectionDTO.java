package org.springblade.task.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "新建智库质检任务", description = "新建智库质检任务对象")
public class QualityInspectionDTO {

	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "智库id")
	private String ebId;
	@ApiModelProperty(value = "质检任务名字")
	private String taskName;
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "模版id")
	private Long templateId;
	@ApiModelProperty(value = "质检任务编码")
	private String code;
	@ApiModelProperty(value = "质检任务描述")
	private String description;
	@ApiModelProperty(value = "开始时间")
	private Date startTime;
	@ApiModelProperty(value = "完成时间")
	private Date endTime;
	@ApiModelProperty(value = "优先级")
	private Integer priority;
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "标注大任务id")
	private Long annotationTaskId;
	@ApiModelProperty(value = "抽查数量")
	private Integer count;
	@ApiModelProperty(value = "质检类型")
	private Integer inspectionType;
	@ApiModelProperty(value = "时间")
	private Integer time;
	@ApiModelProperty(value = "质检截图")
	private String picture;

}
