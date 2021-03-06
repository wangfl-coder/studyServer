package org.springblade.task.feign;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.AllArgsConstructor;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.flow.core.feign.IFlowClient;
import org.springblade.task.vo.CompositionClaimListVO;
import org.springblade.task.vo.ExpertLabelTaskVO;
import org.springblade.task.dto.ExpertTaskDTO;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.Task;
import org.springblade.task.service.LabelTaskService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
//import sun.security.krb5.internal.LastReq;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@NonDS
@ApiIgnore()
@RestController
@AllArgsConstructor
public class LabelTaskClient implements ILabelTaskClient {

	private final LabelTaskService labelTaskService;
	private final IFlowClient flowClient;

	@Override
	@PostMapping(START_LABEL_PROCESS)
	public R startProcess(@RequestBody ExpertTaskDTO expertTaskDTO) {
//		Task task = Objects.requireNonNull(BeanUtil.copy(expertTaskDTO, Task.class));
//		boolean b = labelTaskService.startProcess(expertTaskDTO.getProcessDefinitionId(), task, expertTaskDTO.getExperts());
//		return R.status(b);
		return R.status(false);
	}

	@Override
	@PostMapping(START_LABEL_REALSET_PROCESS)
	public R<Map<String, String>> startRealSetProcess(@RequestParam String realSetProcessDefinitions, @RequestBody Task task) {
		Map<String, String> resultMap = labelTaskService.startRealSetProcess(realSetProcessDefinitions, task);
		return R.data(resultMap);
	}

	@Override
	@GetMapping(QUERY_LABEL_TASK)
	public R<LabelTask> queryLabelTask(String processInstanceId) {
		QueryWrapper<LabelTask> labelTaskQueryWrapper = new QueryWrapper<>();
		labelTaskQueryWrapper.eq("process_instance_id",processInstanceId);
		LabelTask labelTask = labelTaskService.getOne(labelTaskQueryWrapper);
		return R.data(labelTask);
	}

	@Override
	@GetMapping(CHANGE_STATUS)
	public R changeStatus(String processInstanceId) {
		UpdateWrapper<LabelTask> labelTaskUpdateWrapper = new UpdateWrapper<>();
		labelTaskUpdateWrapper.eq("process_instance_id",processInstanceId).set("status",2);
		boolean update = labelTaskService.update(labelTaskUpdateWrapper);
		return R.status(update);
	}

	@Override
	@GetMapping(QUERY_COMPLETE_LABEL_TASK)
	public R<List<LabelTask>> queryCompleteTask(Long taskId) {
		List<LabelTask> labelTasks = labelTaskService.queryCompleteTask(taskId);
		return R.data(labelTasks);
	}

	@Override
	@GetMapping(QUERY_COMPLETE_LABEL_TASK_COUNT)
	public R<Integer> completeCount(Long taskId) {
		Integer res = labelTaskService.completeCount(taskId);
		return R.data(res);
	}

	@Override
	@GetMapping(QUERY_LABEL_TASK_ALL)
	public R<List<LabelTask>> queryLabelTaskAll(Long taskId, Integer taskType) {
		QueryWrapper<LabelTask> labelTaskQueryWrapper = new QueryWrapper<>();
		labelTaskQueryWrapper.eq("task_id",taskId)
							 .eq("type", taskType);
		List<LabelTask> list = labelTaskService.list(labelTaskQueryWrapper);
		return R.data(list);
	}

	@Override
	public R<List<LabelTask>> queryLabelTaskByPersonId(Long personId) {
		QueryWrapper<LabelTask> labelTaskQueryWrapper = new QueryWrapper<>();
		labelTaskQueryWrapper.eq("person_id",personId);
		return R.data(labelTaskService.list(labelTaskQueryWrapper));
	}

	@Override
	public R<List<ExpertLabelTaskVO>> queryLabelTaskByExpertId(String expertId) {
		return R.data(labelTaskService.personIdToProcessInstance(expertId));
	}

	@Override
	public R queryLabelTaskDoneCount(String param2) {
		return R.data(labelTaskService.annotationDoneCount(param2));
	}

	@Override
	public R queryLabelTaskTodoCount(String param2) {
		return R.data(labelTaskService.annotationTodoCount(param2));
	}

	@Override
	public R queryLabelTaskClaimCount(List<String> param2) {
		return R.data(labelTaskService.annotationClaimCount(param2));
	}

	@Override
	public R<List<CompositionClaimListVO>> compositionClaimList(List<String> roleAliases) {
		return R.data(labelTaskService.compositionClaimList(roleAliases));
	}
}
