package org.springblade.task.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springblade.core.mp.base.BaseEntity;

import java.util.Date;

@Data
@TableName(value = "mk_task")
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "Task对象", description = "Task对象")
public class Task extends BaseEntity {

	@ApiModelProperty(value = "智库id")
	private Long ebId;
	@ApiModelProperty(value = "任务名字")
	private String taskName;
	@ApiModelProperty(value = "任务类型")
	private Integer taskType;
	@ApiModelProperty(value = "模版id")
	private Long templateId;
	@ApiModelProperty(value = "任务编码")
	private String code;
	@ApiModelProperty(value = "任务描述")
	private String description;
	@ApiModelProperty(value = "开始时间")
	private Date startTime;
	@ApiModelProperty(value = "完成时间")
	private Date finish_time;
	@ApiModelProperty(value = "截止时间")
	private Date deadline;
	@ApiModelProperty(value = "优先级")
	private Integer priority;

}
