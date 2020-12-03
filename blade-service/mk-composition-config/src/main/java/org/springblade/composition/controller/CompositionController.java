package org.springblade.composition.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.service.CompositionService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "composition")
@AllArgsConstructor
@Api(value = "组合接口")
public class CompositionController extends BladeController {

	private CompositionService compositionService;

	@PostMapping("/save")
	@ApiOperation(value = "添加组合")
	public R add(@RequestBody Composition composition){
		return R.status(compositionService.save(composition));
	}

	@RequestMapping(value = "/detail/{id}" , method = RequestMethod.GET)
	@ApiOperation(value = "根据id查询组合")
	public R<Composition> detail(@PathVariable Long id){
		return R.data(compositionService.getById(id));
	}

	@GetMapping("/list")
	@ApiOperation(value = "查询全部组合")
	public R<List<Composition>> list(@RequestParam(value = "name",required = false) String name){
		List<Composition> list;
		if(name != null){
			QueryWrapper<Composition> compositionQueryWrapper = new QueryWrapper<>();
			compositionQueryWrapper.eq("name",name);
			list = compositionService.list(compositionQueryWrapper);
		}else {
			list = compositionService.list();
		}
		return R.data(list);
	}

	@PostMapping(value = "/update")
	@ApiOperation(value = "更新组合")
	public R update(@RequestBody Composition composition){
		return R.status(compositionService.updateById(composition));
	}

	@PostMapping(value = "/remove")
	@ApiOperation(value = "删除组合")
	public R delete(@RequestParam Long id){
		return R.status(compositionService.removeById(id));
	}
}
