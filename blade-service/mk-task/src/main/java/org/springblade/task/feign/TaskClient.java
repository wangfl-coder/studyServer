package org.springblade.task.feign;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.task.entity.MergeExpertTask;
import org.springblade.task.entity.Task;
import org.springblade.task.service.TaskService;
import org.springblade.taskLog.entity.TaskLog;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;


@NonDS
@ApiIgnore()
@RestController
@AllArgsConstructor
public class TaskClient implements ITaskClient{

	private final TaskService taskService;

	@Override
	@PostMapping(UPDATE_TASK)
	public R updateTask(@RequestBody Task task) {
		boolean save = taskService.updateById(task);
		return R.status(save);
	}

	@Override
	@GetMapping(GET_TASK_BY_ID)
	public R<Task> getById(@RequestParam("id") Long id) {
		Task task = taskService.getById(id);
		return R.data(task);
	}

	@Override
	@GetMapping(GET_TASK_BY_TEMPLATE)
	public R<Task> getByTemplate(@RequestParam("templateId") Long templateId) {
		Task taskQuery = new Task();
		taskQuery.setTemplateId(templateId);
		Task task = taskService.getOne(Condition.getQueryWrapper(taskQuery).last("LIMIT 1"));
		return R.data(task);
	}

	@Override
	@GetMapping(CHANGE_STATUS)
	public R changeStatus(@RequestParam Long id, @RequestParam Integer status) {
		UpdateWrapper<Task> taskUpdateWrapper = new UpdateWrapper<>();
		taskUpdateWrapper.eq("id",id).set("status",status);
		boolean update = taskService.update(taskUpdateWrapper);
		return R.status(update);
	}


//	@Override
//	public R save(Task task) {
//		return R.data(ITaskClient.byid(task));
//	}

//	@Override
//	@GetMapping(GET_SUBTASK_COUNT)
//	public R<Integer> getSubTaskCount(@RequestParam("id") Long id) {
//		int count = taskService.getSubTaskCount(id);
//		return R.data(count);
//	}
}
