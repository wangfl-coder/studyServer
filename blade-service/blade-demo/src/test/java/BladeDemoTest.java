import com.alibaba.fastjson.JSON;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springblade.core.test.BladeBootTest;
import org.springblade.core.test.BladeSpringRunner;
import org.springblade.demo.DemoApplication;
import org.springblade.demo.entity.Blog;
import org.springblade.demo.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@RunWith(BladeSpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
@BladeBootTest(appName = "blade-demo", profile = "dev", enableLoader = false)
public class BladeDemoTest {

	@Autowired
	private BlogService service;

	@Test
	public void save() {
		Blog blog = new Blog();
		System.out.println("hello test");
		blog.setBlogTitle("yangkailun");
		blog.setBlogContent("test save3");

		Assert.assertEquals(service.save(blog), true);
	}

	@Test
	public void get() {
		List<Blog> blogs = service.list();
		for (int i = 0; i < blogs.size(); i++) {
			Blog blog =  blogs.get(i);
			System.out.println(JSON.toJSONString(blog));
		}
	}



}
