package org.springblade.desk.feign;

import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.launch.constant.AppConstant;
import org.springblade.core.tool.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = AppConstant.APPLICATION_DESK_NAME)
public interface ISubTaskClient {

	@GetMapping("feign/start-process")
	R startProcess(Long templateId, Long CompositionId);
}
