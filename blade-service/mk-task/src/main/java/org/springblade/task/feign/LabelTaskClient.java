package org.springblade.task.feign;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.flow.core.feign.IFlowClient;
import org.springblade.task.dto.ExpertTaskDTO;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.Task;
import org.springblade.task.service.LabelTaskService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
		Task task = Objects.requireNonNull(BeanUtil.copy(expertTaskDTO, Task.class));
		boolean b = labelTaskService.startProcess(expertTaskDTO.getProcessDefinitionId(), task, expertTaskDTO.getExperts());
		return R.status(b);
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
		QueryWrapper<LabelTask> labelTaskQueryWrapper = new QueryWrapper<>();
		HashMap<String, Object> map = new HashMap<>();
		map.put("task_id",taskId);
		map.put("status",2);
		labelTaskQueryWrapper.allEq(map);
		List<LabelTask> list = labelTaskService.list(labelTaskQueryWrapper);
		return R.data(list);
	}

	@Override
	@GetMapping(QUERY_COMPLETE_LABEL_TASK2)
	public R<ArrayList<LabelTask>> queryCompleteTask2(Long taskId) {
		List<LabelTask> list = labelTaskService.list(Wrappers.<LabelTask>query().lambda().eq(LabelTask::getTaskId, taskId));
		List<String> ids = new ArrayList<>();
		list.forEach(task -> ids.add(task.getProcessInstanceId()));
		R<Kv> processInstancesFinished = flowClient.isProcessInstancesFinished(ids);
		ArrayList<LabelTask> labelTasks = new ArrayList<>();
		if (processInstancesFinished.isSuccess()) {
			Kv kv = processInstancesFinished.getData();
			list.forEach(labelTask -> {
				String processInstanceId = labelTask.getProcessInstanceId();
				if ((boolean)kv.get(processInstanceId)) {
					UpdateWrapper<LabelTask> labelTaskUpdateWrapper = new UpdateWrapper<>();
					labelTaskUpdateWrapper.eq("process_instance_id",processInstanceId).set("status",2);
					labelTaskService.update(labelTaskUpdateWrapper);
					labelTasks.add(labelTask);
				}
			});
		}
		return R.data(labelTasks);
	}

	@Override
	public R queryCompleteTaskCount(Long taskId) {
		int count=0;
		List<LabelTask> list = labelTaskService.list(Wrappers.<LabelTask>query().lambda().eq(LabelTask::getTaskId, taskId));
		List<String> ids = new ArrayList<>();
		list.forEach(task -> ids.add(task.getProcessInstanceId()));
		R<Kv> processInstancesFinished = flowClient.isProcessInstancesFinished(ids);
		if (processInstancesFinished.isSuccess()) {
			count+=1;
		}
		return R.data(count);
	}

	@Override
	public R<List<LabelTask>> queryLabelTask(Long taskId) {
		QueryWrapper<LabelTask> labelTaskQueryWrapper = new QueryWrapper<>();
		labelTaskQueryWrapper.eq("task_id",taskId);
		List<LabelTask> list = labelTaskService.list(labelTaskQueryWrapper);
		return R.data(list);
	}
}
