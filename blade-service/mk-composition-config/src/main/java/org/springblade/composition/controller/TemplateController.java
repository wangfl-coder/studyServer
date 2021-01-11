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
import org.springblade.composition.dto.TemplateCompositionDTO;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.entity.Template;
import org.springblade.composition.entity.TemplateComposition;
import org.springblade.composition.service.ICompositionService;
import org.springblade.composition.service.ITemplateCompositionService;
import org.springblade.composition.service.ITemplateService;
import org.springblade.composition.dto.TemplateDTO;
import org.springblade.composition.vo.TemplateVO;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;


import org.springblade.core.tool.api.ResultCode;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.flow.core.feign.IFlowEngineClient;
import org.springblade.task.entity.Task;
import org.springblade.task.feign.ITaskClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


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
	private final ICompositionService compositionService;
	private final IFlowEngineClient iFlowEngineClient;
	private final ITaskClient taskClient;

	@GetMapping("/role-field")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "角色标注的字段", notes = "传入template ID")
	public R<Composition> detail(Long templateId) {
		TemplateComposition templateComposition = new TemplateComposition();
		templateComposition.setTemplateId(templateId);
		// 补充信息人员的角色名
		String supplementRoleName = "ci";
		// 如果是补充信息人员，直接返回一个composition
		String userRoleName = AuthUtil.getUserRole();
		if(supplementRoleName.equals(userRoleName)){
			Composition composition = new Composition();
			composition.setAnnotationType(3);
			return R.data(composition);
		}
		templateComposition.setRoleName(userRoleName);
		TemplateComposition detail = templateCompositionService.getOne(Condition.getQueryWrapper(templateComposition));
		if (detail == null){
			return R.data(ResultCode.FAILURE.getCode(),null,"数据库中未找到");
		}
		Long compositionId = detail.getCompositionId();
		Composition composition = compositionService.getById(compositionId);
		if (composition == null){
			return R.data(ResultCode.FAILURE.getCode(),null,"数据库中未找到");
		}
		return R.data(composition);
	}

	@GetMapping("/all-compositions")
	@ApiOperationSupport(order = 8)
	@ApiOperation(value = "模板对应的所有组合", notes = "传入模板ID")
	public R<List<Composition>> allCompositions(Long templateId) {
		List<Composition> compositionList = templateService.allCompositions(templateId);
		if (compositionList == null) {
			R.data(ResultCode.FAILURE.getCode(),null,"数据库中未找到");
		}
		return R.data(compositionList);
	}

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
	@ApiOperationSupport(order = 7)
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
	 * 条件：启用状态，还是停用状态
	 */
	@GetMapping("/list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "status", value = "状态(1:启用,2:停用)", paramType = "query", dataType = "int")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入template")
	public R<IPage<Template>> list(@ApiIgnore @RequestParam Map<String, Object> template, Query query) {
		IPage<Template> pages = templateService.page(Condition.getPage(query), Condition.getQueryWrapper(template, Template.class).orderByDesc("update_Time"));
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
	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "新增或修改模板，同时构建组合", notes = "传入templateDTO")
	public R submit(@Valid @RequestBody TemplateDTO templateDTO) {
		Template template = Objects.requireNonNull(BeanUtil.copy(templateDTO, Template.class));
		// 不等于null，就是更新操作。需要首先判断是否有任务已经使用此模板
		if (template.getId() != null){
			Task task = taskClient.getByTemplate(template.getId()).getData();
			if (task.getId() != null){
				return R.fail("有任务已经使用此模板，不能修改，可以停用");
			}
		}

		List<TemplateCompositionDTO> templateCompositions = templateDTO.getTemplateCompositions();
		templateCompositions.forEach(templateCompositionDTO -> {
			Composition composition = compositionService.getById(templateCompositionDTO.getCompositionId());
			templateCompositionDTO.setCompositionName(composition.getName());
			templateCompositionDTO.setCompositionType(composition.getAnnotationType());
			templateCompositionDTO.setCompositionField(composition.getField());
		});
		boolean generateProcess = true;	// 是否根据模版创建流程, 是就根据前端传来的组合数自定义流程, 否就用前端传来的流程定义Id
		if (generateProcess) {
			R result = iFlowEngineClient.deployModelByTemplate(templateDTO);
			if (!result.isSuccess()) {
				return R.fail("部署模版流程失败");
			}
			templateDTO.setProcessDefinitionId((String) result.getData());
		}
		boolean tmp = templateService.saveOrUpdate(template);
		if(!tmp) {
			return R.fail("新建模板失败");
		}
		List<TemplateComposition> templateCompositionList = templateCompositions.stream().map(templateCompositionDTO -> {
			TemplateComposition templateComposition = BeanUtil.copy(templateCompositionDTO, TemplateComposition.class);
			templateComposition.setTemplateId(template.getId());
			return templateComposition;
		}).collect(Collectors.toList());
		return R.status(templateService.compose(templateCompositionList));

	}


	/**
	 * 删除模板
	 * 相应的组合也删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		Task task = taskClient.getByTemplate(Long.valueOf(ids)).getData();
		if (task.getId() != null){
			return R.fail("有任务已经使用此模板，只能停用，不能删除");
		}
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
