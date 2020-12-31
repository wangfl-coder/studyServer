package org.springblade.task.service;

import org.springblade.core.mp.base.BaseService;
import org.springblade.core.tool.api.R;
import org.springblade.task.entity.Task;
import org.springblade.task.vo.TaskVO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface TaskService extends BaseService<Task> {


	/**
	 * 批量设置已完成子任务数
	 *
	 * @param tasks
	 * @return
	 */
	 List<TaskVO> batchSetCompletedCount(List<Task> tasks);

}
