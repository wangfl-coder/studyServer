package org.springblade.desk.controller;

import lombok.AllArgsConstructor;
import org.springblade.common.cache.CacheNames;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.desk.entity.Task;
import org.springblade.desk.service.TaskService;
import org.springblade.system.user.cache.UserCache;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

@NonDS
@ApiIgnore
@RestController
@RequestMapping(value = "process/task")
@AllArgsConstructor
public class TaskController extends BladeController implements CacheNames {

	private TaskService taskService;

	@GetMapping(value = "/detail")
	public R<Task> detail(@RequestParam("id") Long businessId) {
		Task detail = taskService.getById(businessId);
		detail.getFlow().setAssigneeName(UserCache.getUser(detail.getCreateUser()).getName());
		return R.data(detail);
	}

	@PostMapping("start-process")
	public R startProcess(@RequestBody Task task) {
		return R.status(taskService.startProcess(task));
	}

}
