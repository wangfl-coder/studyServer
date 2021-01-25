package org.springblade.composition.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;


@Data
@ApiModel(value = "AutoInspectionVO对象", description = "AutoInspectionVO对象")
public class AutoInspectionVO {

	Integer realSetTotal;
	Integer correctOrErrorCount;
	Integer avgTime;
}
