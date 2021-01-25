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
package org.springblade.flow.business.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.flowable.engine.TaskService;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.RealSetExpert;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.adata.feign.IRealSetExpertClient;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.flow.business.service.FlowBusinessService;
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.entity.SingleFlow;
import org.springblade.flow.core.utils.TaskUtil;
import org.springblade.flow.engine.entity.FlowProcess;
import org.springblade.flow.engine.service.FlowEngineService;
import org.springblade.task.entity.LabelTask;
import org.springblade.task.entity.Task;
import org.springblade.task.feign.ILabelTaskClient;
import org.springblade.task.feign.ITaskClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

/**
 * 流程事务通用接口
 *
 * @author Chill
 */
@NonDS
@RestController
@AllArgsConstructor
@RequestMapping("work")
@Api(value = "流程事务通用接口", tags = "流程事务通用接口")
public class WorkController {

	private final TaskService taskService;
	private final FlowEngineService flowEngineService;
	private final FlowBusinessService flowBusinessService;
	private final ITaskClient taskClient;
	private final IExpertClient expertClient;
	private final IRealSetExpertClient realSetExpertClient;


	/**
	 * 发起事务列表页
	 */
	@GetMapping("start-list")
	@ApiOperationSupport(order = 1)
	@ApiOperation(value = "发起事务列表页", notes = "传入流程类型")
	public R<IPage<FlowProcess>> startList(@ApiParam("流程类型") String category, Query query, @RequestParam(required = false, defaultValue = "1") Integer mode) {
		IPage<FlowProcess> pages = flowEngineService.selectProcessPage(Condition.getPage(query), category, mode);
		return R.data(pages);
	}

	/**
	 * 待签事务列表页
	 */
	@GetMapping("claim-list")
	@ApiOperationSupport(order = 2)
	@ApiOperation(value = "待签事务列表页", notes = "传入流程信息")
	public R<IPage<SingleFlow>> claimList(@ApiParam("流程信息") SingleFlow bladeFlow, Query query) {
		IPage<SingleFlow> pages = flowBusinessService.selectClaimPage(Condition.getPage(query), bladeFlow);
		return R.data(pages);
	}

	/**
	 * 返回一个待签事务并签收
	 */
	@GetMapping("claim-one")
	@ApiOperationSupport(order = 3)
	@ApiOperation(value = "返回一个待签事务并签收", notes = "传入流程信息")
	public R<SingleFlow> claimOne(@RequestParam String categoryName,
								  @RequestParam(value="roleId", required=false) Long roleId,
								  @RequestParam(value="compositionId", required=false) Long compositionId) {
//		Query query = new Query();
//		query.setCurrent(1).setSize(1);
//		IPage<BladeFlow> bladeFlowIPage = flowBusinessService.selectClaimPage(Condition.getPage(query), bladeFlow);
//		if(bladeFlowIPage!=null){
//			BladeFlow bladeFlow1 = bladeFlowIPage.getRecords().get(1);
//			String processInstanceId = bladeFlow1.getProcessInstanceId();
//			R<LabelTask> lableTask = iLabelTaskClient.getLableTask(processInstanceId);
//			return lableTask.getData();
//		}else {
//			return null;
//		}
		SingleFlow flow = flowBusinessService.selectOneClaimPage(categoryName, roleId, compositionId);
		if(flow.getTaskId()!=null){
			// 判断是否出现真题,主页，补充信息，含有bio,bioZh,work,edu等基本信息字段的组合没有真题。其他情况通过掺入比例依概率产生真题。
			String compositionField = flow.getCompositionField();
			if (flow.getCompositionType() == 2 && !StringUtil.containsAny(compositionField,"bio")
				&& !StringUtil.containsAny(compositionField,"bio") && !StringUtil.containsAny(compositionField,"edu")
				&& !StringUtil.containsAny(compositionField,"work")) {
				// 获取到要领取的是哪个任务，查询出任务的掺入真题比例。
				Expert expert = new Expert();
				expert.setId(flow.getPersonId());
				Expert expertDetail = expertClient.detail(expert).getData();
				Task task = taskClient.getById(expertDetail.getTaskId()).getData();
				Random r = new Random();
				int randomResult = r.nextInt(100);
				if (randomResult < task.getRealSetRate()) {
					// 每次产生真题，随机从任务绑定的真题库中抽取一个真题。把真题的主页相关信息，例如hp,name等返回给前端。
					List<RealSetExpert> realSetExpertList = realSetExpertClient.getExpertIds(task.getId()).getData();
					RealSetExpert realSetExpert = realSetExpertList.get(r.nextInt(realSetExpertList.size()));
					flow.setPersonId(realSetExpert.getId());
					flow.setPersonName(realSetExpert.getName());
					flow.setExpertId(realSetExpert.getExpertId());
					flow.setHomepage(realSetExpert.getHomepage());
					flow.setHp(realSetExpert.getHp());
					flow.setGs(realSetExpert.getGs());
					flow.setDblp(realSetExpert.getDblp());
					flow.setPersonNameZh(realSetExpert.getNameZh());
					flow.setOtherHomepage(realSetExpert.getOtherHomepage());
					Long timestamp=System.currentTimeMillis();
					flow.setTaskId(String.valueOf(timestamp));
					// 前端通过这个标志是1判断是真题
					flow.setIsRealSet(1);
					return R.data(flow);
				}
			}

			taskService.claim(flow.getTaskId(), TaskUtil.getTaskUser());
			return R.data(flow);
		}else{
			return R.success("没有任务领取");
		}
	}

