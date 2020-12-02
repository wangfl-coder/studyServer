package org.springblade.composition.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springblade.composition.entity.Composition;
import org.springblade.composition.mapper.CompositionMapper;
import org.springblade.composition.service.CompositionService;
import org.springframework.stereotype.Service;

@Service
public class CompositionServiceImpl extends ServiceImpl<CompositionMapper, Composition> implements CompositionService {
}
