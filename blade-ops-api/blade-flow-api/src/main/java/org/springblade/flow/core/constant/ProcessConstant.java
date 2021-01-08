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
package org.springblade.flow.core.constant;

/**
 * 流程常量.
 *
 * @author Chill
 */
public interface ProcessConstant {

	/**
	 * 请假流程标识
	 */
	String LEAVE_KEY = "Leave";

	/**
	 * 报销流程标识
	 */
	String EXPENSE_KEY = "Expense";

	/**
	 * 标注流程标识
	 */
	String LABEL_KEY = "Label";

	/**
	 * 质检流程标识
	 */
	String QUALITY_INSPECTION_KEY = "Inspection";

	/**
	 * 同意标识
	 */
	String PASS_KEY = "pass";

	/**
	 * 同意代号
	 */
	String PASS_ALIAS = "ok";

	/**
	 * 同意默认批复
	 */
	String PASS_COMMENT = "同意";

	/**
	 * 驳回默认批复
	 */
	String NOT_PASS_COMMENT = "驳回";

	/**
	 * 创建人变量名
	 */
	String TASK_VARIABLE_CREATE_USER = "createUser";

	/**
	 * 主页是否完整标识
	 */
	String HOMEPAGE_FOUND_KEY = "isHomepageFound";

	/**
	 * 基本信息是否完整标识
	 */
	String BASICINFO_COMPLETE_KEY = "isBiComplete";

	/**
	 * 组合ID
	 */
	String COMPOSITION_ID = "compositionId";

	/**
	 * 组合类型
	 */
	String COMPOSITION_TYPE = "compositionType";

	/**
	 * 组合字段
	 */
	String COMPOSITION_FIELD = "compositionField";
}
