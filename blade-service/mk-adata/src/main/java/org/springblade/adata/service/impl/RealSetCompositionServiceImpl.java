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
package org.springblade.adata.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springblade.adata.entity.Expert;
import org.springblade.adata.entity.RealSetComposition;
import org.springblade.adata.magic.MagicRequest;
import org.springblade.adata.mapper.ExpertMapper;
import org.springblade.adata.mapper.RealSetCompositionMapper;
import org.springblade.adata.service.IExpertService;
import org.springblade.adata.service.IRealSetCompositionService;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.feign.ITemplateClient;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springblade.core.mp.support.Query;
import org.springblade.core.tool.support.Kv;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.StringUtil;
import org.springblade.flow.core.constant.ProcessConstant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务实现类
 *
 * @author Chill
 */
@Service
@AllArgsConstructor
public class RealSetCompositionServiceImpl extends BaseServiceImpl<RealSetCompositionMapper, RealSetComposition> implements IRealSetCompositionService {


}
