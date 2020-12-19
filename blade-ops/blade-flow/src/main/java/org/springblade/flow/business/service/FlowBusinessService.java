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
import org.springblade.flow.core.entity.BladeFlow;
import org.springblade.flow.core.entity.SingleFlow;
import org.springblade.task.entity.Task;

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
	IPage<SingleFlow> selectClaimPage(IPage<SingleFlow> page, BladeFlow bladeFlow);


	/**
	 * 流程待签列表
	 *
	 * @return
	 */
	SingleFlow selectOneClaimPage(String categoryName);

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
	 * 完成任务
	 *
	 * @param leave 请假信息
	 * @return boolean
	 */
	boolean completeTask(BladeFlow leave);

	/**
	 * 修改任务评论
	 *
	 * @param flow 流程类
	 * @return boolean
	 */
	boolean changeTaskComment(BladeFlow flow);
}
