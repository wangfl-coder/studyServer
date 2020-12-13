package org.springblade.task.feign;

import lombok.AllArgsConstructor;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.task.dto.ExpertTaskDTO;
import org.springblade.task.entity.Task;
import org.springblade.task.service.LabelTaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Objects;

@NonDS
@ApiIgnore()
@RestController
@AllArgsConstructor
public class LabelTaskClient implements ILabelTaskClient {

	private LabelTaskService labelTaskService;
	@Override
	@PostMapping(START_LABEL_PROCESS)
	public R startProcess(@RequestBody ExpertTaskDTO expertTaskDTO) {
		Task task = Objects.requireNonNull(BeanUtil.copy(expertTaskDTO, Task.class));
		boolean b = labelTaskService.startProcess(expertTaskDTO.getProcessDefinitionId(), task, expertTaskDTO.getExpertIds());
		return R.status(b);
	}
}
