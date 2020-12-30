package org.springblade.composition.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.service.ICompositionService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping(value = "composition")
@AllArgsConstructor
@Api(value = "组合接口")
public class CompositionController extends BladeController {

	private ICompositionService ICompositionService;

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
		@ApiImplicitParam(name = "name", value = "查询条件", paramType = "query", dataType = "string")
	})
	@ApiOperation(value = "分页查询全部组合")
	public R<IPage<Composition>> list(@RequestParam(value = "name",required = false) String name,Query query) {
		QueryWrapper<Composition> compositionQueryWrapper;
		compositionQueryWrapper = new QueryWrapper<>();
		if(name != null) {
			compositionQueryWrapper.like("name", "%"+name+"%").orderByDesc("create_time");
		} else{
			compositionQueryWrapper.orderByDesc("create_time");
		}
		IPage<Composition> pages = ICompositionService.page(Condition.getPage(query), compositionQueryWrapper);
		return R.data(pages);
	}

	@PostMapping(value = "/update")
	@ApiOperation(value = "更新组合")
	public R update(@RequestBody Composition composition){
		return R.status(ICompositionService.updateById(composition));
	}

	@PostMapping(value = "/remove")
	@ApiOperation(value = "删除组合")
	public R delete(@RequestParam String ids){
		return R.status(ICompositionService.deleteLogic(Func.toLongList(ids)));
	}
}
