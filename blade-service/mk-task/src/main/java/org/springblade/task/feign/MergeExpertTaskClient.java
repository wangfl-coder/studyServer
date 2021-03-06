package org.springblade.task.feign;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.task.entity.MergeExpertTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.service.MergeExpertTaskService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import static org.springblade.task.feign.IMergeExpertTaskClient.CHANGE_STATUS;


@NonDS
@ApiIgnore()
@RestController
@AllArgsConstructor
public class MergeExpertTaskClient implements IMergeExpertTaskClient{

	private final MergeExpertTaskService taskService;

	@Override
	public R<MergeExpertTask> queryMergeExpertTask(String processInstanceId) {
		QueryWrapper<MergeExpertTask> taskQueryWrapper = new QueryWrapper<>();
		taskQueryWrapper.eq("process_instance_id",processInstanceId);
		MergeExpertTask task = taskService.getOne(taskQueryWrapper);
		return R.data(task);
	}

	@Override
	@PostMapping(SAVE_TASK)
	public R saveMergeExpertTask(@RequestBody MergeExpertTask task) {
		boolean save = taskService.save(task);
		return R.status(save);
	}

	@Override
	@GetMapping(GET_TASK_BY_ID)
	public R<MergeExpertTask> getById(@RequestParam Long id) {
		MergeExpertTask task = taskService.getById(id);
		return R.data(task);
	}

	@Override
	@GetMapping(CHANGE_STATUS)
	public R changeStatus(@RequestParam Long id, @RequestParam Integer status) {
		UpdateWrapper<MergeExpertTask> mergeExpertTaskUpdateWrapper = new UpdateWrapper<>();
		mergeExpertTaskUpdateWrapper.eq("id",id).set("status",status);
		boolean update = taskService.update(mergeExpertTaskUpdateWrapper);
		return R.status(update);
	}

//	@Override
//	@GetMapping(GET_SUBTASK_COUNT)
//	public R<Integer> getSubMergeExpertTaskCount(@RequestParam("id") Long id) {
//		int count = taskService.getSubMergeExpertTaskCount(id);
//		return R.data(count);
//	}
}
