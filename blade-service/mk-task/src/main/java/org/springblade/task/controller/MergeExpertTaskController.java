package org.springblade.task.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.ExpertOrigin;
import org.springblade.adata.entity.RealSetExpert;
import org.springblade.adata.vo.ExpertVO;
import org.springblade.adata.vo.RealSetExpertVO;
import org.springblade.common.cache.CacheNames;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.Func;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.MergeExpertTask;
import org.springblade.task.entity.QualityInspectionTask;
import org.springblade.task.entity.Task;
import org.springblade.task.service.MergeExpertTaskService;
import org.springblade.task.service.QualityInspectionTaskService;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;


@NonDS
@RestController
@RequestMapping(value = "/process/merge-expert-task")
@AllArgsConstructor
@Api(value = "合并专家任务")
public class MergeExpertTaskController extends BladeController implements CacheNames {

	private MergeExpertTaskService mergeService;
//	private IUserClient userClient;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入task")
	public R<MergeExpertTask> detail(@RequestParam("businessId") Long businessId) {
		MergeExpertTask detail = mergeService.getById(businessId);
		return R.data(detail);
	}

	/**
	 * 分页
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "taskId", value = "任务id", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "status", value = "子任务状态", paramType = "query", dataType = "integer")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页查询列表", notes = "传入param")
	public R<IPage<MergeExpertTask>> list(@ApiIgnore @RequestParam(required = false) Map<String, Object> param, Query query) {
		IPage<MergeExpertTask> pages = mergeService.page(Condition.getPage(query), Condition.getQueryWrapper(param, MergeExpertTask.class).orderByDesc("update_time"));
		return R.data(pages);
	}

//	/**
//	 * 多表联合查询自定义分页
//	 */
//	@GetMapping("/page")
//	@ApiImplicitParams({
//		@ApiImplicitParam(name = "category", value = "公告类型", paramType = "query", dataType = "integer"),
//		@ApiImplicitParam(name = "title", value = "公告标题", paramType = "query", dataType = "string")
//	})
//	@ApiOperationSupport(order = 3)
//	@ApiOperation(value = "分页", notes = "传入expert")
//	public R<IPage<ExpertVO>> page(@ApiIgnore ExpertVO expert, Query query) {
//		IPage<ExpertVO> pages = expertService.selectNoticePage(Condition.getPage(query), expert);
//		return R.data(pages);
//	}

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入expert")
	public R save(@RequestBody MergeExpertTask task) {
		return R.status(mergeService.save(task));
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "修改", notes = "传入expert")
	public R update(@RequestBody MergeExpertTask task) {
		return R.status(mergeService.updateById(task));
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "新增或修改", notes = "传入expert")
	public R submit(@RequestBody MergeExpertTask task) {
		return R.status(mergeService.saveOrUpdate(task));
	}

	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "逻辑删除", notes = "传入expert")
	public R remove(@ApiParam(value = "主键集合") @RequestParam String ids) {
		boolean temp = mergeService.deleteLogic(Func.toLongList(ids));
		return R.status(temp);
	}

}