	/**
	 * 待办事务列表页
	 */
	@GetMapping("todo-list")
	@ApiOperationSupport(order = 4)
	@ApiOperation(value = "待办事务列表页", notes = "传入流程信息")
	public R<IPage<SingleFlow>> todoList(@ApiParam("流程信息") BladeFlow bladeFlow, Query query) {
		IPage<SingleFlow> pages = flowBusinessService.selectTodoPage(Condition.getPage(query), bladeFlow);
		return R.data(pages);
	}

	/**
	 * 已发事务列表页
	 */
	@GetMapping("send-list")
	@ApiOperationSupport(order = 5)
	@ApiOperation(value = "已发事务列表页", notes = "传入流程信息")
	public R<IPage<SingleFlow>> sendList(@ApiParam("流程信息") BladeFlow bladeFlow, Query query) {
		IPage<SingleFlow> pages = flowBusinessService.selectSendPage(Condition.getPage(query), bladeFlow);
		return R.data(pages);
	}

	/**
	 * 办结事务列表页
	 */
	@GetMapping("done-list")
	@ApiOperationSupport(order = 6)
	@ApiOperation(value = "办结事务列表页", notes = "传入流程信息")
	public R<IPage<SingleFlow>> doneList(@ApiParam("流程信息") BladeFlow bladeFlow, Query query) {
		IPage<SingleFlow> pages = flowBusinessService.selectDonePage(Condition.getPage(query), bladeFlow);
		return R.data(pages);
	}

	/**
	 * 根据专家id查询办结事务
	 */
	@GetMapping("done-by-personId")
	@ApiOperationSupport(order = 11)
	@ApiOperation(value = "根据专家id查询办结事务", notes = "传入流程信息")
	public R<IPage<SingleFlow>> queryDoneByPersonId(@ApiParam("流程信息") BladeFlow bladeFlow,Query query) {
		IPage<SingleFlow> pages = flowBusinessService.selectDonePageByPersonId(bladeFlow, Condition.getPage(query));
		return R.data(pages);
	}

	/**
	 * 签收事务
	 *
	 * @param taskId 任务id
	 */
	@PostMapping("claim-task")
	@ApiOperationSupport(order = 7)
	@ApiOperation(value = "签收事务", notes = "传入流程信息")
	public R claimTask(@ApiParam("任务id") String taskId) {
		taskService.claim(taskId, TaskUtil.getTaskUser());
		return R.success("签收事务成功");
	}

//	*
//	 * 签收一条事务
//	 *
//
//	@PostMapping("claim-one-task")
//	@ApiOperationSupport(order = 8)
//	@ApiOperation(value = "签收事务", notes = "传入流程信息")
//	public R claimOneTask(@RequestBody BladeFlow bladeFlow) {
//		taskService.claim(bladeFlow.getTaskId(), TaskUtil.getTaskUser());
//		return R.success("签收事务成功");
//	}

	/**
	 * 完成任务
	 *
	 * @param flow 请假信息
	 */
	@PostMapping("complete-task")
	@ApiOperationSupport(order = 9)
	@ApiOperation(value = "完成任务", notes = "传入流程信息")
	public R completeTask(@ApiParam("任务信息") @RequestBody SingleFlow flow) {
		if (!flow.getStatus().equals("finish")) {
			return R.status(flowBusinessService.completeTask(flow));
		}else {
			return R.status(flowBusinessService.changeTaskComment(flow));
		}
	}

	/**
	 * 删除任务
	 *
	 * @param taskId 任务id
	 * @param reason 删除原因
	 */
	@PostMapping("delete-task")
	@ApiOperationSupport(order = 10)
	@ApiOperation(value = "删除任务", notes = "传入流程信息")
	public R deleteTask(@ApiParam("任务id") String taskId, @ApiParam("删除原因") String reason) {
		taskService.deleteTask(taskId, reason);
		return R.success("删除任务成功");
	}

}
