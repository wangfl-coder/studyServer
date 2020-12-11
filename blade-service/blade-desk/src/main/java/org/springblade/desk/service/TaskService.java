package org.springblade.desk.service;

import org.springblade.core.mp.base.BaseService;
import org.springblade.desk.entity.Task;

public interface TaskService extends BaseService<Task> {
	boolean startProcess(Task task);
}
