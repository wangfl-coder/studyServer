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
import org.springblade.desk.entity.Task;
import org.springblade.desk.mapper.TaskMapper;
import org.springblade.desk.service.TaskService;
import org.springblade.flow.core.constant.ProcessConstant;
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.feign.IFlowClient;
import org.springblade.flow.core.utils.FlowUtil;
import org.springblade.flow.core.utils.TaskUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class TaskServiceImpl extends BaseServiceImpl<TaskMapper, Task> implements TaskService {

	private final IFlowClient flowClient;

	@Override
	@Transactional(rollbackFor = Exception.class)
	// @GlobalTransactional
	public boolean startProcess(Task task) {
		String businessTable = FlowUtil.getBusinessTable(ProcessConstant.LEAVE_KEY);
		if (Func.isEmpty(task.getId())) {
			// 保存leave
			task.setCreateTime(DateUtil.now());
			save(task);
			// 启动流程
			Kv variables = Kv.create()
				.set(ProcessConstant.TASK_VARIABLE_CREATE_USER, AuthUtil.getUserName())
				.set("taskUser", TaskUtil.getTaskUser(task.getTaskUser()))
				.set("days", DateUtil.between(task.getStartTime(), task.getEndTime()).toDays());
			R<BladeFlow> result = flowClient.startProcessInstanceById(task.getProcessDefinitionId(), FlowUtil.getBusinessKey(businessTable, String.valueOf(task.getId())), variables);
			if (result.isSuccess()) {
				log.debug("流程已启动,流程ID:" + result.getData().getProcessInstanceId());
				// 返回流程id写入leave
				task.setProcessInstanceId(result.getData().getProcessInstanceId());
				updateById(task);
			} else {
				throw new ServiceException("开启流程失败");
			}
		} else {
			updateById(task);
		}
		return true;
	}

}
