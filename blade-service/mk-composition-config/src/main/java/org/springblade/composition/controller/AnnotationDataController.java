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
import org.springblade.adata.feign.IRealSetExpertClient;
import org.springblade.composition.dto.AnnotationCompleteDTO;
import org.springblade.composition.entity.*;
import org.springblade.composition.service.*;
import org.springblade.composition.vo.AnnotationDataVO;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.flow.core.entity.SingleFlow;
import org.springblade.flow.core.feign.IFlowClient;
import org.springblade.task.feign.ILabelTaskClient;
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
@RequestMapping("/annotation_data")
@Api(value = "标注数据", tags = "标注数据")
public class AnnotationDataController extends BladeController {

	private final IAnnotationDataService annotationDataService;
	private final IExpertClient expertClient;
	private final ILabelTaskClient labelTaskClient;
	private final IStatisticsService statisticsService;
	private final ICompositionService compositionService;
	private final IRealSetAnnotationDataService realSetAnnotationDataService;
	private final IRealSetExpertClient realSetExpertClient;
	private final IAutoInspectionService autoInspectionService;
	private final AnnotationDataErrataService annotationDataErrataService;
	private final IFlowClient flowClient;

	/**
	 * 查询标注数据
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入AnnotationData")
	public R<List<AnnotationData>> detail(AnnotationData annotationData) {
		List<AnnotationData> annotationDataList = annotationDataService.list(Condition.getQueryWrapper(annotationData));
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
	@ApiOperation(value = "分页", notes = "传入AnnotationData")
	public R<IPage<AnnotationData>> list(@ApiIgnore @RequestParam Map<String, Object> annotationData, Query query) {
		IPage<AnnotationData> pages = annotationDataService.page(Condition.getPage(query), Condition.getQueryWrapper(annotationData, AnnotationData.class));
		return R.data(pages);
	}

//	/**
//	 * 批量新增或修改标注数据
//	 * 每次都会逻辑删除之前的数据，不需要id，通过sub_task_id与field来查询删除数据
//	 * 每次修改后同时更新mk_adata_expert表中的数据
//	 */
//	@PostMapping("/submit")
//	@ApiOperationSupport(order = 3)
//	@Transactional(rollbackFor = Exception.class)
//	@ApiOperation(value = "批量新增或修改", notes = "传入AnnotationDataVO对象")
//	public R submit(@Valid @RequestBody AnnotationDataVO annotationDataVO) {
//		boolean res = submitData(annotationDataVO);
//		return R.status(res);
//	}

	/**
	 * 批量新增或修改标注数据
	 * 每次都会逻辑删除之前的数据，不需要id，通过sub_task_id与field来查询删除数据
	 * 每次修改后同时更新mk_adata_expert表中的数据
	 */
	@PostMapping("/submit-and-complete")
	@ApiOperationSupport(order = 3)
//	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "批量新增或修改", notes = "传入AnnotationCompleteDTO对象")
	public R submitAndComplete(@Valid @RequestBody AnnotationCompleteDTO annotationCompleteDTO) {
		AnnotationDataVO annotationDataVO = Objects.requireNonNull(BeanUtil.copy(annotationCompleteDTO, AnnotationDataVO.class));
		boolean res = submitData(annotationDataVO);
		SingleFlow singleFlow = Objects.requireNonNull(BeanUtil.copy(annotationCompleteDTO, SingleFlow.class));
		R res2 = flowClient.completeTask(singleFlow);
		if (res2.isSuccess()) {
			return R.data(res2.getData());
		}else {
			return R.fail("完成任务失败");
		}

	}

	@Transactional(rollbackFor = Exception.class)
	public boolean submitData(AnnotationDataVO annotationDataVO) {
		// 清理标注数据前后的多余空白字符
		if (annotationDataVO.getAnnotationDataList() != null) {
			annotationDataVO.getAnnotationDataList().forEach(annotationData -> annotationData.setValue(StringUtil.trimWhitespace(annotationData.getValue())));
			List<AnnotationData> annotationDataList = annotationDataVO.getAnnotationDataList().stream().filter(annotationData -> Func.isNotBlank(annotationData.getField())).collect(Collectors.toList());
			annotationDataVO.setAnnotationDataList(annotationDataList);
		}

		List<AnnotationData> annotationDataList = annotationDataVO.getAnnotationDataList();
		//获得之前标注的数据
		List<AnnotationData> oldAnnotationDataList = annotationDataService.list(Wrappers.<AnnotationData>query().lambda()
			.eq(AnnotationData::getSubTaskId, annotationDataVO.getSubTaskId())
			.eq(AnnotationData::getCompositionId, annotationDataVO.getCompositionId())
			.eq(AnnotationData::getCreateUser, AuthUtil.getUserId())
		);

		// 删除原来的标注数据,同时更新修改时间
		if (oldAnnotationDataList.size() != 0) {
			List<Long> oldAnnotationDataIds = new ArrayList<>();
			oldAnnotationDataList.forEach(oldAnnotationData -> oldAnnotationDataIds.add(oldAnnotationData.getId()));
			annotationDataService.deleteLogic(oldAnnotationDataIds);
		}
		// 注意补充信息角色
		Expert expert = new Expert();
		expert.setId(annotationDataVO.getExpertId());
		if (oldAnnotationDataList.size() != 0) {
			oldAnnotationDataList.forEach(oldAnnotationData->BeanUtil.setProperty(expert, oldAnnotationData.getField(),""));
		}
		if (annotationDataList != null){
			annotationDataList.forEach(annotationData->BeanUtil.setProperty(expert, annotationData.getField(),annotationData.getValue()));
		}
		expertClient.saveExpert(expert);

		//更新统计表，记录标注用时
		statisticsService.updateAnnotationStatistics(
			1,
			annotationDataVO.getSubTaskId(),
			annotationDataVO.getTemplateId(),
			annotationDataVO.getCompositionId(),
			annotationDataVO.getTime());

		if(annotationDataList != null){
			annotationDataService.saveBatch(annotationDataList);
		}
		return true;
	}

}
