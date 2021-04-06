package org.springblade.taskLog.feign;


import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.tool.api.R;
import org.springblade.taskLog.entity.TaskLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
	value =LauncherConstant.MKAPP_LOG_NAME
)

public interface ITaskLogClient {
	String API_PREFIX = "/task-log";
	@PostMapping(API_PREFIX+"save")
	boolean save(@RequestBody TaskLog taskLog);
}
