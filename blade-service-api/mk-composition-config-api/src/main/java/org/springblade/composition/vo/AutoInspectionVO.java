package org.springblade.composition.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;


@Data
@ApiModel(value = "AutoInspectionVO对象", description = "AutoInspectionVO对象")
public class AutoInspectionVO {

	/**
	 * 正确或错误
	 */
	Integer isCompositionTrue;

	/**
	 * 正确数量或错误数量
	 */
	Integer count;

	/**
	 * 平均时间
	 */
	Integer avgTime;
}
