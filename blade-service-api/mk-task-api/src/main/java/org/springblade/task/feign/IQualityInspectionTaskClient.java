package org.springblade.task.feign;

import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.tool.api.R;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = LauncherConstant.MKAPP_TASK_NAME)
public interface IQualityInspectionTaskClient {

	String API_PREFIX = "/client";
	String QUERY_QUALITY_INSPECTION_TASK_BY_INSTANCEID = API_PREFIX + "/query-quality-inspection-task-by-instance-id";
	String QUERY_QUALITY_INSPECTION_TASK_BY_ID = API_PREFIX + "/query-quality-inspection-task-by-id";
	String UPDATE_QUALITY_INSPECTION_TASK_BY_ID = API_PREFIX + "/update-quality-inspection-task-by-id";
	String QUERY_QUALITY_INSPECTION_TASK_BY_PERSON_ID = API_PREFIX + "/query-quality-inspection-task-by-person-id";

	@GetMapping(QUERY_QUALITY_INSPECTION_TASK_BY_INSTANCEID)
	R<QualityInspectionTask> queryQualityInspectionTask(@RequestParam(value = "processInstanceId") String processInstanceId);

	@GetMapping(QUERY_QUALITY_INSPECTION_TASK_BY_ID)
	R<QualityInspectionTask> queryQualityInspectionTaskById(@RequestParam(value = "inspectionId") Long inspectionId);

	@PostMapping(UPDATE_QUALITY_INSPECTION_TASK_BY_ID)
	R updateQualityInspectionTaskById(@RequestBody QualityInspectionTask qualityInspectionTask);

	@GetMapping(QUERY_QUALITY_INSPECTION_TASK_BY_PERSON_ID)
	R<List<QualityInspectionTask>> queryQualityInspectionTaskByPersonId(@RequestParam(value = "personId") Long personId);


}
