package org.springblade.task.service;

import org.springblade.adata.entity.Expert;
import org.springblade.core.mp.base.BaseService;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.entity.Task;

import java.util.List;

public interface QualityInspectionTaskService extends BaseService<QualityInspectionTask> {
	boolean startProcess(Long taskId, Integer count, Integer type, String processDefinitionId, List<LabelTask> labelTasks);
}
