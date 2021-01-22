package org.springblade.flow.core.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


@Data
public class SingleFlow extends BladeFlow{

//	@ApiModelProperty(value = "专家id")
//	@JsonSerialize(using = ToStringSerializer.class)
//	private Long personId;
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
	@ApiModelProperty(value = "标注流程实例id")
	private String labelProcessInstanceId;
	@ApiModelProperty(value = "组合id")
	private String compositionId;
	@ApiModelProperty(value = "组合类型")
	private Integer compositionType;
	@ApiModelProperty(value = "组合字段")
	private String compositionField;
	@ApiModelProperty(value = "hp")
	private String hp;
	@ApiModelProperty(value = "gs")
	private String gs;
	@ApiModelProperty(value = "dblp")
	private String dblp;
	@ApiModelProperty(value = "otherHomepage")
	private String otherHomepage;
	@ApiModelProperty(value = "homepage")
	private String homepage;
	@ApiModelProperty(value = "是否是真题标志(-1:不是，1:是)")
	private int isRealSet;

}
