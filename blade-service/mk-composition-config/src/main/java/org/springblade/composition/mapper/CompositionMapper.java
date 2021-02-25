package org.springblade.composition.mapper;

import com.baomidou.mybatisplus.annotation.SqlParser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springblade.composition.entity.Composition;

@Mapper
public interface CompositionMapper extends BaseMapper<Composition> {

	@SqlParser(filter = true)
	Composition getByIdIgnoreTenant(Long id);
}
