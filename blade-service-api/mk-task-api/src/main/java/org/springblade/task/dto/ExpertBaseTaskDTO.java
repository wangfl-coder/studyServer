package org.springblade.task.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "新建智库标注任务", description = "新建智库标注任务对象")
public class ExpertBaseTaskDTO extends BaseEntity {

	@ApiModelProperty(value = "智库id")
	@JsonSerialize(using = ToStringSerializer.class)
	private String ebId;
    // realSet
	@ApiModelProperty(value = "真题智库id")
	@JsonSerialize(using = ToStringSerializer.class)
	private String realSetEbId;

	@ApiModelProperty(value = "掺入真题数量")
	private Integer realSetCount;

	@ApiModelProperty(value = "任务名字")
	private String taskName;
	@ApiModelProperty(value = "任务类型")
	private Integer taskType;
	@ApiModelProperty(value = "任务编码")
	private String code;
	@ApiModelProperty(value = "任务描述")
	private String description;
	@ApiModelProperty(value = "模版id")
	private Long templateId;
	@ApiModelProperty(value = "流程定义id")
	private String processDefinitionId;
	@ApiModelProperty(value = "开始时间")
	private Date startTime;
	@ApiModelProperty(value = "完成时间")
	private Date endTime;
	@ApiModelProperty(value = "优先级")
	private Integer priority;
	@ApiModelProperty(value = "抽查数量")
	private Integer count;


}
