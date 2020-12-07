package org.springblade.task.service.impl;

import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.task.entity.SubTask;
import org.springblade.task.mapper.SubTaskMapper;
import org.springblade.task.service.SubTaskService;
import org.springframework.stereotype.Service;

@Service
public class SubTaskServiceImpl extends BaseServiceImpl<SubTaskMapper, SubTask> implements SubTaskService {
}
