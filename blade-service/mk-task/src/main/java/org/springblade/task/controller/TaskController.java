package org.springblade.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.RealSetExpert;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.adata.feign.IRealSetExpertClient;
import org.springblade.composition.feign.IStatisticsClient;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.flow.core.feign.IFlowClient;
import org.springblade.system.cache.SysCache;
import org.springblade.task.cache.TaskCache;
import org.springblade.task.dto.ExpertBaseTaskDTO;
import org.springblade.task.dto.QualityInspectionDTO;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.entity.Task;
import org.springblade.task.feign.ILabelTaskClient;
import org.springblade.task.mapper.LabelTaskMapper;
import org.springblade.task.mapper.TaskMapper;
import org.springblade.task.service.LabelTaskService;
import org.springblade.task.service.QualityInspectionTaskService;
import org.springblade.task.service.TaskService;
import org.springblade.task.vo.TaskVO;
import org.springblade.task.wrapper.TaskWrapper;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


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
	private IStatisticsClient statisticsClient;
	private IFlowClient flowClient;
	private TaskMapper taskMapper;
	private LabelTaskMapper labelTaskMapper;
	private IRealSetExpertClient realSetExpertClient;


	@GetMapping(value = "/complete/count")
	@ApiOperation(value = "查询已经完成的任务的数量")
	public R queryCompleteTaskCount(@RequestParam("taskId") Long taskId) {
		Task task = taskService.getById(taskId);
		int count = 0;
		if (1 == task.getTaskType()) {
			count = labelTaskService.completeCount(task.getId());
		}else if (2 == task.getTaskType()){
			count = qualityInspectionTaskService.completeCount(task.getId());
		}
		return R.data(count);
	}

	@GetMapping(value = "/inspection/count")
	@ApiOperation(value = "查询可以质检的标注子任务数量")
	public R queryIsInspectionTaskCount(@RequestParam("taskId") Long taskId) {
		List<LabelTask> labelTasks = labelTaskService.queryCompleteTask1(taskId);
		return R.data(labelTasks.size());
	}

	@GetMapping(value = "/complete/list")
	@ApiOperation(value = "查询已经完成的标注任务列表")
	public R queryCompleteTask(@RequestParam("taskId") Long taskId) {
		List<LabelTask> labelTasks = labelTaskService.queryCompleteTask(taskId);
		return R.data(labelTasks);
	}

	@PostMapping(value = "/inspection/save")
	@ApiOperation(value = "添加质检任务")
	public R inspectionSave(@RequestBody QualityInspectionDTO qualityInspectionDTO) {
		Boolean result;
		List<LabelTask> labelTasks=new ArrayList<>();
		Task task = Objects.requireNonNull(BeanUtil.copy(qualityInspectionDTO, Task.class));
		//1不去重,2去重
		if(qualityInspectionDTO.getInspectionType()==1){
			labelTasks = labelTaskService.queryCompleteTask(task.getAnnotationTaskId());
		}else if(qualityInspectionDTO.getInspectionType()==2){
			labelTasks = labelTaskService.queryCompleteTask1(task.getAnnotationTaskId());
		}
		if (labelTasks.size() > 0){
			boolean save = taskService.save(task);
			try {
				result = qualityInspectionTaskService.startProcess(qualityInspectionDTO.getProcessDefinitionId(), task.getCount(), task.getInspectionType(), task, labelTasks);
				return R.status(result);
			}catch (Exception e){
				taskService.removeById(task.getId());
				return R.fail("创建质检小任务失败");
			}
		}else {
			return R.fail("获取标注完成的任务失败，或者没有标注完成的任务");
		}
	}

	@PostMapping(value = "/save")
	@ApiOperation(value = "添加标注任务")
	public R save(@RequestBody ExpertBaseTaskDTO expertBaseTaskDTO) {
		Boolean result;
		Task task = Objects.requireNonNull(BeanUtil.copy(expertBaseTaskDTO, Task.class));
		boolean save = taskService.save(task);
		R res_eb = expertClient.importExpertBase(task.getEbId(), task.getId());

		boolean flag = true;
		// 导入真题数据库中的所有专家
		if(expertBaseTaskDTO.getRealSetEbId()!=null) {
			R res_real_set_eb = realSetExpertClient.importExpertBase(expertBaseTaskDTO.getRealSetEbId(), task.getId());
			flag = res_real_set_eb.isSuccess();
		}

		if (res_eb.isSuccess() && flag) {
			R<List<Expert>> rexperts = expertClient.getExpertIds(task.getId());
			if (rexperts.isSuccess()) {
				List<Expert> experts = rexperts.getData();

				// 设置任务的真题比例，[0,100)
				task.setRealSetRate(expertBaseTaskDTO.getRealSetCount() * 100 / (expertBaseTaskDTO.getRealSetCount()+experts.size()));
				result = labelTaskService.startProcess(expertBaseTaskDTO.getProcessDefinitionId(), task, experts);
				task.setCount(experts.size());
				taskService.saveOrUpdate(task);
			} else {
				return R.fail("读取专家列表失败");
			}
		} else {
			taskService.removeById(task.getId());
			return R.fail("导入智库失败");
		}
		statisticsClient.initialize(task.getId());
		return R.status(result);
	}

	@RequestMapping(value = "/detail/{id}" , method = RequestMethod.GET)
	@ApiOperation(value = "根据id查询任务")
	public R<TaskVO> detail(@PathVariable Long id){
		Task task = taskService.getById(id);
		TaskVO taskVO = taskService.setCompletedCount(task);
		TaskVO res = taskService.setCorrectCount(taskVO);
		Integer compositionCount = taskService.compositionCount(id);
		res.setCompositionTotal(compositionCount);
		List<Integer> nums = taskMapper.compositionCompleteCount(id);
		int sum=0;
		for (Integer num : nums) {
			sum += num;
		}
		res.setCompositionCompleteCount(sum);
//		QueryWrapper<LabelTask> labelTaskQueryWrapper = new QueryWrapper<>();
//		labelTaskQueryWrapper.eq("task_id",id);
//		Integer count = labelTaskMapper.selectCount(labelTaskQueryWrapper);
//		res.setCount(count);
		return R.data(res);
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
	public R<IPage<TaskVO>> list(@ApiIgnore @RequestParam Map<String, Object> task, Query query) {
		IPage<Task> pages = taskService.page(Condition.getPage(query), Condition.getQueryWrapper(task, Task.class).orderByDesc("update_time"));
		ArrayList<Integer> compositionList = new ArrayList<>();
		ArrayList<Integer> compositionCountList = new ArrayList<>();
		for(Task task1:pages.getRecords()){
			int sum=0;
			Integer compositionCount = taskService.compositionCount(task1.getId());
			compositionCountList.add(compositionCount);
			List<Integer> nums = taskMapper.compositionCompleteCount(task1.getId());
			for (Integer num : nums) {
				sum += num;
			}
			compositionList.add(sum);
//			QueryWrapper<LabelTask> labelTaskQueryWrapper = new QueryWrapper<>();
//			labelTaskQueryWrapper.eq("task_id",task1.getId());
//			Integer count = labelTaskMapper.selectCount(labelTaskQueryWrapper);
//			task1.setCount(count);
//			taskService.updateById(task1);
		}

		List<TaskVO> records = taskService.batchSetCompletedCount(pages.getRecords());
		List<TaskVO> recordList = taskService.batchSetCorrectCount(records);
		for(int j=0;j<recordList.size();j++){
			recordList.get(j).setCompositionCompleteCount(compositionList.get(j));
			recordList.get(j).setCompositionTotal(compositionCountList.get(j));
		}
		IPage<TaskVO> pageVo = new Page(pages.getCurrent(), pages.getSize(), pages.getTotal());
		pageVo.setRecords(recordList);
		return R.data(pageVo);
	}

	@PostMapping(value = "/update")
	@ApiOperation(value = "更新任务")
	public R update(@RequestBody Task task){
		Task originTask = taskService.getById(task.getId());
		if (!task.getPriority().equals(originTask.getPriority())) {
			List<LabelTask> labelTasks = labelTaskService.getByTaskId(task.getId());
			List<String> processInstanceIds = new ArrayList<>();
			labelTasks.forEach(labelTask -> {
				processInstanceIds.add(labelTask.getProcessInstanceId());
			});
			flowClient.setTaskPriorityByProcessInstanceIds(processInstanceIds, task.getPriority());
		}

		return R.status(taskService.updateById(task));
	}

	@PostMapping(value = "/remove")
	@ApiOperation(value = "删除任务")
	public R delete(@RequestParam String ids){
		return R.status(taskService.deleteLogic(Func.toLongList(ids)));
	}


}
