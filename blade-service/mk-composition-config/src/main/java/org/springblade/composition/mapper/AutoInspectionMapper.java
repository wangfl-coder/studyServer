package org.springblade.composition.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springblade.composition.dto.AutoInspectionDTO;
import org.springblade.composition.entity.AutoInspection;

import java.util.List;

/**
 * AutoInspectionMapper 接口
 *
 * @author Chill
 */
public interface AutoInspectionMapper extends BaseMapper<AutoInspection> {

	/**
	 * 查询用户在一段时间内真题标注的数量和速度
	 * @param startTime
	 * @param endTime
	 * @param taskId
	 * @param userId
	 * @return
	 */
	List<AutoInspectionDTO> realSetCount(@Param("startTime")String startTime, @Param("endTime")String endTime, @Param("taskId")Long taskId, @Param("userId")Long userId);

	/**
	 * 查询用户在一段时间内真题标注的正确数量和错误数量
	 * @param startTime
	 * @param endTime
	 * @param taskId
	 * @param userId
	 * @param isCompositionTrue
	 * @return
	 */
	List<AutoInspectionDTO> realSetCorrectOrErrorCount(@Param("isCompositionTrue")Integer isCompositionTrue, @Param("startTime")String startTime, @Param("endTime")String endTime, @Param("taskId")Long taskId, @Param("userId")Long userId);
}
