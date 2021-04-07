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
package org.springblade.feedback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.Ignore;
import io.swagger.annotations.*;
import lombok.AllArgsConstructor;
import org.springblade.composition.feign.IAnnotationDataErrataClient;
import org.springblade.composition.feign.IStatisticsClient;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.feedback.entity.Feedback;
import org.springblade.feedback.service.IFeedbackService;
import org.springblade.feedback.vo.FeedbackVO;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 *  控制器
 *
 * @author BladeX
 * @since 2021-03-22
 */
@RestController
@AllArgsConstructor
@RequestMapping("/feedback")
@Api(value = "", tags = "接口")
public class FeedbackController extends BladeController {

	private final IFeedbackService feedbackService;

	private IStatisticsClient iStatisticsClient;
	private IAnnotationDataErrataClient iAnnotationDataErrataClient;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "详情", notes = "传入feedback")
	public R<Feedback> detail(Feedback feedback) {
		Feedback detail = feedbackService.getOne(Condition.getQueryWrapper(feedback));
		return R.data(detail);
	}

	/**
	 * 分页
	 */
	@GetMapping("/list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "分页", notes = "传入feedback")
	public R<IPage<Feedback>> list(Feedback feedback, Query query) {
		IPage<Feedback> pages = feedbackService.page(Condition.getPage(query), Condition.getQueryWrapper(feedback));
		return R.data(pages);
	}

	/**
	 * 自定义分页
	 */
	@GetMapping("/page")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "分页", notes = "传入feedback")
	public R<IPage<FeedbackVO>> page(FeedbackVO feedback, Query query) {
		IPage<FeedbackVO> pages = feedbackService.selectFeedbackPage(Condition.getPage(query), feedback);
		return R.data(pages);
	}

	/**
	 * 新增
	 */
	@PostMapping("/save")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "新增", notes = "传入feedback")
	public R save(@Valid @RequestBody Feedback feedback) {
		boolean save = feedbackService.save(feedback);
		System.out.println(save);
		return R.status(save);
	}

	/**
	 * 修改
	 */
	@PostMapping("/update")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "修改", notes = "传入feedback")
	public R update(@Valid @RequestBody Feedback feedback) {
		boolean update = feedbackService.updateById(feedback);
		return R.status(update);
	}

	/**
	 * 新增或修改
	 */
	@PostMapping("/submit")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "新增或修改", notes = "传入feedback")
	public R submit(@Valid @RequestBody Feedback feedback) {
		return R.status(feedbackService.saveOrUpdate(feedback));
	}


	/**
	 * 删除
	 */
	@PostMapping("/remove")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "逻辑删除", notes = "传入ids")
	public R remove(@ApiParam(value = "主键集合", required = true) @RequestParam String ids) {
		return R.status(feedbackService.deleteLogic(Func.toLongList(ids)));
	}

	/**
	 * 申述成功
	 */
	@GetMapping("/feedback-success")
	@ApiOperation(value = "申述成功")
	public R<Boolean> setStatusSuccess(@RequestParam Long compositionId, @RequestParam Long subTaskId, @RequestParam Long userId, @RequestParam String field, @RequestBody Map<String,String> fieldValues){

		Boolean flag_ufs = updateFeedbackStatus(compositionId,subTaskId,userId,3,field);
		Boolean flag_uae = iAnnotationDataErrataClient.ifNeedUpdateAnnotationDataErrataStatusAndValue(compositionId,subTaskId,userId,field,fieldValues,3).getData();
		if (iAnnotationDataErrataClient.queryAnnotationDataErrataStatus(compositionId,subTaskId,userId)){
			return R.data(flag_ufs && flag_uae && iStatisticsClient.ifNeedToUpdateStatisticIsWrongFeedbackStatus(compositionId,subTaskId,userId).getData());
		}
		return R.data(flag_uae && flag_ufs);
	}

	/**
	 * 根据参数更新feedback的status，1。未审核 2。审核中 3。审核成功 4。审核失败
	 * @param compositionId
	 * @param subTaskId
	 * @param userId
	 * @param field
	 * @return
	 */
	public Boolean updateFeedbackStatus(Long compositionId, Long subTaskId, Long userId, int status, String field){
		LambdaUpdateWrapper<Feedback> updateWrapper = Wrappers.lambdaUpdate();
		updateWrapper.eq(Feedback::getCompositionId, compositionId)
			.eq(Feedback::getSubTaskId, subTaskId)
			// 1。未审核 2。审核中 3。审核成功 4。审核失败
			.eq(Feedback::getCreateUser, userId)
			.eq(Feedback::getField, field)
			.set(Feedback::getStatus, status);
		Boolean flag = feedbackService.update(updateWrapper);
		return flag;

	}

	/**
	 * 申述失败
	 */
	@GetMapping("/feedback-fail")
	@ApiOperation(value = "申述失败")
	public R<Boolean> setStatusFail(@RequestParam Long compositionId, @RequestParam Long subTaskId, @RequestParam Long userId, @RequestParam String field, @RequestBody Map<String,String> fieldValues){
		Boolean flag_ufs = updateFeedbackStatus(compositionId,subTaskId,userId,4,field);
		Boolean flag_uae = iAnnotationDataErrataClient.ifNeedUpdateAnnotationDataErrataStatusAndValue(compositionId,subTaskId,userId,field,fieldValues,4).getData();
		return R.data(flag_uae && flag_ufs && iStatisticsClient.ifNeedToUpdateStatisticFeedbackStatus(4,compositionId,subTaskId,userId).getData());
	}

}
