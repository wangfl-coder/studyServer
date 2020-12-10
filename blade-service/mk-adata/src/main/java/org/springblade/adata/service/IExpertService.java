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

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.adata.entity.Expert;
import org.springblade.core.mp.base.BaseService;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.api.R;


import java.util.Map;

/**
 * 服务类
 *
 * @author Chill
 */
public interface IExpertService extends BaseService<Expert> {

	/**
	 * 详情
	 * @param id
	 * @return
	 */
	R<String> fetchDetail(String id);

	/**
	 * 列表
	 * @param params
	 * @param query
	 * @return
	 */
	R<String> fetchList(Map<String, Object> params, Query query);

	/**
	 * 导入
	 * @param id
	 * @return
	 */
	R<String> importDetail(String id);
}
