package org.springblade.composition.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.entity.TemplateComposition;
import org.springblade.composition.service.ICompositionService;
import org.springblade.composition.service.ITemplateCompositionService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(value = "composition")
@AllArgsConstructor
@Api(value = "组合接口",tags = "组合")
public class CompositionController extends BladeController {

	private ICompositionService ICompositionService;
	private final ITemplateCompositionService templateCompositionService;

	@PostMapping("/save")
	@ApiOperation(value = "添加组合")
	public R add(@RequestBody Composition composition){
		return R.status(ICompositionService.save(composition));
	}

	@RequestMapping(value = "/detail/{id}" , method = RequestMethod.GET)
	@ApiOperation(value = "根据id查询组合")
	public R<Composition> detail(@PathVariable Long id){
		return R.data(ICompositionService.getById(id));
	}

	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "name", value = "组合名", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "status", value = "组合状态(1:启用的组合,2:停用的组合)", paramType = "query", dataType = "int")
	})
	@ApiOperation(value = "分页查询全部组合")
	public R<IPage<Composition>> list(@RequestParam(value = "name",required = false) String name,@RequestParam(value = "status",required = false) Integer status,Query query) {
		QueryWrapper<Composition> compositionQueryWrapper;
		compositionQueryWrapper = new QueryWrapper<>();
		compositionQueryWrapper.ne("annotation_type",3);
		if(status != null) {
			compositionQueryWrapper.eq("status", status);
		}
		if(name != null) {
			compositionQueryWrapper.like("name", "%"+name+"%").orderByDesc("update_time");
		} else{
			compositionQueryWrapper.orderByDesc("update_time");
		}
		IPage<Composition> pages = ICompositionService.page(Condition.getPage(query), compositionQueryWrapper);
		return R.data(pages);
	}

	@PostMapping(value = "/update")
	@ApiOperation(value = "更新组合")
	public R update(@RequestBody Composition composition){
//		TemplateComposition templateComposition = new TemplateComposition();
//		templateComposition.setCompositionId(composition.getId());
//		if (templateCompositionService.getOne(Condition.getQueryWrapper(templateComposition).last("LIMIT 1")).getId() != null){
//			return R.fail("不能更新组合，已经有模板使用此组合，可以停用");
//		}
		QueryWrapper<TemplateComposition> templateCompositionQueryWrapper = new QueryWrapper<>();
		templateCompositionQueryWrapper.eq("composition_id",composition.getId());
		if (templateCompositionService.list(templateCompositionQueryWrapper).size()!=0){
			return R.fail("不能更新组合，已经有模板使用此组合，可以停用");
		}
		return R.status(ICompositionService.updateById(composition));
	}

	@PostMapping(value = "/disable_or_enable")
	@ApiOperation(value = "停用或启用组合")
	public R disableOrEnable(@RequestBody Composition composition){
		return R.status(ICompositionService.updateById(composition));
	}

	@PostMapping(value = "/remove")
	@ApiOperation(value = "删除组合")
	public R delete(@RequestParam String ids){
		TemplateComposition templateComposition = new TemplateComposition();
		templateComposition.setCompositionId(Func.toLong(ids));
		if (templateCompositionService.getOne(Condition.getQueryWrapper(templateComposition).last("LIMIT 1")).getId() != null){
			return R.fail("不能删除组合，已经有模板使用此组合，可以停用");
		}
		return R.status(ICompositionService.deleteLogic(Func.toLongList(ids)));
	}
}
