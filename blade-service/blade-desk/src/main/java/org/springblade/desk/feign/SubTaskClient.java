package org.springblade.desk.feign;

import lombok.AllArgsConstructor;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.desk.service.SubTaskService;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@NonDS
@ApiIgnore()
@RestController
@AllArgsConstructor
public class SubTaskClient implements ISubTaskClient{

	private SubTaskService subTaskService;
	@Override
	public R startProcess(Long templateId, Long compositionId) {
		boolean b = subTaskService.startProcess(templateId, compositionId);
		return R.status(b);
	}
}
