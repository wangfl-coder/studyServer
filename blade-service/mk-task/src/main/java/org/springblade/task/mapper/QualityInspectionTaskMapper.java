package org.springblade.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springblade.task.entity.QualityInspectionTask;

public interface QualityInspectionTaskMapper extends BaseMapper<QualityInspectionTask> {

	/**
	 * 已完成子任务数
	 *
	 * @param taskId    任务Id
	 * @param endActId  结束流程节点Id
	 * @return
	 */
	int completeCount(Long taskId, String endActId);

}
