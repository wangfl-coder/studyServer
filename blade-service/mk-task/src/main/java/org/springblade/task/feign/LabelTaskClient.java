package org.springblade.task.feign;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.AllArgsConstructor;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.task.dto.ExpertTaskDTO;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.Task;
import org.springblade.task.service.LabelTaskService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@NonDS
@ApiIgnore()
@RestController
@AllArgsConstructor
public class LabelTaskClient implements ILabelTaskClient {

	private LabelTaskService labelTaskService;
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
		labelTaskUpdateWrapper.eq("process_instance_id",processInstanceId).set("status",0);
		boolean update = labelTaskService.update(labelTaskUpdateWrapper);
		return R.status(update);
	}

	@Override
	@GetMapping(QUERY_COMPLETE_LABEL_TASK)
	public R<List<LabelTask>> queryCompleteTask(Long taskId) {
		QueryWrapper<LabelTask> labelTaskQueryWrapper = new QueryWrapper<>();
		HashMap<String, Object> map = new HashMap<>();
		map.put("task_id",taskId);
		map.put("status",0);
		labelTaskQueryWrapper.allEq(map);
		List<LabelTask> list = labelTaskService.list(labelTaskQueryWrapper);
		return R.data(list);
	}


}
