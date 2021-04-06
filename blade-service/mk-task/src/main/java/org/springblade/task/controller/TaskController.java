package org.springblade.task.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.adata.feign.IRealSetExpertClient;
import org.springblade.composition.feign.IStatisticsClient;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.constant.BladeConstant;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.flow.core.feign.IFlowClient;
import org.springblade.taskLog.entity.TaskLog;
//import org.springblade.log.feign.ITaskLogClient;
import org.springblade.flow.core.feign.IFlowEngineClient;
import org.springblade.mq.rabbit.feign.IMQRabbitClient;
import org.springblade.task.dto.ExpertBaseTaskDTO;
import org.springblade.task.dto.MergeExpertTaskDTO;
import org.springblade.task.dto.QualityInspectionDTO;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.MergeExpertTask;
import org.springblade.task.entity.Task;
import org.springblade.task.enums.LabelTaskTypeEnum;
import org.springblade.task.enums.TaskStatusEnum;
import org.springblade.task.enums.TaskTypeEnum;
import org.springblade.task.feign.ILabelTaskClient;
import org.springblade.task.mapper.LabelTaskMapper;
import org.springblade.task.mapper.TaskMapper;
import org.springblade.task.service.LabelTaskService;
import org.springblade.task.service.MergeExpertTaskService;
import org.springblade.task.service.QualityInspectionTaskService;
import org.springblade.task.service.TaskService;
import org.springblade.task.vo.TaskVO;
import org.springblade.taskLog.feign.ITaskLogClient;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;


