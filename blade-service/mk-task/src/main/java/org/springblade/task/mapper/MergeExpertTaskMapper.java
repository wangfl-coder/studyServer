package org.springblade.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springblade.task.entity.Field;
import org.springblade.task.entity.MergeExpertTask;
import org.springblade.task.entity.Task;
import org.springblade.task.vo.ExpertQualityInspectionTaskVO;

import java.util.List;

public interface MergeExpertTaskMapper extends BaseMapper<MergeExpertTask> {

	/**
	 * 已完成子任务数
	 *
	 * @param taskId    任务Id
	 * @return
	 */
	int completeCount(@Param("taskId")Long taskId);

	/**
	 * 根据专家id查对应子任务流程实例id
	 * @param expertId    专家真正的id
	 * @return
	 */
	List<ExpertQualityInspectionTaskVO> personIdToProcessInstance(@Param("expertId")String expertId);

	/**
	 * 所有子任务的字段列表
	 *
	 * @param taskId    	质检任务id
	 * @return
	 */
	List<Field> allLabelTaskFields(@Param("taskId")Long taskId);

	/**
	 * 所有子任务的错误字段列表
	 *
	 * @param taskId    	质检任务id
	 * @return
	 */
	List<Field> allLabelTaskWrongFields(@Param("taskId")Long taskId);
}
