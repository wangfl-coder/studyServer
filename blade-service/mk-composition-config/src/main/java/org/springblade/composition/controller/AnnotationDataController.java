/*
 *      Copyright (c) 2018-2028, Chill Zhuang All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: Chill 庄骞 (smallchill@163.com)
 */
package org.springblade.composition.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.composition.entity.AnnotationData;
import org.springblade.composition.service.IAnnotationDataService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 控制器
 *
 * @author KaiLun
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/annotation_data")
@Api(value = "标注数据", tags = "标注数据")
public class AnnotationDataController extends BladeController {

	private final IAnnotationDataService annotationDataService;
	private final IExpertClient expertClient;

	/**
	 * 查询标注数据
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入AnnotationData")
	public R<List<AnnotationData>> detail(AnnotationData annotationData) {
		List<AnnotationData> annotationDataList = annotationDataService.list(Condition.getQueryWrapper(annotationData));
		return R.data(annotationDataList);
	}

	/**
	 * 分页
	 *
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "sub_task_id", value = "子任务ID", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入AnnotationData")
	public R<IPage<AnnotationData>> list(@ApiIgnore @RequestParam Map<String, Object> annotationData, Query query) {
		IPage<AnnotationData> pages = annotationDataService.page(Condition.getPage(query), Condition.getQueryWrapper(annotationData, AnnotationData.class));
		return R.data(pages);
	}

	/**
	 * 批量新增或修改标注数据
	 * 每次都会逻辑删除之前的数据，但是要有id
	 * 每次修改后同时更新mk_adata_expert表中的数据
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 3)
	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "批量新增或修改", notes = "传入AnnotationData列表")
	public R submit(@Valid @RequestBody List<AnnotationData> annotationDataList) {
		// 删除原来的标注数据
		List<Long> annotationDataIds = new ArrayList<>();
		Expert expert = new Expert();
		expert.setId(annotationDataList.get(0).getExpertId());
		annotationDataList.forEach(annotationData -> {
			annotationDataIds.add(annotationData.getId());
			BeanUtil.setProperty(expert, StringUtil.underlineToHump(annotationData.getField()),annotationData.getValue());
		});
		expertClient.saveExpert(expert);
		annotationDataService.remove(Wrappers.<AnnotationData>update().lambda().in(AnnotationData::getId, annotationDataIds));
		annotationDataList.forEach(annotationData -> {
			annotationData.setId(null);
		});
		return R.status(annotationDataService.saveBatch(annotationDataList));
	}


}