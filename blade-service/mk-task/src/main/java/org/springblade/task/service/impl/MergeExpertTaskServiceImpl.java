package org.springblade.task.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springblade.task.entity.*;
import org.springblade.task.mapper.MergeExpertTaskMapper;
import org.springblade.task.service.MergeExpertTaskService;
import org.springblade.task.vo.ExpertQualityInspectionTaskVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MergeExpertTaskServiceImpl extends BaseServiceImpl<MergeExpertTaskMapper, MergeExpertTask> implements MergeExpertTaskService {

	private final IFlowClient flowClient;


	@Override
	@Transactional(rollbackFor = Exception.class)
	// @GlobalTransactional
	public boolean startProcess(Integer mergeType,Task task, List<MergeExpertTask> mergeExpertTasks) {
		String businessTable = FlowUtil.getBusinessTable(ProcessConstant.MERGE_EXPERT_KEY);

		mergeExpertTasks.forEach( mergeExpertTask -> {
			// 启动流程
			Kv variables = Kv.create()
				.set(ProcessConstant.TASK_VARIABLE_CREATE_USER, AuthUtil.getUserName())
				.set("taskUser", TaskUtil.getTaskUser(mergeExpertTask.getTaskUser()))
				.set("type", mergeType)
				.set("priority", task.getPriority());
			R<BladeFlow> result = flowClient.startProcessInstanceById(mergeExpertTask.getProcessDefinitionId(), FlowUtil.getBusinessKey(businessTable, String.valueOf(mergeExpertTask.getId())), variables);
			if (result.isSuccess()) {
				log.debug("流程已启动,流程ID:" + result.getData().getProcessInstanceId());
				// 返回流程id写入leave
				mergeExpertTask.setProcessInstanceId(result.getData().getProcessInstanceId());
				mergeExpertTask.setMergeTaskId(task.getId());
				mergeExpertTask.setTaskType(task.getTaskType());
				mergeExpertTask.setMergeType(mergeType);
				updateById(mergeExpertTask);
			} else {
				throw new ServiceException("开启流程失败");
			}
		});
		return true;
	}

	@Override
	public int completeCount(Long taskId) {
		return baseMapper.completeCount(taskId);
	}

	@Override
	public int correctCount(Long taskId) {
		return baseMapper.selectCount(Wrappers.<MergeExpertTask>query().lambda().eq(MergeExpertTask::getMergeTaskId, taskId).eq(MergeExpertTask::getStatus, 2));
	}

	@Override
	public List<ExpertQualityInspectionTaskVO> personIdToProcessInstance(String expertId) {
		return baseMapper.personIdToProcessInstance(expertId);
	}

	@Override
	public Kv compositions(Long id) {
		List<Field> fields = baseMapper.allLabelTaskFields(id);
//		List<Field> fields = new ArrayList<>(10);
		Kv total = Kv.create();
		Kv totalRes = Kv.create();
		for (Field field : fields) {
			String value = field.getField();
			if (total.containsKey(value)) {
				int count = total.getInt(value);
				count++;
				total.put(value, count);
				totalRes.put(field.getName(), count);
			} else {
				total.put(value, 1);
				totalRes.put(field.getName(), 1);
			}
		}

		List<Field> wrongFields = baseMapper.allLabelTaskWrongFields(id);
		Kv wrong = Kv.create();
		Kv wrongRes = Kv.create();
		for (String key : total.keySet()) {
			ArrayList<Long> subTaskIds = new ArrayList<>();
			for (Field field : wrongFields) {
				if (key.indexOf(field.getField()) >= 0) {
					if (subTaskIds.contains(field.getId()))
						continue;
					if (wrong.containsKey(key)) {
						int count = wrong.getInt(key);
						count++;
						wrong.put(key, count);
						for (Field f: fields) {
							if(f.getField().equals(key))
								wrongRes.put(f.getName(), count);
						}
					} else {
						wrong.put(key, 1);
						for (Field f: fields) {
							if(f.getField().equals(key))
								wrongRes.put(f.getName(), 1);
						}
					}
					subTaskIds.add(field.getId());
				}
			}
		}
		Kv res = Kv.create();
		res.put("total", totalRes);
		res.put("wrong", wrongRes);
		return res;
	}
}
