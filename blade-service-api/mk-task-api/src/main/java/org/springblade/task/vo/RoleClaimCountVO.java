package org.springblade.task.vo;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "返回当前用户所有角色及分别可接的任务数", description = "返回当前用户所有角色及分别可接的任务数")
public class RoleClaimCountVO {

	@ApiModelProperty(value = "角色别名")
	private String roleAlias;

	@ApiModelProperty(value = "可接任务数")
	private Integer count;

}
