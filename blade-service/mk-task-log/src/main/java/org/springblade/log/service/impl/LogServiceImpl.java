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
package org.springblade.log.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.log.entity.Log;
import org.springblade.log.mapper.LogMapper;
import org.springblade.log.service.ILogService;
import org.springblade.log.vo.LogVO;
import org.springframework.stereotype.Service;

/**
 *  服务实现类
 *
 * @author BladeX
 * @since 2021-03-23
 */
@Service
public class LogServiceImpl extends BaseServiceImpl<LogMapper, Log> implements ILogService {

	@Override
	public IPage<LogVO> selectLogPage(IPage<LogVO> page, LogVO log) {
		return page.setRecords(baseMapper.selectLogPage(page, log));
	}

}
