package org.springblade.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.tool.api.R;
import org.springblade.task.entity.Task;
import org.springblade.task.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "task")
@Api(value = "任务接口")
public class TaskController {

	private TaskService taskService;

	@PostMapping(value = "/save")
	@ApiOperation(value = "添加任务")
	public R save(@RequestBody Task task){
		boolean save = taskService.save(task);
		return R.status(save);
	}

	@RequestMapping(value = "/detail/{id}" , method = RequestMethod.GET)
	@ApiOperation(value = "根据id查询任务")
	public R<Task> detail(@PathVariable Long id){
		return R.data(taskService.getById(id));
	}

	@GetMapping("/list")
	@ApiOperation(value = "查询全部任务")
	public R<List<Task>> list(@RequestParam(value = "taskName",required = false) String taskName){
		List<Task> list;
		if(taskName!=null){
			QueryWrapper<Task> compositionQueryWrapper = new QueryWrapper<>();
			compositionQueryWrapper.eq("task_name",taskName);
			list = taskService.list(compositionQueryWrapper);
		}else{
			list = taskService.list();
		}
		return R.data(list);
	}

	@PostMapping(value = "/update")
	@ApiOperation(value = "更新任务")
	public R update(@RequestBody Task task){
		return R.status(taskService.updateById(task));
	}

	@PostMapping(value = "/remove")
	@ApiOperation(value = "删除任务")
	public R delete(@RequestParam Long id){
		return R.status(taskService.removeById(id));
	}


}
