package org.springblade.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springblade.task.entity.Task;

import java.util.List;

public interface TaskMapper extends BaseMapper<Task> {

	/**
	 * 已完成子任务数
	 *
	 * @param env    	运行环境
	 * @param taskId    任务Id
	 * @param endActId  结束流程节点Id
	 * @return
	 */
	int labelTaskCompleteCount(String env, Long taskId, String endActId);

	/**
	 * 已完成子任务数
	 *
	 * @param env    	运行环境
	 * @param taskId    任务Id
	 * @param endActId  结束流程节点Id
	 * @return
	 */
	int qualityInspectionTaskCompleteCount(String env, Long taskId, String endActId);

	/**
	 * 查询完成的各种组合的数量
	 * @return
	 */
	List<Integer> compositionCompleteCount(@Param("taskId") Long taskId);
}
