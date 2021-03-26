package org.springblade.task.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LabelTaskTypeEnum {

	LABEL(1),
	REAL_SET(2);

	final int num;
}
