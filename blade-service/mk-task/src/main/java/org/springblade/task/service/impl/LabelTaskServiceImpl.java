package org.springblade.task.service.impl;

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
import org.springblade.task.entity.LabelTask;
import org.springblade.flow.core.constant.ProcessConstant;
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.feign.IFlowClient;
import org.springblade.flow.core.utils.FlowUtil;
import org.springblade.flow.core.utils.TaskUtil;
import org.springblade.task.entity.Task;
import org.springblade.task.mapper.LabelTaskMapper;
import org.springblade.task.service.LabelTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class LabelTaskServiceImpl extends BaseServiceImpl<LabelTaskMapper, LabelTask> implements LabelTaskService {

	private final IFlowClient flowClient;


	@Override
	@Transactional(rollbackFor = Exception.class)
	// @GlobalTransactional
	public boolean startProcess(String processDefinitionId, Task task, List<Expert> experts) {
		String businessTable = FlowUtil.getBusinessTable(ProcessConstant.LABEL_KEY);
		//List<Expert> experts = persons.getData();
		experts.forEach( expert -> {
			LabelTask labelTask = new LabelTask();
			labelTask.setProcessDefinitionId(processDefinitionId);
			if (Func.isEmpty(labelTask.getId())) {
				// 保存leave
				labelTask.setCreateTime(DateUtil.now());
				boolean save = save(labelTask);
				// 启动流程
				Kv variables = Kv.create()
					.set(ProcessConstant.TASK_VARIABLE_CREATE_USER, AuthUtil.getUserName())
					.set("taskUser", TaskUtil.getTaskUser(labelTask.getTaskUser()))
					.set("priority", task.getPriority());
//					.set("taskPriority", task.getPriority());
				//set("days", DateUtil.between(subTask.getStartTime(), subTask.getEndTime()).toDays());
				R<BladeFlow> result = flowClient.startProcessInstanceById(labelTask.getProcessDefinitionId(), FlowUtil.getBusinessKey(businessTable, String.valueOf(labelTask.getId())), variables);
				if (result.isSuccess()) {
					log.debug("流程已启动,流程ID:" + result.getData().getProcessInstanceId());
					// 返回流程id写入leave
					labelTask.setProcessInstanceId(result.getData().getProcessInstanceId());
					labelTask.setTemplateId(task.getTemplateId());
					labelTask.setPersonId(expert.getId());
					labelTask.setPersonName(expert.getName());
					updateById(labelTask);
				} else {
					throw new ServiceException("开启流程失败");
				}
			} else {
				updateById(labelTask);
			}
		});
		return true;

	}
}
