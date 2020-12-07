package org.springblade.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;

@Data
@ApiModel(value = "子任务实体类", description = "子任务实体类")
public class SubTask extends BaseEntity {

	@ApiModelProperty(value = "任务id")
	private Long taskId;
	@ApiModelProperty(value = "目标id")
	private Long targetId;
	@ApiModelProperty(value = "组合id")
	private Long compositionID;
	@ApiModelProperty(value = "目标类型")
	private Integer targetType;
	@ApiModelProperty(value = "分配给谁")
	private String assignTo;
	@ApiModelProperty(value = "前驱任务")
	private Long prerequisiteTask;
	@ApiModelProperty(value = "开始时间")
	private Date startTime;
	@ApiModelProperty(value = "完成时间")
	private Date finishTime;
	@ApiModelProperty(value = "优先级")
	private Integer priority;

}
