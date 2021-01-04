package org.springblade.task.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.entity.Task;
import org.springblade.task.mapper.TaskMapper;
import org.springblade.task.service.LabelTaskService;
import org.springblade.task.service.QualityInspectionTaskService;
import org.springblade.task.service.TaskService;
import org.springblade.task.vo.TaskVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
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
}
