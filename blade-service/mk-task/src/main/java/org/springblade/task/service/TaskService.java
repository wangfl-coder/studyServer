package org.springblade.task.service;

import org.apache.ibatis.annotations.Param;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.task.entity.Task;
import org.springblade.task.vo.TaskVO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface TaskService extends BaseService<Task> {

	/**
	 * 设置已完成子任务数
	 *
	 * @param task
	 * @return
	 */
	TaskVO setCompletedCount(Task task);

	/**
	 * 批量设置已完成子任务数
	 *
	 * @param tasks
	 * @return
	 */
	 List<TaskVO> batchSetCompletedCount(List<Task> tasks);

	/**
	 * 设置质检正确的子任务数
	 *
	 * @param task
	 * @return
	 */
	TaskVO setCorrectCount(TaskVO task);

	/**
	 * 批量设置质检正确的子任务数
	 *
	 * @param tasks
	 * @return
	 */
	List<TaskVO> batchSetCorrectCount(List<TaskVO> tasks);

	/**
	 * 查询当前时刻组合的数量
	 * @Param taskId 任务id
	 * @return
	 */
	Integer compositionCount(@Param("taskId") Long taskId);
}
