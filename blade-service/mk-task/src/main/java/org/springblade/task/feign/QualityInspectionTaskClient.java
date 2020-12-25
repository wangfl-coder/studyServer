package org.springblade.task.feign;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.service.QualityInspectionTaskService;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@NonDS
@ApiIgnore()
@RestController
@AllArgsConstructor
public class QualityInspectionTaskClient implements IQualityInspectionTaskClient{

	private final QualityInspectionTaskService qualityInspectionTaskService;

	@Override
	public R<QualityInspectionTask> queryQualityInspectionTask(String processInstanceId) {
		QueryWrapper<QualityInspectionTask> labelTaskQueryWrapper = new QueryWrapper<>();
		labelTaskQueryWrapper.eq("process_instance_id",processInstanceId);
		QualityInspectionTask qualityInspectionTask = qualityInspectionTaskService.getOne(labelTaskQueryWrapper);
		return R.data(qualityInspectionTask);
	}
}