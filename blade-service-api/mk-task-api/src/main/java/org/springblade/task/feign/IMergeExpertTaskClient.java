package org.springblade.task.feign;

import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.tool.api.R;
import org.springblade.task.entity.MergeExpertTask;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springblade.task.feign.ILabelTaskClient.CHANGE_STATUS;

@FeignClient(value = LauncherConstant.MKAPP_TASK_NAME)
public interface IMergeExpertTaskClient {
	String API_PREFIX = "/client";
	String GET_TASK_BY_ID = API_PREFIX + "/get-merge-expert-task-by-id";
	String SAVE_TASK = API_PREFIX + "/save-merge-expert-task";
	String GET_TASK_BY_TEMPLATE = API_PREFIX + "/get-merge-expert-task-by-template";
	String GET_SUBTASK_COUNT = API_PREFIX + "/get-merge-expert-subtask-count";
	String CHANGE_STATUS = API_PREFIX + "/change-merge-expert-task-status";


	@PostMapping(SAVE_TASK)
	R saveMergeExpertTask(@RequestBody MergeExpertTask task);

	@GetMapping(GET_TASK_BY_ID)
	R<MergeExpertTask> getById(@RequestParam Long id);

	@GetMapping(CHANGE_STATUS)
	R changeStatus(@RequestParam Long id);

//	@GetMapping(GET_SUBTASK_COUNT)
//	R<Integer> getSubMergeExpertTaskCount(@RequestParam("id") Long id);
}
