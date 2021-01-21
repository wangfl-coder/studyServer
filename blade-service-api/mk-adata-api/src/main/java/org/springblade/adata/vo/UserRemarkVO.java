package org.springblade.adata.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "UserRemarkVO对象", description = "UserRemarkVO对象")
public class UserRemarkVO {

	@JsonSerialize(using = ToStringSerializer.class)
	Long personId;
	String processInstanceId;
}