import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


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
	private MergeExpertTaskService mergeExpertTaskService;
	private IStatisticsClient statisticsClient;
	private IFlowClient flowClient;
	private TaskMapper taskMapper;
	private LabelTaskMapper labelTaskMapper;
	private IRealSetExpertClient realSetExpertClient;
	private IMQRabbitClient mqRabbitClient;
	private IFlowEngineClient flowEngineClient;
	private ITaskLogClient iTaskLogclient;


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
		List<LabelTask> labelTasks = labelTaskService.queryUniqueCompleteTask(taskId);
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
		R processListRes = flowEngineClient.processList(null, null);
		if (!processListRes.isSuccess()) {
			return R.fail("获取流程实例列表失败");
		}
		List<LinkedHashMap<String,String>> processList = (List<LinkedHashMap<String,String>>)processListRes.getData();
		AtomicReference<String> processDefinitionId = new AtomicReference<>();
		processList.forEach(process -> {
			if ("Inspection".equals((String)process.get("key"))) {
				String id = (String)process.get("id");
				processDefinitionId.set(id);
			}
		});
		List<LabelTask> labelTasks=new ArrayList<>();
		Task task = Objects.requireNonNull(BeanUtil.copy(qualityInspectionDTO, Task.class));
		task.setTaskType(TaskTypeEnum.INSPECTION.getNum());
		//1不去重,2去重
		if(qualityInspectionDTO.getInspectionType()==1){
			labelTasks = labelTaskService.queryCompleteTask(task.getAnnotationTaskId());
		}else if(qualityInspectionDTO.getInspectionType()==2){
			labelTasks = labelTaskService.queryUniqueCompleteTask(task.getAnnotationTaskId());
		}
		if (labelTasks.size() > 0 && labelTasks.size()>=qualityInspectionDTO.getCount()) {
			boolean save = taskService.save(task);
			try {
				result = qualityInspectionTaskService.startProcess(processDefinitionId.get(), task.getCount(), task.getInspectionType(), task, labelTasks);
				return R.status(result);
			}catch (Exception e){
				taskService.removeById(task.getId());
				return R.fail("创建质检小任务失败");
			}
		}else if(labelTasks.size()<qualityInspectionDTO.getCount()) {
			return R.fail("质检count超过最大可以质检的数量，最大可以质检的数量为："+labelTasks.size());
		}else{
			return R.fail("获取标注完成的任务失败，或者没有标注完成的任务");
		}
	}

	@PostMapping(value = "/merge-expert/save")
	@ApiOperation(value = "添加合并任务")
	public R mergeExpertSave(@RequestBody MergeExpertTaskDTO mergeExpertTaskDTO) {
		Task labelTaskExported = taskService.getById(mergeExpertTaskDTO.getAnnotationTaskId());
//		if (labelTaskExported.getStatus() != TaskStatusEnum.EXPORTED.getNum()) {
//			return R.fail("这个标注任务并未生效，无法合并");
//		}

		Boolean result;
		R processListRes = flowEngineClient.processList(null, null);
		if (!processListRes.isSuccess()) {
			return R.fail("获取流程实例列表失败");
		}
		List<LinkedHashMap<String,String>> processList = (List<LinkedHashMap<String,String>>)processListRes.getData();
		AtomicReference<String> processDefinitionId = new AtomicReference<>();
		processList.forEach(process -> {
			if ("MergeExpert".equals((String)process.get("key"))) {
				String id = (String)process.get("id");
				processDefinitionId.set(id);
			}
		});

		Task task = Objects.requireNonNull(BeanUtil.copy(mergeExpertTaskDTO, Task.class));
		task.setTaskType(TaskTypeEnum.MERGE_EXPERT.getNum());
		List<LabelTask> labelTasks = labelTaskService.getByTaskId(task.getAnnotationTaskId());
		if (labelTasks.size() > 0) {
			boolean save = taskService.save(task);
			try {
				List<String> ids = new ArrayList<>();
				labelTasks.forEach( labelTask -> {
					if (labelTask.getType().equals(LabelTaskTypeEnum.LABEL.getNum())) {
						MergeExpertTask mergeTask = new MergeExpertTask();
						mergeTask.setProcessDefinitionId(processDefinitionId.get());
						// 保存
						mergeTask.setCreateTime(DateUtil.now());
						mergeTask.setMergeTaskId(task.getId());
						mergeTask.setPersonId(labelTask.getPersonId());
						mergeTask.setPersonName(labelTask.getPersonName());
						mergeTask.setLabelTaskId(labelTask.getId());
						mergeTask.setLabelProcessInstanceId(labelTask.getProcessInstanceId());
						mergeTask.setTaskId(labelTask.getTaskId());
						mergeTask.setTaskType(task.getTaskType());
						mergeExpertTaskService.save(mergeTask);

						String tmp = StringUtil.format("{},{},{},{}", task.getId(), mergeTask.getId(), labelTask.getExpertId(), labelTask.getPersonId());
						ids.add(tmp);
					}
				});
//				R<List<String>> expertIdsRes = expertClient.getExpertsId(mergeExpertTaskDTO.getAnnotationTaskId());
				R<Boolean> res = mqRabbitClient.preprocessPureSupPerson(ids);
//				result = mergeExpertTaskService.startProcess(mergeExpertTaskDTO.getProcessDefinitionId(), labelTasks.size(), 0, mergeTask, labelTasks);
				return R.status(res.getData());
			} catch (Exception e) {
				taskService.removeById(task.getId());
				return R.fail("创建合并小任务失败");
			}
		}else{
			return R.fail("获取标注完成的任务失败，或者没有标注完成的任务");
		}
	}

	@PostMapping(value = "/save")
	@ApiOperation(value = "添加标注任务")
	public R save(@RequestBody ExpertBaseTaskDTO expertBaseTaskDTO) {
		Boolean result;
		Boolean taskse = null;
		Task task = Objects.requireNonNull(BeanUtil.copy(expertBaseTaskDTO, Task.class));
		task.setTenantId(AuthUtil.getTenantId());
		task.setTaskType(TaskTypeEnum.LABEL.getNum());
		boolean save = taskService.save(task);
		R res_eb = expertClient.importExpertBase(task.getEbId(), task.getId());


		boolean flag = true;
		// 导入真题数据库中的所有专家
		if(expertBaseTaskDTO.getRealSetEbId()!=null) {
			R res_real_set_eb = realSetExpertClient.importExpertBase(expertBaseTaskDTO.getRealSetEbId(), task.getId());
			flag = res_real_set_eb.isSuccess();
		}
//res_eb.isSuccess() && flag
		if (res_eb.isSuccess() && flag) {
			R<List<Expert>> expertsResult = expertClient.getExpertsByTaskId(task.getId());
//			expertsResult.isSuccess()
			if (expertsResult.isSuccess()) {
				List<Expert> experts = expertsResult.getData();
				if(expertBaseTaskDTO.getRealSetRate() != null) {
					// 设置任务的真题比例，[0,100)
					task.setRealSetRate(expertBaseTaskDTO.getRealSetRate());
				}
				result = labelTaskService.startProcess(
					expertBaseTaskDTO.getProcessDefinitionId(),
					task,
					experts);
				task.setCount(experts.size());
				task.setRealSetEbId(expertBaseTaskDTO.getRealSetEbId());
				task.setStatus(TaskStatusEnum.IMPORTED.getNum());
				Boolean tasksave = taskService.saveOrUpdate(task);
				if (tasksave) {
					Long id = task.getId();
					Task tasks = taskService.getById(id);
					TaskLog tasklog2 = Objects.requireNonNull(BeanUtil.copy(tasks, TaskLog.class));
					taskse = saveLog(tasklog2);
				}

			} else {
				return R.fail("读取专家列表失败");
			}
		} else {
			taskService.removeById(task.getId());
			return R.fail("导入智库失败");
		}
		statisticsClient.initializeLabelTask(task.getId());
		return R.data(taskse);
	}

	@PostMapping(value = "/fix")
	@ApiOperation(value = "添加标注任务")
	public R fix(Long taskId) {
//			long taskId=L;
			String defId="AnnotationV2:12:35c81237-7c0f-11eb-96ae-5e380f867c41";
			Task task = taskService.getById(taskId);
			R<List<Expert>> expertsResult = expertClient.getExpertsByTaskId(taskId);
			if (expertsResult.isSuccess()) {
				List<Expert> experts = expertsResult.getData();
//				if(expertBaseTaskDTO.getRealSetRate() != null) {
//					// 设置任务的真题比例，[0,100)
//					task.setRealSetRate(expertBaseTaskDTO.getRealSetRate());
//				}
				for (Expert expert: experts) {
					QueryWrapper<LabelTask> labelTaskQueryWrapper = new QueryWrapper<>();
					labelTaskQueryWrapper.eq("person_id",expert.getId());
					Integer count = labelTaskMapper.selectCount(labelTaskQueryWrapper);
					if (count == 0) {
						LabelTask labelTask = labelTaskService.startFixProcess(
							defId,
							task,
							expert);
						//				task.setCount(experts.size());
						//				task.setRealSetEbId(expertBaseTaskDTO.getRealSetEbId());
						//				taskService.saveOrUpdate(task);
						statisticsClient.initializeSingleLabelTask(labelTask);
					}
				}

			} else {
				return R.fail("读取专家列表失败");
			}


		return R.status(true);
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
		@ApiImplicitParam(name = "taskType", value = "任务类型", paramType = "query", dataType = "integer"),
		@ApiImplicitParam(name = "tenantId", value = "要过滤的租户Id（只对管理租户起作用）", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "isCount", value = "是否需要统计", paramType = "query", dataType = "boolean")
	})
	@ApiOperation(value = "分页查询全部任务")
	public R<IPage<TaskVO>> list(@ApiIgnore @RequestParam Map<String, Object> task, Query query, BladeUser bladeUser) {
		boolean isCount = Boolean.parseBoolean((String) task.get("isCount"));
		task.remove("isCount");
		QueryWrapper<Task> queryWrapper = Condition.getQueryWrapper(task, Task.class);
		String name = (String)task.get("taskName");
		if (name != null) {
			queryWrapper.like("task_name", "%"+name+"%").orderByDesc("update_time");
		} else{
			queryWrapper.orderByDesc("update_time");
		}
		if (bladeUser.getTenantId().equals(BladeConstant.ADMIN_TENANT_ID) && StringUtil.isNotBlank((String)task.get("tenantId"))) {
			queryWrapper.lambda().eq(Task::getTenantId, task.get("tenantId"));
		} else {
			queryWrapper.lambda().eq(Task::getTenantId, bladeUser.getTenantId());
		}
		IPage<Task> pages = taskService.page(Condition.getPage(query), queryWrapper.orderByDesc("update_time"));
		if (!isCount){
			List<TaskVO> taskVOS = taskService.batchCastTaskVO(pages.getRecords());
			IPage<TaskVO> pageVo = new Page(pages.getCurrent(), pages.getSize(), pages.getTotal());
			pageVo.setRecords(taskVOS);
			return R.data(pageVo);
		}
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

//	private ITaskLogClient iTaskLogClient;
//	@PostMapping(value = "/save1")
//	public R save(TaskLog taskLog){
//		return R.data(iTaskLogClient.save(taskLog));
//	}

	public boolean saveLog(TaskLog taskLog) {
//		TaskLog taskLog = new TaskLog();
		int action=1;
//		String tenant_id =task.getTenantId();
//		Long task_id = task.getAnnotationTaskId();
//		Integer is_deleted = task.getIsDeleted();
//		Long create_user = task.getCreateUser();
//		Long create_dept = task.getCreateDept();
		String action_log ="导入真题";
		taskLog.setActionLog(action_log);
//		taskLog.setCreateDept(create_dept);
//		taskLog.setCreateUser(create_user);
//		taskLog.setIsDeleted(is_deleted);
//		taskLog.setTaskId(task_id);
//		taskLog.setTenantId(tenant_id);
		taskLog.setAction(action);
		boolean savelog = iTaskLogclient.save(taskLog);
		return savelog;
	}


}
