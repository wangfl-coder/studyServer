package org.springblade.desk.service;

import org.springblade.adata.entity.Expert;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.tool.api.R;
import org.springblade.desk.entity.SubTask;

import java.util.List;

public interface SubTaskService extends BaseService<SubTask> {
	boolean startProcess(Long templateId, R<List<Expert>> persons);
}
