package org.springblade.log.feign;
import org.springblade.common.constant.CommonConstant;
import org.springblade.log.entity.TaskLog;
import org.springblade.core.tool.api.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
	value = CommonConstant.MKAPP_LOG_NAME
)
public interface ITaskLogClient {

	String API_PREFIX = "/log";
	/**
	 * 获取详情
	 *
	 * @param
	 * @return
	 */
	@PostMapping(API_PREFIX + "/save")
	R save(@RequestBody TaskLog taskLog);

}
