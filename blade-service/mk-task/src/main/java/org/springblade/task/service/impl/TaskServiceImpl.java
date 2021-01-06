package org.springblade.task.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.CharPool;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.task.entity.Field;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.entity.Task;
import org.springblade.task.mapper.TaskMapper;
import org.springblade.task.service.LabelTaskService;
import org.springblade.task.service.QualityInspectionTaskService;
import org.springblade.task.service.TaskService;
import org.springblade.task.vo.TaskVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl extends BaseServiceImpl<TaskMapper, Task> implements TaskService {

	private final QualityInspectionTaskService qualityInspectionTaskService;

	@Value("${spring.profiles.active}")
	public String env;

	@Override
	public TaskVO setCompletedCount(Task task) {
		TaskVO taskVO = Objects.requireNonNull(BeanUtil.copy(task, TaskVO.class));
		if (1 == task.getTaskType()) {
			int count = baseMapper.labelTaskCompleteCount(env, task.getId(), "end");
			taskVO.setCompleted(count);
		}else if (2 == task.getTaskType()){
			int count = baseMapper.qualityInspectionTaskCompleteCount(env, task.getId(), "end");
			taskVO.setCompleted(count);
		}
		return taskVO;
	}

	@Override
	public List<TaskVO> batchSetCompletedCount(List<Task> tasks) {
		List<TaskVO> records = tasks.stream().map(task -> {
			TaskVO taskVO = Objects.requireNonNull(BeanUtil.copy(task, TaskVO.class));
			if (1 == task.getTaskType()) {
				int count = baseMapper.labelTaskCompleteCount(env, task.getId(), "end");
				taskVO.setCompleted(count);
			}else if (2 == task.getTaskType()){
				int count = baseMapper.qualityInspectionTaskCompleteCount(env, task.getId(), "end");
				taskVO.setCompleted(count);
			}
			return taskVO;
		}).collect(Collectors.toList());
		return records;
	}

	@Override
	public TaskVO setCorrectCount(TaskVO task) {
		TaskVO taskVO = Objects.requireNonNull(BeanUtil.copy(task, TaskVO.class));
		if (2 == task.getTaskType()){
			Integer count = qualityInspectionTaskService.correctCount(task.getId());
			taskVO.setCorrect(count);
		}
		return taskVO;
	}

	@Override
	public List<TaskVO> batchSetCorrectCount(List<TaskVO> tasks) {
		List<TaskVO> records = tasks.stream().map(task -> {
			TaskVO taskVO = Objects.requireNonNull(BeanUtil.copy(task, TaskVO.class));
			if (2 == task.getTaskType()){
				Integer count = qualityInspectionTaskService.correctCount(task.getId());
				taskVO.setCorrect(count);
			}
			return taskVO;
		}).collect(Collectors.toList());
		return records;
	}

	@Override
	public Kv compositions(Long id) {
		List<Field> fields = baseMapper.allLabelTaskFields(id);
		Kv total = Kv.create();
		for (Field field : fields) {
			String value = field.getField();
			if (total.containsKey(value)) {
				int count = total.getInt(value);
				count++;
				total.put(value, count);
			} else {
				total.put(value, 1);
			}
		}

		List<Field> wrongFields = baseMapper.allLabelTaskWrongFields(id);
		Kv wrong = Kv.create();
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
					} else {
						wrong.put(key, 1);
					}
					subTaskIds.add(field.getId());
				}
			}
		}
		Kv res = Kv.create();
		res.put("total", total);
		res.put("wrong", wrong);
		return res;
	}
}
