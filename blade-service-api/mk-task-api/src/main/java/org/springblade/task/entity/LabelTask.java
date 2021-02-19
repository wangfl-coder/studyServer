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


import java.util.Date;

@Data
@TableName(value = "mk_task_label")
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "标注Task对象", description = "标注Task对象")
public class LabelTask extends BaseEntity {

	@ApiModelProperty(value = "流程定义id")
	private String processDefinitionId;
	@ApiModelProperty(value = "流程实例id")
	private String processInstanceId;
	@ApiModelProperty(value = "任务id")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long taskId;
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "专家id")
	private Long personId;
	@ApiModelProperty(value = "专家名字")
	private String personName;
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "组合id")
	private Long compositionId;
	@JsonSerialize(using = ToStringSerializer.class)
	@ApiModelProperty(value = "模版id")
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
	@ApiModelProperty(value = "类型 1.标注 2.真题")
	private Integer type;
}

