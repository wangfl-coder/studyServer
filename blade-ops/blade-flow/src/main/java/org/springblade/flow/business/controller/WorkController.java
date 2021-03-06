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
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springblade.adata.feign.IExpertClient;
import org.springblade.common.cache.CacheNames;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.redis.cache.BladeRedis;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tenant.annotation.NonDS;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.flow.business.service.FlowBusinessService;
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.entity.SingleFlow;
import org.springblade.flow.core.utils.TaskUtil;
import org.springblade.flow.engine.entity.FlowProcess;
import org.springblade.flow.engine.service.FlowEngineService;
import org.springblade.system.cache.SysCache;
import org.springblade.system.user.cache.UserCache;
import org.springblade.system.user.entity.User;
import org.springblade.system.user.enums.UserStatusEnum;
import org.springblade.system.user.feign.IUserClient;
import org.springblade.task.feign.ILabelTaskClient;
import org.springblade.task.feign.ITaskClient;
import org.springblade.task.vo.CompositionClaimCountVO;
import org.springblade.task.vo.CompositionClaimListVO;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

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
	private final RuntimeService runtimeService;
	private final FlowEngineService flowEngineService;
	private final FlowBusinessService flowBusinessService;
	private final ITaskClient taskClient;
	private final ILabelTaskClient labelTaskClient;
	private final IExpertClient expertClient;
	private final BladeRedis bladeRedis;
	private final IUserClient userClient;


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
		User user = UserCache.getUser(AuthUtil.getUserId());
		if (user.getStatus().equals(UserStatusEnum.BLOCKED.getNum())) {
			return R.fail("错误率过高，无法接任务，请联系管理员");
		}
		String redisCode = this.bladeRedis.get(CacheNames.FLOW_CLAIMONE_KEY + AuthUtil.getUserId());
		if (redisCode != null) {
			return R.fail("领取任务过快");
		}
		String uuid = StringUtil.randomUUID();
		bladeRedis.setEx(CacheNames.FLOW_CLAIMONE_KEY + AuthUtil.getUserId(), uuid, Duration.ofSeconds(1));
		SingleFlow flow = flowBusinessService.selectOneClaimPage(categoryName, roleId, compositionId);
		if(flow.getTaskId()!=null){
			// 判断是否出现真题,主页，补充信息，含有bio,bioZh,work,edu等基本信息字段的组合没有真题。其他情况通过掺入比例依概率产生真题。
//			String compositionField = flow.getCompositionField();
//			if (compositionField != null && flow.getCompositionType() == 2 && !StringUtil.containsAny(compositionField,"bio")
//				&& !StringUtil.containsAny(compositionField,"bio") && !StringUtil.containsAny(compositionField,"edu")
//				&& !StringUtil.containsAny(compositionField,"work")) {
//				// 获取到要领取的是哪个任务，查询出任务的掺入真题比例。
//				Expert expert = new Expert();
//				expert.setId(flow.getPersonId());
//				Expert expertDetail = expertClient.detail(expert).getData();
//				Task task = taskClient.getById(expertDetail.getTaskId()).getData();
//				Random r = new Random();
//				int randomResult = r.nextInt(100);
//				if (randomResult < task.getRealSetRate()) {
//					// 每次产生真题，随机从任务绑定的真题库中抽取一个真题。把真题的主页相关信息，例如hp,name等返回给前端。
//					List<RealSetExpert> realSetExpertList = realSetExpertClient.getExpertIds(task.getId()).getData();
//					RealSetExpert realSetExpert = realSetExpertList.get(r.nextInt(realSetExpertList.size()));
//					flow.setPersonId(realSetExpert.getId());
//					flow.setPersonName(realSetExpert.getName());
//					flow.setExpertId(realSetExpert.getExpertId());
//					flow.setHomepage(realSetExpert.getHomepage());
//					flow.setHp(realSetExpert.getHp());
//					flow.setGs(realSetExpert.getGs());
//					flow.setDblp(realSetExpert.getDblp());
//					flow.setPersonNameZh(realSetExpert.getNameZh());
//					flow.setOtherHomepage(realSetExpert.getOtherHomepage());
//					Long timestamp=System.currentTimeMillis();
//					flow.setTaskId(String.valueOf(timestamp));
//					// 前端通过这个标志是1判断是真题
//					flow.setIsRealSet(1);
//					return R.data(flow);
//				}
//			}

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
			try {
				return R.status(flowBusinessService.completeTask(flow));
			} catch (FlowableObjectNotFoundException e) {
				return R.fail(e.getMessage());
			}
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

	@GetMapping("/composition-claim-count")
	@ApiOperation(value = "返回当前用户所有组合及分别可接的任务数")
	public R<List<CompositionClaimCountVO>> compositionClaimCount(BladeUser user) {
		List<String> roleAlias = SysCache.getRoleAliases(user.getRoleId());
		List<CompositionClaimCountVO> result = flowBusinessService.getCompositionClaimCountByRoleAlias(roleAlias, AuthUtil.getUserId());
		return R.data(result);
//		R<List<CompositionClaimListVO>> res = labelTaskClient.compositionClaimList(roleAlias);
//		if (res.isSuccess()) {
//			Map<String, Object> processVariablesMap = new HashMap<>();
//
//			List<CompositionClaimListVO> list = res.getData();
//			List<CompositionClaimListVO> resList = new ArrayList<>();
//			for(CompositionClaimListVO compositionClaimListVO: list) {
//				String processInstanceId = compositionClaimListVO.getProcessInstanceId();
//				Map<String, Object> processVariables = (Map<String, Object>)processVariablesMap.get(processInstanceId);
//				if (processVariables == null) {
//					try {
//						processVariables = runtimeService.getVariables(processInstanceId);
//						processVariablesMap.put(processInstanceId, processVariables);
//					}catch(FlowableObjectNotFoundException e){
//						e.printStackTrace();
//					}
//				}
//				if (processVariables != null && processVariables.containsKey(compositionClaimListVO.getName()+"-"+ AuthUtil.getUserId()+"-done")) {
//					continue;
//				}else {
//					resList.add(compositionClaimListVO);
//				}
//			}
//			Map<String, List<CompositionClaimListVO>> dataPerComposition = resList.stream()
//				.collect(groupingBy(CompositionClaimListVO::getCompositionId));
//			List<CompositionClaimCountVO> result = new ArrayList<>();
//			for (Map.Entry<String, List<CompositionClaimListVO>> entry : dataPerComposition.entrySet()) {
//				CompositionClaimCountVO compositionClaimCountVO = new CompositionClaimCountVO();
//				compositionClaimCountVO.setCompositionId(entry.getKey());
//				compositionClaimCountVO.setName(entry.getValue().get(0).getName());
//				compositionClaimCountVO.setCount(entry.getValue().size());
//				result.add(compositionClaimCountVO);
//			}
//			return R.data(result);
//		} else {
//			return R.fail("获取组合信息错误");
//		}
	}
}
