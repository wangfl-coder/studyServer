package org.springblade.task.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatusEnum {

	DEFAULT(1),
	IMPORTED(2),
	EXPORTED(3);
//	EXPORTED(4);

	final int num;
}
