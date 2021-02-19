package org.springblade.task.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "返回当前用户所有组合及分别可接的任务列表", description = "返回当前用户所有组合及分别可接的任务列表")
public class CompositionClaimListVO {
	@ApiModelProperty(value = "组合id")
	private String compositionId;

	@ApiModelProperty(value = "组合名")
	private String name;

	@ApiModelProperty(value = "流程实例id")
	private String processInstanceId;

}
