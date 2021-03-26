package org.springblade.system.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatusEnum {

	DEFAULT(1),
	BLOCKED(2);

	final int num;
}
