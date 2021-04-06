package org.springblade.task.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskTypeEnum {

	LABEL(1),
	INSPECTION(2),
	MERGE_EXPERT(3);

	final int num;
}
