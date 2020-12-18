package org.springblade.task.feign;

import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.tool.api.R;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = LauncherConstant.MKAPP_TASK_NAME)
public interface IQualityInspectionTaskClient {

	String API_PREFIX = "/client";
	String QUERY_QUALITY_INSPECTION_TASK = API_PREFIX + "/query-quality-inspection-task";

	@GetMapping(QUERY_QUALITY_INSPECTION_TASK)
	R<QualityInspectionTask> queryQualityInspectionTask(@RequestParam(value = "processInstanceId") String processInstanceId);
}
