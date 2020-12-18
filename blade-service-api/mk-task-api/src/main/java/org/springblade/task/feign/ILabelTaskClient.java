package org.springblade.task.feign;

import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.launch.constant.AppConstant;
import org.springblade.core.tool.api.R;
import org.springblade.task.dto.ExpertTaskDTO;
import org.springblade.task.entity.LabelTask;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = LauncherConstant.MKAPP_TASK_NAME)
public interface ILabelTaskClient {
	String API_PREFIX = "/client";
	String START_LABEL_PROCESS = API_PREFIX + "/start-label-process";
	String QUERY_LABEL_TASK = API_PREFIX + "/query-label-task";
	String CHANGE_STATUS = API_PREFIX + "/change-status";
	String QUERY_COMPLETE_LABEL_TASK = API_PREFIX + "/complete-label-task";

	@PostMapping(START_LABEL_PROCESS)
	R startProcess(@RequestBody ExpertTaskDTO expertTaskDTO);

	@GetMapping(QUERY_LABEL_TASK)
	R<LabelTask> queryLabelTask(@RequestParam(value = "processInstanceId") String processInstanceId);

	@GetMapping(CHANGE_STATUS)
	R changeStatus(@RequestParam(value = "processInstanceId") String processInstanceId);

	@GetMapping(QUERY_COMPLETE_LABEL_TASK)
	R<List<LabelTask>> queryCompleteTask(@RequestParam(value = "taskId") Long taskId);


}
