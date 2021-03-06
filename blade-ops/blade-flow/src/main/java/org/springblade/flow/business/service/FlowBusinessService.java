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
package org.springblade.flow.business.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.entity.SingleFlow;
import org.springblade.task.vo.CompositionClaimCountVO;

import java.util.List;

/**
 * 流程业务类
 *
 * @author Chill
 */
public interface FlowBusinessService {

	/**
	 * 流程待签列表
	 *
	 * @param page      分页工具
	 * @param bladeFlow 流程类
	 * @return
	 */
	IPage<SingleFlow> selectClaimPage(IPage<SingleFlow> page, SingleFlow bladeFlow);


	/**
	 * 流程待签列表
	 *
	 * @return
	 */
	SingleFlow selectOneClaimPage(String categoryName, Long roleId, Long compositionId);

	/**
	 * 流程待办列表
	 *
	 * @param page      分页工具
	 * @param bladeFlow 流程类
	 * @return
	 */
	IPage<SingleFlow> selectTodoPage(IPage<SingleFlow> page, BladeFlow bladeFlow);

	/**
	 * 流程已发列表
	 *
	 * @param page      分页工具
	 * @param bladeFlow 流程类
	 * @return
	 */
	IPage<SingleFlow> selectSendPage(IPage<SingleFlow> page, BladeFlow bladeFlow);

	/**
	 * 流程办结列表
	 *
	 * @param page      分页工具
	 * @param bladeFlow 流程类
	 * @return
	 */
	IPage<SingleFlow> selectDonePage(IPage<SingleFlow> page, BladeFlow bladeFlow);

	/**
	 * 根据专家id查询办结事务
	 *
	 * @param bladeFlow 流程类
	 * @return
	 */
	IPage<SingleFlow> selectDonePageByPersonId(BladeFlow bladeFlow,IPage<SingleFlow> page);

	/**
	 * 完成任务
	 *
	 * @param leave 请假信息
	 * @return boolean
	 */
	boolean completeTask(SingleFlow leave);

	/**
	 * 修改任务评论
	 *
	 * @param flow 流程类
	 * @return boolean
	 */
	boolean changeTaskComment(BladeFlow flow);

	/**
	 * 修改流程实例中所有任务的优先级
	 *
	 * @param processInstanceId 流程实例Id
	 * @param priority 			优先级
	 * @return boolean
	 */
	boolean setTaskPriorityByProcessInstanceId(String processInstanceId, int priority);

	/**
	 * 修改流程实例中所有任务的优先级
	 *
	 * @param processInstanceIds 流程实例Id列表
	 * @param priority 			 优先级
	 * @return boolean
	 */
	boolean setTaskPriorityByProcessInstanceIds(List<String> processInstanceIds, int priority);

	/**
	 * 定时将超时待办返回待签
	 *
	 * @return R
	 */
	boolean todoTimeoutHandler();

	/**
	 * 返回当前用户所有组合及分别可接的组合数
	 * @param roleAlias  角色列表
	 * @param userId  用户Id
	 * @return
	 */
	List<CompositionClaimCountVO> getCompositionClaimCountByRoleAlias(List<String> roleAlias, Long userId);

	/**
	 * 返回当前用户所有组合及分别可接的组合数
	 * @param roleAlias  角色列表
	 * @param userId  用户Id
	 * @return
	 */
	String claimOneByCompositionId(List<String> roleAlias, Long userId, Long compositionId);
}
