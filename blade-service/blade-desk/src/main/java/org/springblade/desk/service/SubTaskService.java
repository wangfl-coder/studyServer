package org.springblade.desk.service;

import org.springblade.core.mp.base.BaseService;
import org.springblade.desk.entity.SubTask;

import java.util.List;

public interface SubTaskService extends BaseService<SubTask> {
	boolean startProcess(Long templateId, Long compositionId);
}
