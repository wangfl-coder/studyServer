package org.springblade.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springblade.task.entity.Field;
import org.springblade.task.vo.ExpertLabelTaskVO;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.vo.ExpertQualityInspectionTaskVO;

import java.util.List;

public interface QualityInspectionTaskMapper extends BaseMapper<QualityInspectionTask> {

	/**
	 * 已完成子任务数
	 *
	 * @param env    	运行环境
	 * @param taskId    任务Id
	 * @return
	 */
	int completeCount(String env, Long taskId);

	/**
	 * 根据专家id查对应子任务流程实例id
	 * @param expertId    专家真正的id
	 * @return
	 */
	List<ExpertQualityInspectionTaskVO> personIdToProcessInstance(String expertId);

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
}
