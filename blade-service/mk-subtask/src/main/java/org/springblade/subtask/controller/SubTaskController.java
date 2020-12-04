package org.springblade.subtask.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.subtask.entity.SubTask;
import org.springblade.subtask.service.SubTaskService;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "subtask")
@Api(value = "子任务接口")
public class SubTaskController {

	private SubTaskService subTaskService;

	@PostMapping(value = "/save")
	@ApiOperation(value = "添加子任务")
	public R save(@RequestBody SubTask subTask){
		boolean save = subTaskService.save(subTask);
		return R.status(save);
	}

	@RequestMapping(value = "/detail/{id}" , method = RequestMethod.GET)
	@ApiOperation(value = "根据id查询子任务")
	public R<SubTask> detail(@PathVariable Long id){
		return R.data(subTaskService.getById(id));
	}

	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "taskName", value = "查询条件", paramType = "query", dataType = "string")
	})
	@ApiOperation(value = "分页查询全部子任务")
	public R<IPage<SubTask>> list(@RequestParam(value = "targetId",required = false) String targetId, Query query) {
		QueryWrapper<SubTask> compositionQueryWrapper = new QueryWrapper<>();
		if(targetId != null) {
			compositionQueryWrapper.like("target_id", "%"+targetId+"%").orderByDesc("create_time");
		} else{
			compositionQueryWrapper.orderByDesc("create_time");
		}
		IPage<SubTask> pages = subTaskService.page(Condition.getPage(query), compositionQueryWrapper);
		return R.data(pages);
	}

	@PostMapping(value = "/update")
	@ApiOperation(value = "更新子任务")
	public R update(@RequestBody SubTask subTask){
		return R.status(subTaskService.updateById(subTask));
	}

	@PostMapping(value = "/remove")
	@ApiOperation(value = "删除子任务")
	public R delete(@RequestParam String ids){
		return R.status(subTaskService.deleteLogic(Func.toLongList(ids)));
	}
}
