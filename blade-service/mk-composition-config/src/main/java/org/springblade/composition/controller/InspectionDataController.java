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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.composition.entity.AnnotationData;
import org.springblade.composition.entity.InspectionData;
import org.springblade.composition.entity.Statistics;
import org.springblade.composition.service.IAnnotationDataService;
import org.springblade.composition.service.IInspectionDataService;
import org.springblade.composition.service.IStatisticsService;
import org.springblade.composition.vo.AnnotationDataVO;
import org.springblade.composition.vo.InspectionDataVO;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.task.feign.ILabelTaskClient;
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
@RequestMapping("/inspection_data")
@Api(value = "质检数据", tags = "质检数据")
public class InspectionDataController extends BladeController {

	private final IInspectionDataService inspectionDataService;
	private final IExpertClient expertClient;
	private final ILabelTaskClient labelTaskClient;
	private final IStatisticsService statisticsService;

	/**
	 * 查询质检数据
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入InspectionData")
	public R<List<InspectionData>> detail(InspectionData inspectionData) {
		List<InspectionData> inspectionDataList = inspectionDataService.list(Condition.getQueryWrapper(inspectionData));
		//iSubTaskClient.startProcess()
		return R.data(inspectionDataList);
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
	@ApiOperation(value = "分页", notes = "传入InspectionData")
	public R<IPage<InspectionData>> list(@ApiIgnore @RequestParam Map<String, Object> inspectionData, Query query) {
		IPage<InspectionData> pages = inspectionDataService.page(Condition.getPage(query), Condition.getQueryWrapper(inspectionData, InspectionData.class));
		return R.data(pages);
	}

	/**
	 * 批量新增或修改质检数据
	 * 每次都会逻辑删除之前的数据，不需要id，通过sub_task_id与用户id来查询删除数据
	 * 每次修改后同时更新mk_adata_expert表中的数据
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 3)
	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "批量新增或修改", notes = "传入InspectionDataVO对象")
	public R submit(@Valid @RequestBody InspectionDataVO inspectionDataVO) {

		Long subTaskId  = inspectionDataVO.getSubTaskId();
		List<InspectionData> inspectionDataList = inspectionDataVO.getInspectionDataList();
		// 删除原来的标注数据
		inspectionDataService.remove(Wrappers.<InspectionData>update().lambda().eq(InspectionData::getSubTaskId, inspectionDataVO.getSubTaskId()).and(i->i.eq(InspectionData::getUpdateUser, AuthUtil.getUserId())));

		//更新统计表，记录标注用时
		Statistics statistics_query = new Statistics();
		statistics_query.setSubTaskId(subTaskId);
		statistics_query.setCompositionId(inspectionDataVO.getCompositionId());
		statistics_query.setUserId(AuthUtil.getUserId());

		Statistics statistics = statisticsService.getOne(Condition.getQueryWrapper(statistics_query));
		if (statistics != null){
			statistics.setTime(statistics.getTime() + inspectionDataVO.getTime());
		} else {
			statistics = new Statistics();
			statistics.setType(1);
			statistics.setTime(inspectionDataVO.getTime());
			statistics.setUserId(AuthUtil.getUserId());
			statistics.setCompositionId(inspectionDataVO.getCompositionId());
			statistics.setSubTaskId(subTaskId);
			statistics.setTemplateId(inspectionDataVO.getTemplateId());
		}
		statisticsService.saveOrUpdate(statistics);
		if(inspectionDataList != null){
			return R.status(inspectionDataService.saveBatch(inspectionDataList));
		}else{
			return R.success("没有数据保存");
		}

	}



}
