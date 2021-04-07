package org.springblade.task.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MergeExpertTaskStatusEnum {

	DEFAULT(1),
	PURE_SUPED(2),
	STARTED(3);
//	EXPORTED(4);

	final int num;
}
