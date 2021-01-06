package org.springblade.task.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.task.entity.Task;

/**
 * 通知公告视图类
 *
 * @author Chill
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskVO extends Task {

	@ApiModelProperty(value = "已完成数")
	private Integer completed;

	@ApiModelProperty(value = "正确数")
	private Integer correct;

	@ApiModelProperty(value = "当前时刻组合总数")
	private Integer compositionTotal;

	@ApiModelProperty(value = "完成组合数量")
	private Integer compositionCompleteCount;
}
