package org.springblade.adata.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RealSetExpertStatusEnum {

	AVAILABLE(1),
	USED(2);

	final int num;
}
