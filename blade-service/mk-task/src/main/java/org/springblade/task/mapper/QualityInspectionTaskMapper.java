package org.springblade.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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


}
