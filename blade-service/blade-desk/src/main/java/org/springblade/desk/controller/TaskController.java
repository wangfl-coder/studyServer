package org.springblade.desk.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.common.cache.CacheNames;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.desk.entity.Task;
import org.springblade.desk.service.TaskService;
import org.springblade.system.user.cache.UserCache;
import org.springframework.web.bind.annotation.*;

@NonDS
@RestController
@RequestMapping(value = "process/task")
@AllArgsConstructor
@Api(value = "任务")
public class TaskController extends BladeController implements CacheNames {

	private TaskService taskService;

	@GetMapping(value = "/detail")
	@ApiOperation(value = "详情")
	public R<Task> detail(@RequestParam("businessId") Long businessId) {
		Task detail = taskService.getById(businessId);
		//detail.getFlow().setAssigneeName(UserCache.getUser(detail.getCreateUser()).getName());
		return R.data(detail);
	}

	@PostMapping("start-process")
	@ApiOperation(value = "新增或修改")
	public R startProcess(@RequestBody Task task) {
		return R.status(taskService.startProcess(task));
	}

}
