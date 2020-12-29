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

	@ApiModelProperty(value = "通知类型名")
	private Integer completed;

	@ApiModelProperty(value = "总数")
	private Integer total;
}
