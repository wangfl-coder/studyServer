package org.springblade.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springblade.core.mp.base.BaseEntity;
import org.springblade.core.tenant.mp.TenantEntity;


import java.util.Date;

@Data
@TableName(value = "mk_task_quality_inspection")
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "质检Task对象", description = "质检Task对象")
public class QualityInspectionTask extends TenantEntity {

	@ApiModelProperty(value = "流程定义id")
	private String processDefinitionId;
	@ApiModelProperty(value = "流程实例id")
	private String processInstanceId;
	@ApiModelProperty(value = "质检大任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long inspectionTaskId;
	@ApiModelProperty(value = "标注大任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long taskId;
	@ApiModelProperty(value = "专家id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long personId;
	@ApiModelProperty(value = "标注子任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long labelTaskId;
	@ApiModelProperty(value = "标注流程实例id")
	private String labelProcessInstanceId;
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
	@ApiModelProperty(value = "备注")
	private String remark;
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
	@ApiModelProperty(value = "时间")
	private Integer time;
	@ApiModelProperty(value = "质检截图")
	private String picture;




}

