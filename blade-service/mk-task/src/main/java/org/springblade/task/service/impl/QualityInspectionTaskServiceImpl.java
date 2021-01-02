package org.springblade.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.adata.entity.Expert;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.flow.core.constant.ProcessConstant;
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.feign.IFlowClient;
import org.springblade.flow.core.utils.FlowUtil;
import org.springblade.flow.core.utils.TaskUtil;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.entity.Task;
import org.springblade.task.mapper.QualityInspectionTaskMapper;
import org.springblade.task.service.QualityInspectionTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@AllArgsConstructor
@Slf4j
public class QualityInspectionTaskServiceImpl extends BaseServiceImpl<QualityInspectionTaskMapper, QualityInspectionTask> implements QualityInspectionTaskService {

	private final IFlowClient flowClient;

	@Override
	@Transactional(rollbackFor = Exception.class)
	// @GlobalTransactional
	public boolean startProcess(String processDefinitionId,Integer count,Integer inspectionType,Task task, List<LabelTask> labelTasks) {
		String businessTable = FlowUtil.getBusinessTable(ProcessConstant.QUALITY_INSPECTION_KEY);
		Random random = new Random();
		HashSet<LabelTask> set = new HashSet<>();
		do {
			set.add(labelTasks.get(random.nextInt(labelTasks.size())));
		} while (set.size() != count);
		ArrayList<LabelTask> labelTasks1 = new ArrayList<>(set);
		labelTasks1.forEach( labelTask -> {
			QualityInspectionTask inspectionTask = new QualityInspectionTask();
			inspectionTask.setProcessDefinitionId(processDefinitionId);
			if (Func.isEmpty(inspectionTask.getId())) {
				// 保存leave
				inspectionTask.setCreateTime(DateUtil.now());
				boolean save = save(inspectionTask);
				// 启动流程
				Kv variables = Kv.create()
					.set(ProcessConstant.TASK_VARIABLE_CREATE_USER, AuthUtil.getUserName())
					.set("taskUser", TaskUtil.getTaskUser(inspectionTask.getTaskUser()))
					.set("type", inspectionType)
				    .set("priority", task.getPriority());
					//set("days", DateUtil.between(subTask.getStartTime(), subTask.getEndTime()).toDays());
				R<BladeFlow> result = flowClient.startProcessInstanceById(processDefinitionId, FlowUtil.getBusinessKey(businessTable, String.valueOf(inspectionTask.getId())), variables);
				if (result.isSuccess()) {
					log.debug("流程已启动,流程ID:" + result.getData().getProcessInstanceId());
					// 返回流程id写入leave
					inspectionTask.setProcessInstanceId(result.getData().getProcessInstanceId());
					inspectionTask.setTemplateId(labelTask.getTemplateId());
					inspectionTask.setInspectionTaskId(task.getId());
					inspectionTask.setPersonId(labelTask.getPersonId());
					inspectionTask.setPersonName(labelTask.getPersonName());
					inspectionTask.setLabelTaskId(labelTask.getId());
					inspectionTask.setLabelProcessInstanceId(labelTask.getProcessInstanceId());
					inspectionTask.setTaskId(labelTask.getTaskId());
					inspectionTask.setTaskType(task.getTaskType());
					inspectionTask.setInspectionType(inspectionType);
					updateById(inspectionTask);
				} else {
					throw new ServiceException("开启流程失败");
				}
			} else {
				updateById(inspectionTask);
			}
		});
		return true;
	}

	@Override
	public int completeCount(Long taskId, String endActId) {
		return baseMapper.completeCount(taskId, endActId);
	}

}
