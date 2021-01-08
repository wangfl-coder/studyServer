package org.springblade.task.feign;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.AllArgsConstructor;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.service.QualityInspectionTaskService;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

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

	@Override
	public R<QualityInspectionTask> queryQualityInspectionTaskById(Long inspectionId) {
		QueryWrapper<QualityInspectionTask> labelTaskQueryWrapper = new QueryWrapper<>();
		labelTaskQueryWrapper.eq("id",inspectionId);
		QualityInspectionTask qualityInspectionTask = qualityInspectionTaskService.getOne(labelTaskQueryWrapper);
		return R.data(qualityInspectionTask);
	}

	@Override
	public R updateQualityInspectionTaskById(QualityInspectionTask qualityInspectionTask) {
		UpdateWrapper<QualityInspectionTask> qualityInspectionTaskUpdateWrapper = new UpdateWrapper<>();
		qualityInspectionTaskUpdateWrapper.eq("id",qualityInspectionTask.getId()).set("time",qualityInspectionTask.getTime()).set("picture",qualityInspectionTask.getPicture()).set("status",qualityInspectionTask.getStatus()).set("remark",qualityInspectionTask.getRemark()).set("update_time", DateUtil.now());
		boolean update = qualityInspectionTaskService.update(qualityInspectionTaskUpdateWrapper);
		return R.status(update);
	}

	@Override
	public R<List<QualityInspectionTask>> queryQualityInspectionTaskByPersonId(Long personId) {
		QueryWrapper<QualityInspectionTask> qualityInspectionTaskQueryWrapper = new QueryWrapper<>();
		qualityInspectionTaskQueryWrapper.eq("person_id",personId);
		List<QualityInspectionTask> qualityInspectionTasks = qualityInspectionTaskService.list(qualityInspectionTaskQueryWrapper);
		return R.data(qualityInspectionTasks);
	}

}
