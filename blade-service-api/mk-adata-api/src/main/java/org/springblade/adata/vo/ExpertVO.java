package org.springblade.adata.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.adata.entity.Expert;

/**
 * 通知公告视图类
 *
 * @author Chill
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExpertVO extends Expert {

//	@ApiModelProperty(value = "通知类型名")
//	private String categoryName;
//
//	@ApiModelProperty(value = "租户编号")
//	private String tenantId;

}
