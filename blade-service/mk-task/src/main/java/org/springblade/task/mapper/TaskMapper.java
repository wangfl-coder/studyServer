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
	 * 所有子任务的字段列表
	 *
	 * @param id    	质检任务id
	 * @return
	 */
	List<String> allLabelTaskFields(Long id);

	/**
	 * 所有子任务的错误字段列表
	 *
	 * @param id    	质检任务id
	 * @return
	 */
	List<String> allLabelTaskWrongFields(Long id);
}
