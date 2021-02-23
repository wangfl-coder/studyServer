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
package org.springblade.adata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springblade.adata.entity.ExpertExtend;
import org.springblade.core.mp.base.BaseService;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 服务类
 *
 * @author Chill
 */
public interface IExpertExtendService<T> extends IService<T> {

//	/**
//	 * 获取参数值
//	 *
//	 * @param paramKey 参数key
//	 * @return String
//	 */
//	String getValue(String paramKey);

	/**
	 * 逻辑删除
	 *
	 * @param ids id集合
	 * @return
	 */
	boolean deleteLogic(@NotEmpty List<String> ids);

	/**
	 * 变更状态
	 *
	 * @param ids    id集合
	 * @param status 状态值
	 * @return
	 */
	boolean changeStatus(@NotEmpty List<String> ids, Integer status);


	boolean saveOrUpdateToAminer(T entity);
}
