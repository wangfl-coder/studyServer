package org.springblade.task.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.common.cache.CacheNames;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.task.dto.ExpertTaskDTO;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.entity.Task;
import org.springblade.task.service.QualityInspectionTaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@NonDS
@RestController
@RequestMapping(value = "/process/quality-inspection-task")
@AllArgsConstructor
@Api(value = "质检任务")
public class QualityInspectionTaskController extends BladeController implements CacheNames {

	private QualityInspectionTaskService service;
//	private IUserClient userClient;

	@GetMapping(value = "/detail")
	@ApiOperation(value = "详情")
	public R<QualityInspectionTask> detail(@RequestParam("businessId") Long businessId) {
		QualityInspectionTask task = service.getById(businessId);
		User user = UserCache.getUser(task.getCreateUser());
//		User user = userClient.userInfoById(detail.getCreateUser()).getData();
//		task.getFlow().setAssigneeName(user.getName());
		return R.data(task);
	}

	@PostMapping("start-process")
	@ApiOperation(value = "新增或修改")
	public R startProcess(String processDefinitionId,Integer count,Task task,Integer inspectionType, List<LabelTask> labelTasks) {
//		 List<Person> persons = getPersons().getData();
		//Task task = Objects.requireNonNull(BeanUtil.copy(expertTaskDTO, Task.class));
		return R.status(service.startProcess(processDefinitionId,count,inspectionType,task,labelTasks));
	}

	@GetMapping("/compositions")
	@ApiOperation(value = "根据id查询任务中所有组合正确率")
	public R<Kv> compositions(Long id) {
		Kv kv = service.compositions(id);
		return R.data(kv);
	}

}
