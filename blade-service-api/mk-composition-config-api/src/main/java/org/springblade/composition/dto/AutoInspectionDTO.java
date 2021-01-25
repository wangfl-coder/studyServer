package org.springblade.composition.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;


@Data
@ApiModel(value = "AutoInspectionDTO对象", description = "AutoInspectionDTO对象")
public class AutoInspectionDTO {

	Integer realSetTotal;
	Integer correctOrErrorCount;
	float avgTime;
}
