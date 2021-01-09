package org.springblade.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
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
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.entity.Task;
import org.springblade.task.mapper.LabelTaskMapper;
import org.springblade.task.service.LabelTaskService;
import org.springblade.task.service.QualityInspectionTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class LabelTaskServiceImpl extends BaseServiceImpl<LabelTaskMapper, LabelTask> implements LabelTaskService {

	private final IFlowClient flowClient;
	private final QualityInspectionTaskService qualityInspectionTaskService;

	@Value("${spring.profiles.active}")
	public String env;

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
				R<BladeFlow> result = flowClient.startProcessInstanceById(labelTask.getProcessDefinitionId(), FlowUtil.getBusinessKey(businessTable, String.valueOf(labelTask.getId())), variables);
				if (result.isSuccess()) {
					log.debug("流程已启动,流程ID:" + result.getData().getProcessInstanceId());
					// 返回流程id写入leave
					labelTask.setProcessInstanceId(result.getData().getProcessInstanceId());
					labelTask.setTemplateId(task.getTemplateId());
					labelTask.setTaskId(task.getId());
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

	@Override
	public int completeCount(Long taskId) {
		return baseMapper.completeCount(env, taskId);
	}

//	@Override
//	public Map<Long, Integer> batchQueryCompleteTaskCount(List<Long> taskIds) {
//		List<String> ids = new ArrayList<>();
//		Map<Long, List<LabelTask>> taskIdMap = new LinkedHashMap<>();
//		taskIds.forEach(taskId -> {
//			List<LabelTask> list = list(Wrappers.<LabelTask>query().lambda().eq(LabelTask::getTaskId, taskId));
//			list.forEach(task -> ids.add(task.getProcessInstanceId()));
//			taskIdMap.put(taskId, list);
//		});
//		R processInstancesFinished = flowClient.isProcessInstancesFinished(ids);
//		if (processInstancesFinished.isSuccess()) {
//			LinkedHashMap kv = (LinkedHashMap) processInstancesFinished.getData();
//			Map<Long, Integer> resultMap = new LinkedHashMap<>();
//			taskIdMap.entrySet().stream().forEach(ele -> {
//				AtomicInteger counter = new AtomicInteger(0);
//				List<LabelTask> labelTasks = ele.getValue();
//				labelTasks.forEach(labelTask -> {
//					String processInstanceId = labelTask.getProcessInstanceId();
//					if ((boolean) kv.get(processInstanceId)) {
//						counter.getAndIncrement();
//					}
//				});
//				resultMap.put(ele.getKey(), counter.get());
//			});
//			return resultMap;
//
//		}
//		return null;
//	}

	@Override
	public List<LabelTask> queryCompleteTask(Long taskId) {
		List<LabelTask> list = list(Wrappers.<LabelTask>query().lambda().eq(LabelTask::getTaskId, taskId));
		List<String> ids = new ArrayList<>();
		list.forEach(task -> ids.add(task.getProcessInstanceId()));
		R processInstancesFinished = flowClient.isProcessInstancesFinished(ids);
		List<LabelTask> labelTasks = new ArrayList<>();
		if (processInstancesFinished.isSuccess()) {
			LinkedHashMap kv = (LinkedHashMap)processInstancesFinished.getData();
			list.forEach(labelTask -> {
				String processInstanceId = labelTask.getProcessInstanceId();
				QueryWrapper<QualityInspectionTask> qualityInspectionTaskQueryWrapper = new QueryWrapper<>();
				qualityInspectionTaskQueryWrapper.eq("label_task_id",labelTask.getId());
				List<QualityInspectionTask> list1 = qualityInspectionTaskService.list(qualityInspectionTaskQueryWrapper);
				if ((boolean)kv.get(processInstanceId) && list1.size() == 0) {
//					UpdateWrapper<LabelTask> labelTaskUpdateWrapper = new UpdateWrapper<>();
//					labelTaskUpdateWrapper.eq("process_instance_id",processInstanceId).set("status",2);
//					labelTaskService.update(labelTaskUpdateWrapper);
					labelTasks.add(labelTask);
				}
			});
		}
		return labelTasks;
	}

	@Override
	public List<LabelTask> getByTaskId(Long taskId) {
		List<LabelTask> list = list(Wrappers.<LabelTask>query().lambda().eq(LabelTask::getTaskId, taskId));
		return list;
	}

	@Override
	public long annotationDoneCount(String param2) {
		return baseMapper.annotationDoneCount(env,param2);
	}

	@Override
	public long annotationTodoCount(String param2) {
		return baseMapper.annotationTodoCount(env,param2);
	}

	@Override
	public int annotationClaimCount(List<String> param2) {
		return baseMapper.annotationClaimCount(env,param2);
	}

}
