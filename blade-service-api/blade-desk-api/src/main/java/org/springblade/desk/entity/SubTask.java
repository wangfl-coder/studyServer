package org.springblade.desk.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springblade.flow.core.entity.FlowEntity;

import java.util.Date;

@Data
@TableName(value = "mk_subtask")
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Task对象", description = "Task对象")
public class SubTask extends FlowEntity {

	@ApiModelProperty(value = "流程定义id")
	private String processDefinitionId;
	@ApiModelProperty(value = "流程实例id")
	private String processInstanceId;
	@ApiModelProperty(value = "任务id")
	private Long taskId;
	@ApiModelProperty(value = "专家id")
	private Long personId;
	@ApiModelProperty(value = "组合id")
	private Long compositionId;
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

}

