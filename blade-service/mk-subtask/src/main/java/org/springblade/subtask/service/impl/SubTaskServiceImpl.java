package org.springblade.subtask.service.impl;

import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.subtask.entity.SubTask;
import org.springblade.subtask.mapper.SubTaskMapper;
import org.springblade.subtask.service.SubTaskService;
import org.springframework.stereotype.Service;

@Service
public class SubTaskServiceImpl extends BaseServiceImpl<SubTaskMapper, SubTask> implements SubTaskService {
}
