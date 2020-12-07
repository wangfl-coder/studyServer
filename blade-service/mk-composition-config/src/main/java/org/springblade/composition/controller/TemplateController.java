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
import org.springblade.composition.entity.Template;
import org.springblade.composition.entity.TemplateComposition;
import org.springblade.composition.service.ITemplateCompositionService;
import org.springblade.composition.service.ITemplateService;
import org.springblade.composition.vo.TemplateVO;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;


import org.springblade.core.tool.utils.BeanUtil;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * 控制器
 *
 * @author KaiLun
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/template")
@Api(value = "模板", tags = "模板")
public class TemplateController extends BladeController {

	private final ITemplateService templateService;
	private final ITemplateCompositionService templateCompositionService;

	/**
	 * 模板详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入template")
	public R<Template> detail(Template template) {
		Template detail = templateService.getOne(Condition.getQueryWrapper(template));
		return R.data(detail);
	}

	/**
	 * 查询模板的所有组合
	 */
	@GetMapping("/compositions")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "模板的所有组合", notes = "传入template")
	public R<TemplateVO> compositions(Template template) {
		Template detail = templateService.getOne(Condition.getQueryWrapper(template));
		TemplateVO templateVO = Objects.requireNonNull(BeanUtil.copy(detail, TemplateVO.class));
		List<TemplateComposition> templateCompositions = templateCompositionService.list(Wrappers.<TemplateComposition>query().lambda().in(TemplateComposition::getTemplateId, template.getId()));
		templateVO.setTemplateCompositions(templateCompositions);
		return R.data(templateVO);
	}

	/**
	 * 分页
	 *
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "templateName", value = "模板名", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入template")
	public R<IPage<Template>> list(@ApiIgnore @RequestParam Map<String, Object> template, Query query) {
		IPage<Template> pages = templateService.page(Condition.getPage(query), Condition.getQueryWrapper(template, Template.class));
		return R.data(pages);
	}

	/**
	 * 新增或修改模板
	 * 新增一个没有组合的新模板，修改名字或者备注
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "新增或修改", notes = "传入template")
	public R submit(@Valid @RequestBody Template template) {
		return R.status(templateService.saveOrUpdate(template));
	}

	/**
	 * 新增或修改模板
	 * 同时构建组合
	 */
	@PostMapping("/create")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "新增或修改模板，同时构建组合", notes = "传入templateVO")
	public R submit(@Valid @RequestBody TemplateVO templateVO) {
		Template template = templateVO;
		boolean tmp = templateService.saveOrUpdate(template);
		if(tmp) {
			List<TemplateComposition> templateCompositions = templateVO.getTemplateCompositions();
			templateCompositions.forEach(templateComposition -> templateComposition.setTemplateId(template.getId()));
			return R.status(templateService.compose(templateCompositions));
		} else {
			R.fail("新建模板失败");
		}
		return R.status(templateService.saveOrUpdate(template));
	}


	/**
	 * 删除模板
	 * 相应的组合也删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		return R.status(templateService.remove(ids));
	}

	/**
	 * 构建组合
	 */
	@PostMapping("/compose")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "构建组合", notes = "传入templateComposition集合")
	public R compose(@RequestBody List<TemplateComposition> templateCompositions) {
		boolean temp = templateService.compose(templateCompositions);
		return R.status(temp);
	}

}
