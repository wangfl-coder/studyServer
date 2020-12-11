package org.springblade.desk.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.desk.entity.SubTask;
import org.springblade.desk.mapper.SubTaskMapper;
import org.springblade.desk.service.SubTaskService;
import org.springblade.flow.core.constant.ProcessConstant;
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.feign.IFlowClient;
import org.springblade.flow.core.utils.FlowUtil;
import org.springblade.flow.core.utils.TaskUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class SubTaskServiceImpl extends BaseServiceImpl<SubTaskMapper, SubTask> implements SubTaskService {

	private final IFlowClient flowClient;

	@Override
	@Transactional(rollbackFor = Exception.class)
	// @GlobalTransactional
	public boolean startProcess(Long templateId, Long compositionId) {
		String businessTable = FlowUtil.getBusinessTable(ProcessConstant.LEAVE_KEY);
		for(int i=0;i<persons.size();i++){
			SubTask subTask = new SubTask();
			if (Func.isEmpty(subTask.getId())) {
				// 保存leave
				subTask.setCreateTime(DateUtil.now());
				save(subTask);
				// 启动流程
				Kv variables = Kv.create()
					.set(ProcessConstant.TASK_VARIABLE_CREATE_USER, AuthUtil.getUserName())
					.set("taskUser", TaskUtil.getTaskUser(subTask.getTaskUser()))
					.set("days", DateUtil.between(subTask.getStartTime(), subTask.getEndTime()).toDays());
				R<BladeFlow> result = flowClient.startProcessInstanceById(subTask.getProcessDefinitionId(), FlowUtil.getBusinessKey(businessTable, String.valueOf(subTask.getId())), variables);
				if (result.isSuccess()) {
					log.debug("流程已启动,流程ID:" + result.getData().getProcessInstanceId());
					// 返回流程id写入leave
					subTask.setProcessInstanceId(result.getData().getProcessInstanceId());
					subTask.setPersonId(persons.get(i).getId());
					subTask.setTemplateId(templateId);
					subTask.setCompositionId(compositionId);
					updateById(subTask);
				} else {
					throw new ServiceException("开启流程失败");
				}
			} else {
				updateById(subTask);
			}
		}
		return true;

	}
}
