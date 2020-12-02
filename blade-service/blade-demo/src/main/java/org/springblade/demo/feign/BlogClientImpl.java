package org.springblade.demo.feign;

import lombok.AllArgsConstructor;
import org.springblade.core.tool.api.R;
import org.springblade.demo.entity.Blog;
import org.springblade.demo.service.BlogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class BlogClientImpl implements BlogClient{
	private BlogService service;

	@Override
	@GetMapping(API_PREFIX + "/detail")
	public R<Blog> detail(Long id){
		int cnt = 100 / 0; //模拟服务调用异常
		return R.data(service.getById(id));
	}
}
