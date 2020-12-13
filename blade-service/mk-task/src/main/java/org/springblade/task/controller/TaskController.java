package org.springblade.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.task.dto.ExpertBaseTaskDTO;
import org.springblade.task.entity.Task;
import org.springblade.task.service.LabelTaskService;
import org.springblade.task.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;


@RestController
@AllArgsConstructor
@RequestMapping(value = "task")
@Api(value = "任务接口")
public class TaskController extends BladeController {

	private TaskService taskService;
	private IExpertClient expertClient;
	private LabelTaskService labelTaskService;

	@PostMapping(value = "/save")
	@ApiOperation(value = "添加任务")
	public R save(@RequestBody ExpertBaseTaskDTO expertBaseTaskDTO){
		Boolean result;
		Task task = Objects.requireNonNull(BeanUtil.copy(expertBaseTaskDTO, Task.class));
		boolean save = taskService.save(task);
		R res_eb = expertClient.importExpertBase(task.getEbId(), task.getId());
		if (res_eb.isSuccess()) {
			R res_ids = expertClient.getExpertIds(task.getId());
			if (res_ids.isSuccess()) {
				List<Long> ids = (List<Long>)res_ids.getData();
				result = labelTaskService.startProcess(expertBaseTaskDTO.getProcessDefinitionId(), task, ids);
			} else {
				return R.fail("读取专家列表失败");
			}
		} else {
			return R.fail("导入智库失败");
		}

		return R.status(result);
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
