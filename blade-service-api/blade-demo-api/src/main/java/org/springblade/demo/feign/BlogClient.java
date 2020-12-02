package org.springblade.demo.feign;

import org.springblade.core.launch.constant.AppConstant;
import org.springblade.core.tool.api.R;
import org.springblade.demo.entity.Blog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
	//定义Feign指向的service-id
	value = AppConstant.APPLICATION_DEMO_NAME,
	//定义hystrix配置类
	fallback = BlogClientFallback.class
)
public interface BlogClient {

	/**
	 * 接口前缀
	 */
	String API_PREFIX = "/api/blog";

	/**
	 * 获取详情
	 */
	@GetMapping(API_PREFIX + "/detail")
	R<Blog> detail(@RequestParam("id") Long id);

}
