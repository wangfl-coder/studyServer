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
	 * @param taskId    任务Id
	 * @return
	 */
	int labelTaskCompleteCount(@Param("taskId")Long taskId);

	/**
	 * 已完成子任务数
	 *
	 * @param taskId    任务Id
	 * @return
	 */
	int qualityInspectionTaskCompleteCount(@Param("taskId")Long taskId);

	/**
	 * 查询完成的各种组合的数量
	 * @Param taskId 任务id
	 * @return
	 */
	List<Integer> compositionCompleteCount(@Param("taskId") Long taskId);

	/**
	 * 查询当前时刻组合的数量 wangshan：伦哥写的这个为当前时刻可接任务的组合的总数
	 * @Param taskId 任务id
	 * @return
	 */
	Integer compositionCount(@Param("taskId") Long taskId);

	/**
	 * 查询当前时刻组合的数量
	 * @Param taskId 任务id
	 * @return
	 */
	Integer compositionCount2(@Param("taskId") Long taskId);
}
