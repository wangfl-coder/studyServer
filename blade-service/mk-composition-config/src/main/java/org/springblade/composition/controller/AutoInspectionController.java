package org.springblade.composition.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.composition.dto.AutoInspectionDTO;
import org.springblade.composition.mapper.AutoInspectionMapper;
import org.springblade.composition.service.IAutoInspectionService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.tool.api.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/real-set")
@Api(value = "真题集统计", tags = "真题集统计")
@AllArgsConstructor
public class AutoInspectionController extends BladeController {

	private AutoInspectionMapper autoInspectionMapper;

	/**
	 * 真题集总数统计
	 */
	@GetMapping("/total")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "真题集总数统计", notes = "真题集总数统计")
	public R<List<AutoInspectionDTO>> realSetCount(String startTime, String endTime, Long taskId, Long userId) {
		return R.data(autoInspectionMapper.realSetCount(startTime,endTime,taskId,userId));
	}

	/**
	 * 真题集正确错误统计
	 */
	@GetMapping("/correct-or-error-count")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "真题集正确错误数量统计", notes = "真题集正确错误数量统计")
	public R<List<AutoInspectionDTO>> realSetCorrectOrErrorCount(Integer isCompositionTrue,String startTime, String endTime, Long taskId, Long userId) {
		return R.data(autoInspectionMapper.realSetCorrectOrErrorCount(isCompositionTrue,startTime,endTime,taskId,userId));
	}
}
