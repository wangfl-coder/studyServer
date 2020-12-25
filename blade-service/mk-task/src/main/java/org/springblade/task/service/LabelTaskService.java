package org.springblade.task.service;

import org.springblade.adata.entity.Expert;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.tool.api.R;
import org.springblade.task.dto.ExpertBaseTaskDTO;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.Task;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface LabelTaskService extends BaseService<LabelTask> {

	boolean startProcess(String ProcessDefinitionId, Task task, List<Expert> experts);

	int queryCompleteTaskCount(Long taskId);

	List<LabelTask> queryCompleteTask(Long taskId);
}
