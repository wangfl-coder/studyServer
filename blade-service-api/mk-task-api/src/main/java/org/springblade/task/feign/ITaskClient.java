package org.springblade.task.feign;

import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.tool.api.R;
import org.springblade.task.entity.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = LauncherConstant.MKAPP_TASK_NAME)
public interface ITaskClient {
	String API_PREFIX = "/client";
	String SAVE_TASK = API_PREFIX+"/save-task";

	@PostMapping(SAVE_TASK)
	R saveTask(@RequestBody Task task);

}
