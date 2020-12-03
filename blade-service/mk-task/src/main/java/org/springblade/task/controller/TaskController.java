package org.springblade.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
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
	@ApiImplicitParams({
		@ApiImplicitParam(name = "paramName", value = "参数名称", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "paramKey", value = "参数键名", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "paramValue", value = "参数键值", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "name", value = "查询条件", paramType = "query", dataType = "string")
	})
	@ApiOperation(value = "分页查询全部任务")
	public R<IPage<Task>> list(@RequestParam(value = "taskName",required = false) String taskName, Query query) {
		QueryWrapper<Task> compositionQueryWrapper;
		if(taskName != null) {
			compositionQueryWrapper = new QueryWrapper<>();
			compositionQueryWrapper.like("task_name", "%"+taskName+"%");
		} else{
			compositionQueryWrapper = null;
		}
		IPage<Task> pages = taskService.page(Condition.getPage(query), compositionQueryWrapper);
		return R.data(pages);
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
