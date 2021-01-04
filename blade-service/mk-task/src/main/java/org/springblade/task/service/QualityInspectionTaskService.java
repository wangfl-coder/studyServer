package org.springblade.task.service;

import io.swagger.models.auth.In;
import org.springblade.adata.entity.Expert;
import org.springblade.core.mp.base.BaseService;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.entity.Task;

import java.util.List;

public interface QualityInspectionTaskService extends BaseService<QualityInspectionTask> {
	boolean startProcess(String processDefinitionId, Integer count, Integer inspectionType,Task task, List<LabelTask> labelTasks);

	int completeCount(Long taskId, String endActId);

	int correctCount(Long taskId);
}
