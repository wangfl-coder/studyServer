package org.springblade.taskLog.feign;

import lombok.AllArgsConstructor;
import org.springblade.core.tool.api.R;
import org.springblade.taskLog.entity.TaskLog;
import org.springblade.taskLog.service.ITaskLogService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TaskLogClientImpl implements ITaskLogClient{
	private final ITaskLogService iTaskLogService;
//	@Override
//	//@PostMapping(API_PREFIX+"detail")
//	public Boolean detail(TaskLog taskLog){
//		Boolean save = logService.save(taskLog);
//		return save;
//	}

	@Override
	public boolean save(TaskLog taskLog) {
		boolean saves =iTaskLogService.save(taskLog);
		return saves;
	}
}
