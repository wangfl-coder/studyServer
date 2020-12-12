package org.springblade.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.desk.feign.ISubTaskClient;
import org.springblade.task.entity.Task;
import org.springblade.task.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@AllArgsConstructor
@RequestMapping(value = "task")
@Api(value = "任务接口")
public class TaskController extends BladeController {

	private TaskService taskService;
	private IExpertClient iExpertClient;
	private ISubTaskClient iSubTaskClient;

	@PostMapping(value = "/save")
	@ApiOperation(value = "添加任务")
	public R save(@RequestBody Task task){
		boolean save = taskService.save(task);
		R tmp = iExpertClient.importExpertBase(task.getEbId(), task.getId());
		Expert expert = new Expert();
		expert.setTaskId(task.getId());
		List<Long> ids = iExpertClient.detail_list(expert);
		iSubTaskClient.startProcess(task.getTemplateId(),ids);
		return R.status(save);
	}

	@RequestMapping(value = "/detail/{id}" , method = RequestMethod.GET)
	@ApiOperation(value = "根据id查询任务")
	public R<Task> detail(@PathVariable Long id){
		return R.data(taskService.getById(id));
	}

	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "taskName", value = "查询条件", paramType = "query", dataType = "string")
	})
	@ApiOperation(value = "分页查询全部任务")
	public R<IPage<Task>> list(@RequestParam(value = "taskName",required = false) String taskName, Query query) {
		QueryWrapper<Task> compositionQueryWrapper = new QueryWrapper<>();
		if(taskName != null) {
			compositionQueryWrapper.like("task_name", "%"+taskName+"%").orderByDesc("create_time");
		} else{
			compositionQueryWrapper.orderByDesc("create_time");
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
	public R delete(@RequestParam String ids){
		return R.status(taskService.deleteLogic(Func.toLongList(ids)));
	}


}
