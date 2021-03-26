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
import lombok.AllArgsConstructor;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.RealSetExpert;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.adata.feign.IRealSetExpertClient;
import org.springblade.composition.entity.*;
import org.springblade.composition.service.*;
import org.springblade.composition.vo.AnnotationDataVO;
import org.springblade.composition.vo.RealSetAnnotationDataVO;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.Func;
import org.springblade.core.tool.utils.StringUtil;
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
@RequestMapping("/realset-annotation-data")
@Api(value = "标注数据", tags = "标注数据")
public class RealSetAnnotationDataController extends BladeController {

	private final IAnnotationDataService annotationDataService;
	private final IExpertClient expertClient;
	private final ILabelTaskClient labelTaskClient;
	private final IStatisticsService statisticsService;
	private final ICompositionService compositionService;
	private final IRealSetAnnotationDataService realSetAnnotationDataService;
	private final IRealSetExpertClient realSetExpertClient;
	private final IAutoInspectionService autoInspectionService;
	private final AnnotationDataErrataService annotationDataErrataService;

	/**
	 * 查询标注数据
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入RealSetAnnotationData")
	public R<List<RealSetAnnotationData>> detail(RealSetAnnotationData realSetAnnotationData) {
		List<RealSetAnnotationData> realSetAnnotationDataList = realSetAnnotationDataService.list(Condition.getQueryWrapper(realSetAnnotationData));
		//iSubTaskClient.startProcess()
		return R.data(realSetAnnotationDataList);
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
	public R<IPage<RealSetAnnotationData>> list(@ApiIgnore @RequestParam Map<String, Object> realSetAnnotationData, Query query) {
		IPage<RealSetAnnotationData> pages = realSetAnnotationDataService.page(Condition.getPage(query), Condition.getQueryWrapper(realSetAnnotationData, RealSetAnnotationData.class));
		return R.data(pages);
	}

	/**
	 * 提交真题标志数据接口
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 4)
	@Transactional(rollbackFor = Exception.class)
	@ApiOperation(value = "真题标注数据批量新增或修改", notes = "传入RealSetAnnotationDataVO对象")
	public R submit(@Valid @RequestBody RealSetAnnotationDataVO annotationDataVO) {
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
//		AtomicInteger isCompositionTrue = new AtomicInteger(1);
//		Long timestamp=System.currentTimeMillis();
//		AtomicInteger totalTime = new AtomicInteger();
//		annotationDataList.forEach(realSetAnnotationData -> {
//			realSetAnnotationData.setRealSetId(timestamp);
//			int is_true = 2;
//			String answer = String.valueOf(BeanUtil.getProperty(realData,realSetAnnotationData.getField()));
//			if (answer == null){
//				answer = "";
//			}
//			if(realSetAnnotationData.getValue().equals(answer)){
//				is_true = 1;
//			}
//			if(is_true==2){
//				isCompositionTrue.set(2);
//			}
//			totalTime.addAndGet(realSetAnnotationData.getTime());
//			realSetAnnotationData.setIsTrue(is_true);
//		});
		List<AnnotationDataErrata> annotationDataErrataList = new ArrayList<>();
		// 每次提交的通过时间戳生成一个唯一id
		Long timestamp=System.currentTimeMillis();
		// 判断这次提交的正确错误，有一个字段错，这次提交就是错的。1是正确，2是错误
		int isCompositionTrue = 1;
		for(RealSetAnnotationData realSetAnnotationData:annotationDataList){
			realSetAnnotationData.setRealSetId(timestamp);
			// 判断字段是正确还是错误，2是错误，1是正确。
			int is_true = 2;
			String answer = String.valueOf(BeanUtil.getProperty(realData,realSetAnnotationData.getField()));
			if (answer == null){
				answer = "";
			}
			if ("avatar".equals(realSetAnnotationData.getField())) {	//头像默认正确，直接保存
				realSetAnnotationData.setIsTrue(1);
				realSetAnnotationDataService.save(realSetAnnotationData);
			}else {
				if (realSetAnnotationData.getValue().equals(answer)) {
					is_true = 1;
				}
				realSetAnnotationData.setIsTrue(is_true);
				realSetAnnotationDataService.save(realSetAnnotationData);
				if (is_true == 2) {
					isCompositionTrue = 2;
					AnnotationDataErrata annotationDataErrata = Objects.requireNonNull(BeanUtil.copy(realSetAnnotationData, AnnotationDataErrata.class));
					annotationDataErrata.setSubTaskId(annotationDataVO.getSubTaskId());
					annotationDataErrata.setAnnotationDataId(realSetAnnotationData.getId());
					annotationDataErrata.setLabelerId(AuthUtil.getUserId());
					annotationDataErrata.setValue(answer);
					annotationDataErrata.setAnnotationDataId(realSetAnnotationData.getId());
					annotationDataErrata.setSource(2);
					annotationDataErrataList.add(annotationDataErrata);
				}
			}
		}
		if (annotationDataErrataList.size() > 0) {
			annotationDataErrataService.saveBatch(annotationDataErrataList);
		}

		Long subTaskId  = annotationDataVO.getSubTaskId();
		//更新统计表，记录标注用时
		Statistics statistics_query = new Statistics();
		statistics_query.setSubTaskId(subTaskId);
		statistics_query.setCompositionId(annotationDataVO.getCompositionId());
		statistics_query.setUserId(AuthUtil.getUserId());
		statistics_query.setType(2);

		Statistics statistics = statisticsService.getOne(Condition.getQueryWrapper(statistics_query));
		if (statistics != null){
			statistics.setTime(statistics.getTime() + annotationDataVO.getTime());
			statistics.setStatus(2);
			statisticsService.saveOrUpdate(statistics);
		} else {	//有两种情况:
			// 1.组合没有被标;2.被标过但不是当前的人
			boolean wrote = false;
			Statistics stat_his_query = new Statistics();
			stat_his_query.setSubTaskId(subTaskId);
			stat_his_query.setCompositionId(annotationDataVO.getCompositionId());
			List<Statistics> statistics_his = statisticsService.list(Condition.getQueryWrapper(stat_his_query));
			for(Statistics statistics_history: statistics_his) {
				if (null == statistics_history.getUserId()) {	//	组合没有被标
					statistics_history.setTime(annotationDataVO.getTime());
					statistics_history.setStatus(2);
					statistics_history.setUserId(AuthUtil.getUserId());
					statistics_history.setCompositionId(annotationDataVO.getCompositionId());
					statistics_history.setSubTaskId(subTaskId);
					statistics_history.setTemplateId(annotationDataVO.getTemplateId());
					statistics_history.setType(2);
					if (isCompositionTrue == 2) {
						statistics_history.setIsWrong(2);
						statisticsService.calcReliabilityRate(AuthUtil.getUserId());
					}
					wrote = true;
					statisticsService.saveOrUpdate(statistics_history);
					break;
				}
			};
//			if (!wrote) {    //	被标过但不是当前的人
//				Statistics new_stat = new Statistics();
//				new_stat.setTime(annotationDataVO.getTime());
//				new_stat.setStatus(2);
//				new_stat.setUserId(AuthUtil.getUserId());
//				new_stat.setCompositionId(annotationDataVO.getCompositionId());
//				new_stat.setSubTaskId(subTaskId);
//				new_stat.setTemplateId(annotationDataVO.getTemplateId());
//				new_stat.setType(1);
//				statisticsService.saveOrUpdate(new_stat);
//			}
		}

		// 保存结果
		AutoInspection autoInspection = Objects.requireNonNull(BeanUtil.copy(annotationDataList.get(0), AutoInspection.class));
		autoInspection.setIsCompositionTrue(isCompositionTrue);
		autoInspectionService.save(autoInspection);
		return R.status(true);
	}

}
