package org.springblade.task.feign;

import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.tool.api.R;
import org.springblade.task.vo.ExpertLabelTaskVO;
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
	String QUERY_COMPLETE_LABEL_TASK_COUNT = API_PREFIX + "/complete-label-task-count";
	String QUERY_COMPLETE_LABEL_TASK_COUNT_LIST = API_PREFIX + "/complete-label-task-count-list";
	String QUERY_LABEL_TASK_ALL = API_PREFIX + "/query-label-task-all";
	String QUERY_LABEL_TASK_BY_PERSON_ID = API_PREFIX + "/query-label-task-by-person-id";
	String QUERY_LABEL_TASK_BY_EXPERT_ID = API_PREFIX + "/query-label-task-by-expert-id";
	String QUERY_LABEL_TASK_DONE_COUNT = API_PREFIX + "/query-label-task-done-count";
	String QUERY_LABEL_TASK_TODO_COUNT = API_PREFIX + "/query-label-task-todo-count";
	String QUERY_LABEL_TASK_CLAIM_COUNT = API_PREFIX + "/query-label-task-claim-count";

	@PostMapping(START_LABEL_PROCESS)
	R startProcess(@RequestBody ExpertTaskDTO expertTaskDTO);

	@GetMapping(QUERY_LABEL_TASK)
	R<LabelTask> queryLabelTask(@RequestParam(value = "processInstanceId") String processInstanceId);

	@GetMapping(CHANGE_STATUS)
	R changeStatus(@RequestParam(value = "processInstanceId") String processInstanceId);

	@GetMapping(QUERY_COMPLETE_LABEL_TASK)
	R<List<LabelTask>> queryCompleteTask(@RequestParam(value = "taskId") Long taskId);

	@GetMapping(QUERY_COMPLETE_LABEL_TASK_COUNT)
	R<Integer> completeCount(@RequestParam(value = "taskId") Long taskId);

	@GetMapping(QUERY_LABEL_TASK_ALL)
	R<List<LabelTask>> queryLabelTaskAll(@RequestParam(value = "taskId") Long taskId);

	@GetMapping(QUERY_LABEL_TASK_BY_PERSON_ID)
	R<List<LabelTask>> queryLabelTaskByPersonId(@RequestParam(value = "personId") Long personId);

	@GetMapping(QUERY_LABEL_TASK_BY_EXPERT_ID)
	R<List<ExpertLabelTaskVO>> queryLabelTaskByExpertId(@RequestParam(value = "expertId") String expertId);

	@GetMapping(QUERY_LABEL_TASK_DONE_COUNT)
	R queryLabelTaskDoneCount(@RequestParam(value = "param2") String param2);

	@GetMapping(QUERY_LABEL_TASK_TODO_COUNT)
	R queryLabelTaskTodoCount(@RequestParam(value = "param2") String param2);

	@GetMapping(QUERY_LABEL_TASK_CLAIM_COUNT)
	R queryLabelTaskClaimCount(@RequestParam(value = "param2") List<String> param2);
}
