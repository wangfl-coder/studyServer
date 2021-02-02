package org.springblade.task.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.RealSetExpert;
import org.springblade.adata.feign.IRealSetExpertClient;
import org.springblade.composition.entity.Composition;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.Holder;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.task.vo.CompositionClaimCountVO;
import org.springblade.task.vo.ExpertLabelTaskVO;
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
import org.springblade.task.vo.RoleClaimCountVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class LabelTaskServiceImpl extends BaseServiceImpl<LabelTaskMapper, LabelTask> implements LabelTaskService {

	private final IFlowClient flowClient;
	private final IRealSetExpertClient realSetExpertClient;
	private final QualityInspectionTaskService qualityInspectionTaskService;

	@Value("${spring.profiles.active}")
	public String env;

	@Override
	@Transactional(rollbackFor = Exception.class)
	// @GlobalTransactional
	public boolean startProcess(String processDefinitionId,
								Task task,
								List<Expert> experts) {
		String businessTable = FlowUtil.getBusinessTable(ProcessConstant.LABEL_KEY);
		//List<Expert> experts = persons.getData();
		experts.forEach(expert -> {
			LabelTask labelTask = new LabelTask();
			labelTask.setProcessDefinitionId(processDefinitionId);
			// 保存任务
			labelTask.setCreateTime(DateUtil.now());
			boolean save = save(labelTask);
			Kv variables = createProcessVariables(task, labelTask);
			variables.put("isRealSet", false);
			R<BladeFlow> result = flowClient.startProcessInstanceById(labelTask.getProcessDefinitionId(), FlowUtil.getBusinessKey(businessTable, String.valueOf(labelTask.getId())), variables);
			if (result.isSuccess()) {
				log.debug("流程已启动,流程ID:" + result.getData().getProcessInstanceId());
				// 返回流程id写入任务
				labelTask.setProcessInstanceId(result.getData().getProcessInstanceId());
				labelTask.setTemplateId(task.getTemplateId());
				labelTask.setTaskId(task.getId());
				labelTask.setPersonId(expert.getId());
				labelTask.setPersonName(expert.getName());
				updateById(labelTask);
			} else {
				throw new ServiceException("开启流程失败");
			}
		});
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	// @GlobalTransactional
	public boolean startRealSetProcess(String realSetProcessDefinitions,
								Task task) {
		R<List<RealSetExpert>> expertsRealSetResult = realSetExpertClient.getExpertIds(task.getId());
		if (!expertsRealSetResult.isSuccess()) {
			return false;
		}
		List<RealSetExpert> expertsRealSet = expertsRealSetResult.getData();
		Random random = Holder.RANDOM;
		RealSetExpert expert = expertsRealSet.get(random.nextInt(expertsRealSet.size()));
		String businessTable = FlowUtil.getBusinessTable(ProcessConstant.LABEL_KEY);

		JSONObject realSetJSONObject = JSONObject.parseObject(realSetProcessDefinitions);
		realSetJSONObject.entrySet().forEach(entry -> {
			LabelTask labelTask = new LabelTask();
			labelTask.setProcessDefinitionId((String)entry.getValue());
			// 保存任务
			labelTask.setCreateTime(DateUtil.now());
			boolean save = save(labelTask);
			Kv variables = createProcessVariables(task, labelTask);
			variables.put("isRealSet", true);
			R<BladeFlow> result = flowClient.startProcessInstanceById((String)entry.getValue(), FlowUtil.getBusinessKey(businessTable, String.valueOf(labelTask.getId())), variables);
			if (result.isSuccess()) {
				log.debug("流程已启动,流程ID:" + result.getData().getProcessInstanceId());
				// 返回流程id写入任务
				labelTask.setProcessInstanceId(result.getData().getProcessInstanceId());
				labelTask.setTemplateId(task.getTemplateId());
				labelTask.setTaskId(task.getId());
				labelTask.setPersonId(expert.getId());
				labelTask.setPersonName(expert.getName());
				updateById(labelTask);
			} else {
				throw new ServiceException("开启流程失败");
			}
		});

		return true;
	}

	@Override
	public int completeCount(Long taskId) {
		return baseMapper.completeCount(env, taskId);
	}

	@Override
	public List<ExpertLabelTaskVO> personIdToProcessInstance(String expertId) {
		return baseMapper.personIdToProcessInstance(expertId);
	}

	/**
	 * 创建不去重质检任务查询完成的标注任务
	 * @param taskId  标注大任务id
	 * @return
	 */
	@Override
	public List<LabelTask> queryCompleteTask(Long taskId) {
		return baseMapper.queryCompleteTask(env, taskId);
	}

	/**
	 * 创建去重质检任务查询完成的标注任务
	 * @param taskId  标注大任务id
	 * @return
	 */
	@Override
	public List<LabelTask> queryUniqueCompleteTask(Long taskId) {
		List<LabelTask> labelTasks = baseMapper.queryCompleteTask(env, taskId);
		List<LabelTask> uniqueCompleteTasks = new ArrayList<>();
		for(LabelTask labelTask:labelTasks){
			QueryWrapper<QualityInspectionTask> qualityInspectionTaskQueryWrapper = new QueryWrapper<>();
			qualityInspectionTaskQueryWrapper.eq("label_task_id",labelTask.getId());
			List<QualityInspectionTask> qualityInspectionTasks = qualityInspectionTaskService.list(qualityInspectionTaskQueryWrapper);
			if(qualityInspectionTasks.size()==0){
				uniqueCompleteTasks.add(labelTask);
			}
		}
		return uniqueCompleteTasks;
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

//	/**
//	 * 创建不去重质检任务查询完成的标注任务
//	 * @param taskId  标注大任务id
//	 * @return
//	 */
//	@Override
//	public List<LabelTask> queryCompleteTask(Long taskId) {
//		List<LabelTask> list = list(Wrappers.<LabelTask>query().lambda().eq(LabelTask::getTaskId, taskId));
//		List<String> ids = new ArrayList<>();
//		list.forEach(task -> ids.add(task.getProcessInstanceId()));
//		R processInstancesFinished = flowClient.isProcessInstancesFinished(ids);
//		List<LabelTask> labelTasks = new ArrayList<>();
//		if (processInstancesFinished.isSuccess()) {
//			LinkedHashMap kv = (LinkedHashMap)processInstancesFinished.getData();
//			list.forEach(labelTask -> {
//				String processInstanceId = labelTask.getProcessInstanceId();
//				if ((boolean)kv.get(processInstanceId)) {
////					UpdateWrapper<LabelTask> labelTaskUpdateWrapper = new UpdateWrapper<>();
////					labelTaskUpdateWrapper.eq("process_instance_id",processInstanceId).set("status",2);
////					labelTaskService.update(labelTaskUpdateWrapper);
//					labelTasks.add(labelTask);
//				}
//			});
//		}
//		return labelTasks;
//	}


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
	public int annotationClaimCount(List<String> roleAlias) {
		return baseMapper.annotationClaimCount2(env, roleAlias, AuthUtil.getUserId());
	}

	@Override
	public List<RoleClaimCountVO> roleClaimCount(List<String> roleAlias) {
		return baseMapper.roleClaimCount(env, roleAlias, AuthUtil.getUserId());
	}

	@Override
	public List<CompositionClaimCountVO> compositionClaimCount(List<String> roleAlias) {
		return baseMapper.compositionClaimCount(env, roleAlias, AuthUtil.getUserId());
	}

	private Kv createProcessVariables(Task task, LabelTask labelTask) {
		Kv variables = Kv.create();
		List<Composition> compositions = baseMapper.allCompositions(task.getTemplateId());
		for(Composition composition : compositions) {
			if (2 == composition.getAnnotationType()) {
				variables.set("biCounter"+composition.getId(), 0)
					.set("biSame"+composition.getId(), 0)
					.set("biNotfound"+composition.getId(), 0);
			}
		}
		variables.set("isEduWorkEasy", false)
			.set("isEduWorkNeedInspect", false)
			.set("isBioNeedInspect", false);
		// 启动流程
		variables.set(ProcessConstant.TASK_VARIABLE_CREATE_USER, AuthUtil.getUserName())
			.set("taskUser", null)
			.set("priority", task.getPriority());
		return variables;
	}
}
