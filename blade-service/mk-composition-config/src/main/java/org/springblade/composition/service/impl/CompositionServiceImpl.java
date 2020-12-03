package org.springblade.composition.service.impl;

import org.springblade.composition.entity.Composition;
import org.springblade.composition.mapper.CompositionMapper;
import org.springblade.composition.service.CompositionService;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CompositionServiceImpl extends BaseServiceImpl<CompositionMapper, Composition> implements CompositionService {
}
