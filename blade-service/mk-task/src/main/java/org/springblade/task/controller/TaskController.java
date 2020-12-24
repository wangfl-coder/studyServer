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
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.task.dto.ExpertBaseTaskDTO;
import org.springblade.task.dto.QualityInspectionDTO;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.entity.Task;
import org.springblade.task.feign.ILabelTaskClient;
import org.springblade.task.service.LabelTaskService;
import org.springblade.task.service.QualityInspectionTaskService;
import org.springblade.task.service.TaskService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@RestController
@AllArgsConstructor
@RequestMapping(value = "task")
@Api(value = "任务接口")
public class TaskController extends BladeController {

	private TaskService taskService;
	private IExpertClient expertClient;
	private LabelTaskService labelTaskService;
	private ILabelTaskClient iLabelTaskClient;
	private QualityInspectionTaskService qualityInspectionTaskService;

	@GetMapping(value = "/complete/count")
	@ApiOperation(value = "查询已经完成的任务的数量")
	public R queryCompleteTaskCount(@RequestParam("taskId") Long taskId, @RequestParam("categoryName") String categoryName) {
		if (categoryName.equals("标注流程")) {
			int res = labelTaskService.queryCompleteTaskCount(taskId);
			int k = 0;
		} else if (categoryName.equals("质检流程")) {
//			qualityInspectionTaskService.queryCompleteTaskCount(taskId);
		}
//		R r = iLabelTaskClient.queryCompleteTaskCount(taskId);
		return R.success("");
	}

	@GetMapping(value = "/complete/list")
	@ApiOperation(value = "查询已经完成的任务列表")
	public R queryCompleteTask(@RequestParam("taskId") Long taskId) {
		ArrayList<LabelTask> arrayList = iLabelTaskClient.queryCompleteTask2(taskId).getData();
		return R.data(arrayList);
	}


	@PostMapping(value = "/inspection/save")
	@ApiOperation(value = "添加质检任务")
	public R inspectionSave(@RequestBody QualityInspectionDTO qualityInspectionDTO) {
		Boolean result;
		Task task = Objects.requireNonNull(BeanUtil.copy(qualityInspectionDTO, Task.class));
		boolean save = taskService.save(task);
		R<ArrayList<LabelTask>> listR = iLabelTaskClient.queryCompleteTask2(task.getAnnotationTaskId());
		if (listR.isSuccess()){
			List<LabelTask> labelTasks = listR.getData();
			result = qualityInspectionTaskService.startProcess(qualityInspectionDTO.getProcessDefinitionId(),task.getCount(),task.getInspectionType(),task,labelTasks);
		}else {
			return R.fail("获取标注完成的任务失败");
		}
		return R.status(result);
	}

	@PostMapping(value = "/save")
	@ApiOperation(value = "添加标注任务")
	public R save(@RequestBody ExpertBaseTaskDTO expertBaseTaskDTO) {
		Boolean result;
		Task task = Objects.requireNonNull(BeanUtil.copy(expertBaseTaskDTO, Task.class));
		boolean save = taskService.save(task);
		R res_eb = expertClient.importExpertBase(task.getEbId(), task.getId());
		if (res_eb.isSuccess()) {
			R<List<Expert>> rexperts = expertClient.getExpertIds(task.getId());
			if (rexperts.isSuccess()) {
				List<Expert> experts = rexperts.getData();
				result = labelTaskService.startProcess(expertBaseTaskDTO.getProcessDefinitionId(), task, experts);
			} else {
				return R.fail("读取专家列表失败");
			}
		} else {
			taskService.removeById(task.getId());
			return R.fail("导入智库失败");
		}
		return R.status(result);
	}

	@RequestMapping(value = "/detail/{id}" , method = RequestMethod.GET)
	@ApiOperation(value = "根据id查询任务")
	public R<Task> detail(@PathVariable Long id){
		return R.data(taskService.getById(id));
	}

//	@GetMapping("/list")
//	@ApiImplicitParams({
//		@ApiImplicitParam(name = "taskName", value = "查询条件", paramType = "query", dataType = "string")
//	})
//	@ApiOperation(value = "分页查询全部任务")
//	public R<IPage<Task>> list(@RequestParam(value = "taskName",required = false) String taskName, Query query) {
//		QueryWrapper<Task> compositionQueryWrapper = new QueryWrapper<>();
//		if(taskName != null) {
//			compositionQueryWrapper.like("task_name", "%"+taskName+"%").orderByDesc("create_time");
//		} else{
//			compositionQueryWrapper.orderByDesc("create_time");
//		}
//		IPage<Task> pages = taskService.page(Condition.getPage(query), compositionQueryWrapper);
//		return R.data(pages);
//	}
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "taskName", value = "查询条件", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "taskType", value = "任务类型", paramType = "query", dataType = "integer")
	})
	@ApiOperation(value = "分页查询全部任务")
	public R<IPage<Task>> list(@ApiIgnore @RequestParam Map<String, Object> task, Query query) {
		IPage<Task> pages = taskService.page(Condition.getPage(query), Condition.getQueryWrapper(task, Task.class));
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
