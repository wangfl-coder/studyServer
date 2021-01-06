package org.springblade.task.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springblade.task.entity.Field;
import org.springblade.task.entity.Task;

import java.util.List;
import java.util.Map;

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
	List<Field> allLabelTaskFields(Long id);

	/**
	 * 所有子任务的错误字段列表
	 *
	 * @param id    	质检任务id
	 * @return
	 */
	List<Field> allLabelTaskWrongFields(Long id);

	/**
	 * 查询完成的各种组合的数量
	 * @Param taskId 任务id
	 * @return
	 */
	List<Integer> compositionCompleteCount(@Param("taskId") Long taskId);

	/**
	 * 查询当前时刻组合的数量
	 * @Param taskId 任务id
	 * @return
	 */
	Integer compositionCount(@Param("env")String env,@Param("taskId") Long taskId);

}
