package org.springblade.desk.feign;

import org.springblade.adata.entity.Expert;
import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.launch.constant.AppConstant;
import org.springblade.core.tool.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = AppConstant.APPLICATION_DESK_NAME)
public interface ISubTaskClient {

	@PostMapping("feign/start-process")
	R startProcess(@RequestParam(value = "templateId") Long templateId,@RequestBody R<List<Expert>> persons);
}
