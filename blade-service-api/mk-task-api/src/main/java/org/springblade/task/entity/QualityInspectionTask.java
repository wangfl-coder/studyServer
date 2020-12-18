package org.springblade.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springblade.flow.core.entity.FlowEntity;

import java.util.Date;

@Data
@TableName(value = "mk_task_quality_inspection")
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "质检Task对象", description = "质检Task对象")
public class QualityInspectionTask extends FlowEntity {

	@ApiModelProperty(value = "流程定义id")
	private String processDefinitionId;
	@ApiModelProperty(value = "流程实例id")
	private String processInstanceId;
	@ApiModelProperty(value = "任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long taskId;
	@ApiModelProperty(value = "专家id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long personId;
	@ApiModelProperty(value = "专家名字")
	private String personName;
	@ApiModelProperty(value = "组合id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long compositionId;
	@ApiModelProperty(value = "模版id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long templateId;
	@ApiModelProperty(value = "任务领取人")
	private String taskUser;
	@ApiModelProperty(value = "开始时间")
	private Date startTime;
	@ApiModelProperty(value = "完成时间")
	private Date endTime;
	@ApiModelProperty(value = "请假理由")
	private String reason;
	@ApiModelProperty(value = "流程申请时间")
	private String applyTime;
	@ApiModelProperty(value = "持续时间")
	private double duration;
	@ApiModelProperty(value = "优先级")
	private Integer priority;
	@ApiModelProperty(value = "质检任务类型")
	private Integer inspectionType;
	@ApiModelProperty(value = "任务类型")
	private Integer taskType;



}

