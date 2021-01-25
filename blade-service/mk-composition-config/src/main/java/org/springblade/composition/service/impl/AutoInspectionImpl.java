package org.springblade.composition.service.impl;


import org.springblade.composition.entity.AutoInspection;
import org.springblade.composition.mapper.AutoInspectionMapper;
import org.springblade.composition.service.IAutoInspectionService;
import org.springblade.core.mp.base.BaseServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AutoInspectionImpl extends BaseServiceImpl<AutoInspectionMapper, AutoInspection> implements IAutoInspectionService {
}
