package org.springblade.task.service;

import org.springblade.adata.entity.Expert;
import org.springblade.core.mp.base.BaseService;
import org.springblade.task.dto.ExpertBaseTaskDTO;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.Task;

import java.util.List;

public interface LabelTaskService extends BaseService<LabelTask> {
	boolean startProcess(String ProcessDefinitionId, Task task, List<Expert> experts);
}
