package org.springblade.composition.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springblade.core.mp.base.BaseEntity;
import org.springblade.core.tenant.mp.TenantEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "mk_composition")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "Composition对象", description = "Composition对象")
public class Composition extends TenantEntity {

	@ApiModelProperty("组合名字")
	private String name;
	@ApiModelProperty("组合字段")
	private String field;
//	@ApiModelProperty("组合状态")
//	private Integer status;
	@ApiModelProperty("组合描述")
	private String description;
	@ApiModelProperty("标注类型")
	private Integer annotationType;
//	@ApiModelProperty("积分")
//	private Integer points;

	@TableField(exist = false)
	@ApiModelProperty("提交（完成）的数量")
	private Integer submitCount;
//	@TableLogic
//	private Integer isDel;
//	private Date createTime;
//	private String createBy;

}
