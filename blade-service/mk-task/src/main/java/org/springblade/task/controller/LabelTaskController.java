package org.springblade.task.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.common.cache.CacheNames;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.task.dto.ExpertBaseTaskDTO;
import org.springblade.task.dto.ExpertTaskDTO;
import org.springblade.task.entity.LabelTask;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.task.entity.Task;
import org.springblade.task.service.LabelTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@NonDS
@RestController
@RequestMapping(value = "/process/label-task")
@AllArgsConstructor
@Api(value = "任务")
public class LabelTaskController extends BladeController implements CacheNames {

	private LabelTaskService labelTaskService;
//	private IUserClient userClient;

	@GetMapping(value = "/detail")
	@ApiOperation(value = "详情")
	public R<LabelTask> detail(@RequestParam("businessId") Long businessId) {
		LabelTask detail = labelTaskService.getById(businessId);
		User user = UserCache.getUser(detail.getCreateUser());
//		User user = userClient.userInfoById(detail.getCreateUser()).getData();
		detail.getFlow().setAssigneeName(user.getName());
		return R.data(detail);
	}

	@PostMapping("start-process")
	@ApiOperation(value = "新增或修改")
	public R startProcess(@RequestBody ExpertTaskDTO expertTaskDTO) {
//		 List<Person> persons = getPersons().getData();
		Task task = Objects.requireNonNull(BeanUtil.copy(expertTaskDTO, Task.class));
		return R.status(labelTaskService.startProcess(expertTaskDTO.getProcessDefinitionId(), task, expertTaskDTO.getExperts()));
	}

}
