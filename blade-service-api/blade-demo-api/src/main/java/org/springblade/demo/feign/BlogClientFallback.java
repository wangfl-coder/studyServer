package org.springblade.demo.feign;

import org.springblade.core.tool.api.R;
import org.springblade.demo.entity.Blog;
import org.springframework.stereotype.Component;


@Component
public class BlogClientFallback implements BlogClient{
	@Override
	public R<Blog> detail(Long id){
//		Blog blog = Blog.builder()
//			.blogTitle("Hystrix")
//			.blogContent("FallBack Success")
//			.isDeleted(0)
//			.build();
		Blog blog = new Blog();
		blog.setBlogTitle("Hystrix");
		blog.setBlogContent("FallBack Success");
		blog.setIsDeleted(0);
		return R.data(blog);
	}
}
