package org.springblade.task.service.impl;

import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.task.entity.Task;
import org.springblade.task.mapper.TaskMapper;
import org.springblade.task.service.TaskService;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl extends BaseServiceImpl<TaskMapper, Task> implements TaskService {
}
