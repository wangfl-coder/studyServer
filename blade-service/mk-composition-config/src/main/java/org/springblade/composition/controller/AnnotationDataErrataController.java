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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.RealSetExpert;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.adata.feign.IRealSetExpertClient;
import org.springblade.composition.dto.AnnotationDataErrataDTO;
import org.springblade.composition.entity.*;
import org.springblade.composition.service.*;
import org.springblade.composition.vo.AnnotationCompositionErrataVO;
import org.springblade.composition.vo.AnnotationDataErrataVO;
import org.springblade.composition.vo.RealSetAnnotationDataVO;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.task.entity.Task;
import org.springblade.task.feign.ILabelTaskClient;
import org.springblade.task.vo.TaskVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;


/**
 * 控制器
 *
 * @author KaiLun
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("/annotation-data-errata")
@Api(value = "勘误标注数据", tags = "勘误标注数据")
public class AnnotationDataErrataController extends BladeController {

	private final IAnnotationDataService annotationDataService;
	private final AnnotationDataErrataService annotationDataErrataService;
	private final IExpertClient expertClient;
	private final ILabelTaskClient labelTaskClient;
	private final IStatisticsService statisticsService;
	private final ICompositionService compositionService;
	private final IRealSetAnnotationDataService realSetAnnotationDataService;
	private final IRealSetExpertClient realSetExpertClient;

	/**
	 * 查询标注数据
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入AnnotationDataErrata")
	public R<List<AnnotationDataErrata>> detail(AnnotationDataErrata annotationDataErrata) {
		List<AnnotationDataErrata> annotationDataList = annotationDataErrataService.list(Condition.getQueryWrapper(annotationDataErrata));
		//iSubTaskClient.startProcess()
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
	@ApiOperation(value = "分页", notes = "传入AnnotationDataErrata")
	public R<IPage<AnnotationDataErrataVO>> list(@ApiIgnore @RequestParam Map<String, Object> annotationDataErrata, Query query) {
		IPage<AnnotationDataErrata> pages = annotationDataErrataService.page(Condition.getPage(query), Condition.getQueryWrapper(annotationDataErrata, AnnotationDataErrata.class));
		List<AnnotationDataErrataVO> recordList = pages.getRecords().stream().map(errata -> {
			AnnotationDataErrataVO errataVO = Objects.requireNonNull(BeanUtil.copy(errata, AnnotationDataErrataVO.class));
			AnnotationData annoData = annotationDataService.getById(errata.getAnnotationDataId());
			errataVO.setAnnotationData(annoData);
			return errataVO;
		}).collect(Collectors.toList());
		IPage<AnnotationDataErrataVO> annotationDataErrataVO = new Page(pages.getCurrent(), pages.getSize(), pages.getTotal());
		annotationDataErrataVO.setRecords(recordList);
		return R.data(annotationDataErrataVO);
	}

	/**
	 * 分页
	 *
	 */
	@GetMapping("/composition-list")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "labelerId", value = "标注员ID", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "compositionName", value = "组合ID", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "startTime", value = "开始时间", paramType = "query", dataType = "string"),
		@ApiImplicitParam(name = "endTime", value = "结束时间", paramType = "query", dataType = "string")
	})
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入annotationDataErrata")
	public R<IPage<AnnotationCompositionErrataVO>> compositionList(@ApiIgnore @RequestParam Map<String, Object> annotationDataErrata, Query query) {
		List<AnnotationCompositionErrataVO> recordList = annotationDataErrataService.getAnnotationCompositionErrataList(annotationDataErrata, (query.getCurrent()-1)*query.getSize(), query.getSize());
		int total = annotationDataErrataService.getAnnotationCompositionErrataAll(annotationDataErrata).size();
		IPage<AnnotationCompositionErrataVO> annotationCompositionErrataVO = new Page(query.getCurrent(), query.getSize(), total);
		annotationCompositionErrataVO.setRecords(recordList);
		return R.data(annotationCompositionErrataVO);
	}

	/**
	 * 批量新增或修改标注数据
	 * 每次都会逻辑删除之前的数据，不需要id，通过sub_task_id与field来查询删除数据
	 * 每次修改后同时更新mk_adata_expert表中的数据
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 3)
	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "批量新增或修改", notes = "传入AnnotationDataDTO对象")
	public R submit(@Valid @RequestBody AnnotationDataErrataDTO annotationDataErrataDTO, BladeUser bladeUser) {
		// 清理标注数据前后的多余空白字符
		if (annotationDataErrataDTO.getAnnotationDataErrataList() != null) {
			annotationDataErrataDTO.getAnnotationDataErrataList().forEach(annotationData -> {annotationData.setValue(StringUtil.trimWhitespace(annotationData.getValue()));});
		}
		Long subTaskId  = annotationDataErrataDTO.getSubTaskId();
		List<AnnotationDataErrata> annotationDataErrataList = annotationDataErrataDTO.getAnnotationDataErrataList();
		//获得之前标注的数据
		List<AnnotationDataErrata> oldAnnotationDataList = annotationDataErrataService.list(Wrappers.<AnnotationDataErrata>query().lambda()
			.eq(AnnotationDataErrata::getSubTaskId, annotationDataErrataDTO.getSubTaskId())
			.eq(AnnotationDataErrata::getCompositionId, annotationDataErrataDTO.getCompositionId())
		);

		// 删除原来的标注数据,同时更新修改时间
		if (annotationDataErrataList != null && oldAnnotationDataList.size() != 0) {
			Map<String, List<AnnotationDataErrata>> dataPerField = annotationDataErrataList.stream()
				.collect(groupingBy(AnnotationDataErrata::getField));
			Set<String> fields = dataPerField.keySet();
			List<Long> oldAnnotationDataIds = new ArrayList<>();
			oldAnnotationDataList.forEach(oldAnnotationData -> {
				if (!fields.contains(oldAnnotationData.getField())) {
					AnnotationDataErrata annotationDataErrata = Objects.requireNonNull(BeanUtil.copy(oldAnnotationData, AnnotationDataErrata.class));
					annotationDataErrata.setId(null);
					annotationDataErrata.setSource(1);
					annotationDataErrataList.add(annotationDataErrata);
				}
				oldAnnotationDataIds.add(oldAnnotationData.getId());
			});
			annotationDataErrataService.deleteLogic(oldAnnotationDataIds);
		}
		// 注意补充信息角色
		Expert expert = new Expert();
		expert.setId(annotationDataErrataDTO.getExpertId());
		if (oldAnnotationDataList.size() != 0) {
			oldAnnotationDataList.forEach(oldAnnotationData->BeanUtil.setProperty(expert, oldAnnotationData.getField(),""));
		}
		if (annotationDataErrataList != null){
			annotationDataErrataList.forEach(annotationDataErrata->BeanUtil.setProperty(expert, annotationDataErrata.getField(),annotationDataErrata.getValue()));
		}
		expertClient.saveExpert(expert);

		//更新统计表，记录标注用时
		Statistics statistics_query = new Statistics();
		statistics_query.setSubTaskId(subTaskId);
		statistics_query.setCompositionId(annotationDataErrataDTO.getCompositionId());
		statistics_query.setUserId(AuthUtil.getUserId());

		Statistics statistics = statisticsService.getOne(Condition.getQueryWrapper(statistics_query));
		if (statistics != null){
			statistics.setTime(statistics.getTime() + annotationDataErrataDTO.getTime());
			statistics.setStatus(2);
			statisticsService.saveOrUpdate(statistics);
		} else {
			Statistics new_stat = new Statistics();
			new_stat.setTime(annotationDataErrataDTO.getTime());
			new_stat.setStatus(2);
			new_stat.setUserId(AuthUtil.getUserId());
			new_stat.setCompositionId(annotationDataErrataDTO.getCompositionId());
			new_stat.setSubTaskId(subTaskId);
			new_stat.setTemplateId(annotationDataErrataDTO.getTemplateId());
			new_stat.setType(3);
			statisticsService.saveOrUpdate(new_stat);
		}

		if(annotationDataErrataList != null){
			return R.status(annotationDataErrataService.saveBatch(annotationDataErrataList));
		}else{
			return R.success("没有数据保存");
		}

	}

	/**
	 * 提交真题标志数据接口
	 */
	@PostMapping("/submit_real_set")
	@ApiOperationSupport(order = 4)
	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "真题标注数据批量新增或修改", notes = "传入AnnotationDataVO对象")
	public R submitRealSet(@Valid @RequestBody RealSetAnnotationDataVO annotationDataVO) {
		// 清理标注数据前后的多余空白字符
		if (annotationDataVO.getRealSetAnnotationDataList() != null) {
			annotationDataVO.getRealSetAnnotationDataList().forEach(annotationData -> {annotationData.setValue(StringUtil.trimWhitespace(annotationData.getValue()));});
		}
		List<RealSetAnnotationData> annotationDataList = annotationDataVO.getRealSetAnnotationDataList();
		// 获取真题答案
		RealSetExpert realSetExpert = new RealSetExpert();
		realSetExpert.setId(annotationDataVO.getExpertId());
		RealSetExpert realData = realSetExpertClient.detail(realSetExpert).getData();

		// 逐个字段检查正确与否
		annotationDataList.forEach(realSetAnnotationData -> {
			int is_true = 2;
			String answer = String.valueOf(BeanUtil.getProperty(realData,realSetAnnotationData.getField()));
			if (answer == null){
				answer = "";
			}
			if(realSetAnnotationData.getValue().equals(answer)){
				is_true = 1;
			}
			realSetAnnotationData.setIsTrue(is_true);
		});
		// 保存结果
		return R.status(realSetAnnotationDataService.saveBatch(annotationDataList));
	}

}
