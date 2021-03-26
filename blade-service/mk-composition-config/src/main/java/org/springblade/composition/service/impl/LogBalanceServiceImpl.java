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
package org.springblade.composition.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.composition.entity.LogBalance;
import org.springblade.composition.mapper.LogBalanceMapper;
import org.springblade.composition.service.ILogBalanceService;
import org.springblade.composition.vo.LogBalanceVO;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 余额日志 服务实现类
 *
 * @author BladeX
 * @since 2021-03-04
 */
@Service
public class LogBalanceServiceImpl extends BaseServiceImpl<LogBalanceMapper, LogBalance> implements ILogBalanceService {

	@Override
	public IPage<LogBalanceVO> selectLogBalancePage(IPage<LogBalanceVO> page, LogBalanceVO logBalance) {
		return page.setRecords(baseMapper.selectLogBalancePage(page, logBalance));
	}

}
