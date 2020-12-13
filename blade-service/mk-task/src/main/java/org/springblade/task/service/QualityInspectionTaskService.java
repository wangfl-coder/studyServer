package org.springblade.task.service;

import org.springblade.core.mp.base.BaseService;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.entity.Task;

import java.util.List;

public interface QualityInspectionTaskService extends BaseService<QualityInspectionTask> {
	boolean startProcess(String ProcessDefinitionId, Task task, List<Long> ids);
}
