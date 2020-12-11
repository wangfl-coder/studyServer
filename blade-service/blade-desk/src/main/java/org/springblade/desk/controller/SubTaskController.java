package org.springblade.desk.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.common.cache.CacheNames;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.desk.entity.SubTask;
import org.springblade.desk.service.SubTaskService;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@NonDS
@RestController
@RequestMapping(value = "process/subtask")
@AllArgsConstructor
@Api(value = "任务")
public class SubTaskController extends BladeController implements CacheNames {

	private SubTaskService taskService;
//	private IUserClient userClient;

	@GetMapping(value = "/detail")
	@ApiOperation(value = "详情")
	public R<SubTask> detail(@RequestParam("businessId") Long businessId) {
		SubTask detail = taskService.getById(businessId);
		User user = UserCache.getUser(detail.getCreateUser());
//		User user = userClient.userInfoById(detail.getCreateUser()).getData();
		detail.getFlow().setAssigneeName(user.getName());
		return R.data(detail);
	}

	@PostMapping("start-process")
	@ApiOperation(value = "新增或修改")
	public R startProcess(@RequestParam(value = "templateId") Long templateId,@RequestBody R<List<Expert>> persons) {
//		 List<Person> persons = getPersons().getData();

		return R.status(taskService.startProcess(templateId,persons));
	}

}
