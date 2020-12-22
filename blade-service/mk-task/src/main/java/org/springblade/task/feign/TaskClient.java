package org.springblade.task.feign;

import lombok.AllArgsConstructor;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.task.entity.Task;
import org.springblade.task.service.TaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;


@NonDS
@ApiIgnore()
@RestController
@AllArgsConstructor
public class TaskClient implements ITaskClient{

	private final TaskService taskService;

	@Override
	@PostMapping(SAVE_TASK)
	public R saveTask(@RequestBody Task task) {
		boolean save = taskService.save(task);
		return R.status(save);
	}
}
