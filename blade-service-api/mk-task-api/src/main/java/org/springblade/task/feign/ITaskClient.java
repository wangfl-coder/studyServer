package org.springblade.task.feign;

import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.tool.api.R;
import org.springblade.task.entity.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = LauncherConstant.MKAPP_TASK_NAME)
public interface ITaskClient {
	String API_PREFIX = "/client";
	String GET_TASK_BY_ID = API_PREFIX + "/get-task-by-id";
	String SAVE_TASK = API_PREFIX + "/save-task";
	String GET_TASK_BY_TEMPLATE = API_PREFIX + "/get-task-by-template";
	String GET_SUBTASK_COUNT = API_PREFIX + "/get-subtask-count";


	@PostMapping(SAVE_TASK)
	R saveTask(@RequestBody Task task);

	@GetMapping(GET_TASK_BY_ID)
	R<Task> getById(@RequestParam Long id);

	@GetMapping(GET_TASK_BY_TEMPLATE)
	R<Task> getByTemplate(@RequestParam Long templateId);

//	@GetMapping(GET_SUBTASK_COUNT)
//	R<Integer> getSubTaskCount(@RequestParam("id") Long id);
}
