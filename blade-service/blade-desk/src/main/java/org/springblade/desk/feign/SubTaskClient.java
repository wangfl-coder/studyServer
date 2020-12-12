package org.springblade.desk.feign;

import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.desk.service.SubTaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@NonDS
@ApiIgnore()
@RestController
@AllArgsConstructor
public class SubTaskClient implements ISubTaskClient{

	private SubTaskService subTaskService;
	@Override
	@PostMapping("feign/start-process")
	public R startProcess(@RequestParam("templateId") Long templateId,@RequestBody List<Long> ids) {
		boolean b = subTaskService.startProcess(templateId,ids);
		return R.status(b);
	}
}
