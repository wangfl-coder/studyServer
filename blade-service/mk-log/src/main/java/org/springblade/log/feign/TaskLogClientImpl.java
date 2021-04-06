package org.springblade.log.feign;


import lombok.AllArgsConstructor;
import org.springblade.log.entity.TaskLog;
import org.springblade.core.tool.api.R;
import org.springblade.log.service.ITaskLogService;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TaskLogClientImpl implements ITaskLogClient {

	private ITaskLogService iTaskLogService;
	@Override
	public R save(TaskLog taskLog) {
		return R.data(iTaskLogService.save(taskLog));
	}
}
