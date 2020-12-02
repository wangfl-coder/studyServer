package org.springblade.demo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.annotation.PreAuth;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.demo.entity.Blog;
import org.springblade.demo.service.BlogService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

/**
 * DemoController
 *
 * @author KaiLun
 */
@Slf4j
@RestController
@RequestMapping("api")
@AllArgsConstructor

public class DemoController {

	private BlogService service;


//	blog.setBlogTitle("yangkailun");

	@GetMapping("info")
//	@PreAuth("hasRole('administrator')")
	@PreAuth("permitAll()")
	@Cacheable(cacheNames = "demo-info", key = "#name")
	public R<String> info(String name){
		log.info("本条信息没有从缓存获取");
		return R.data("Hello, My Name Is: " + name);
	}

	@GetMapping("remove-info")
	@PreAuth("permitAll()")
	@CacheEvict(cacheNames = "demo-info", key = "#name")
	public R<String> removeInfo(String name){
		return R.success("删除缓存成功");
	}

	@GetMapping("count")
	@PreAuth("permitAll()")
	public R<Integer> count(Integer cnt){
		return R.data(cnt * 10);
	}

	@PostMapping("/save")
	public R save(@RequestBody Blog blog){
		return R.status(service.save(blog));
	}

	@PostMapping("update")
	public R update(@RequestBody Blog blog){
		return R.status(service.updateById(blog));
	}

	/**
	 * 删除
	 * @param ids
	 * @return
	 */
	@PostMapping("/remove")
	public R remove(@RequestParam String ids){
		return R.status(service.removeByIds(Func.toLongList(ids)));
	}

	/**
	 * 详情
	 * @param id
	 * @return
	 */
	@GetMapping("/detail")
	public R<Blog> detail(Long id){
		Blog detail = service.getById(id);
		return R.data(detail);
	}

	/**
	 * 查询多条,模糊查询
	 * @return
	 */
//	@GetMapping("/list")
//	public R<List<Blog>> list(@RequestParam Map<String, Object> blog){
//		List<Blog> list = service.list(Condition.getQueryWrapper(blog,Blog.class)); //这个地方文档中没有Blog.class，是一个bug
//		return R.data(list);
//	}

//	@GetMapping("/list")
//	public R<List<Blog>> list(Blog blog){
//		List<Blog> list = service.list(Wrappers.query(blog)); //这个地方目前有编译错误
//		return R.data(list);
//	}

	@GetMapping("/list")
	public R<List<Blog>> list(@RequestParam Map<String, Object> blog){
		List<Blog> list = service.list(Condition.getQueryWrapper(blog,Blog.class).lambda().orderByDesc(Blog::getBlogDate));
		return R.data(list);
	}

	@GetMapping("/page")
	public R<IPage<Blog>> page(@ApiIgnore @RequestParam Map<String, Object> blog, Query query){
		IPage<Blog> pages = service.page(Condition.getPage(query), Condition.getQueryWrapper(blog, Blog.class));
		return R.data(pages);
	}

}

