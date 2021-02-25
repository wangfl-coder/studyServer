package org.springblade.composition.service.impl;

import org.springblade.composition.entity.Composition;
import org.springblade.composition.mapper.CompositionMapper;
import org.springblade.composition.service.ICompositionService;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ICompositionServiceImpl extends BaseServiceImpl<CompositionMapper, Composition> implements ICompositionService {

	public Composition getByIdIgnoreTenant(Long id) {
		return baseMapper.getByIdIgnoreTenant(id);
	}
}
