package org.springblade.composition.feign;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springblade.composition.entity.AnnotationDataErrata;
import org.springblade.composition.service.AnnotationDataErrataService;
import org.springblade.core.tool.api.R;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
public class AnnotationDataErrataClient implements IAnnotationDataErrataClient{
	private AnnotationDataErrataService annotationDataErrataService;



	@Override
	public R<Boolean> ifNeedUpdateAnnotationDataErrataStatusAndValue(Long compositionId, Long subTaskId, Long userId, String field, Map<String,String> fieldValues, int status) {
		Boolean flag;
		LambdaUpdateWrapper<AnnotationDataErrata> updateWrapper = Wrappers.lambdaUpdate();
		updateWrapper.eq(AnnotationDataErrata::getCompositionId, compositionId)
			.eq(AnnotationDataErrata::getSubTaskId, subTaskId)
			.eq(AnnotationDataErrata::getLabelerId,userId)
			// 1。未申述 2。已申述 3。申述成功 4。申述失败
			.eq(AnnotationDataErrata::getField, field);
		if (status == 3){
			String fieldValue = fieldValues.get("fillValue");
			updateWrapper.set(AnnotationDataErrata::getStatus,status).set(AnnotationDataErrata::getValue,fieldValue);
			flag = annotationDataErrataService.update(updateWrapper);
			return R.data(flag);
		}else {
			updateWrapper.set(AnnotationDataErrata::getStatus,status);
			flag = annotationDataErrataService.update(updateWrapper);
			return R.data(flag);
		}

	}

	@Override
	public Boolean queryAnnotationDataErrataStatus(Long compositionId, Long subTaskId, Long userId) {
		LambdaQueryWrapper<AnnotationDataErrata> queryWrapper = Wrappers.lambdaQuery();
		queryWrapper.eq(AnnotationDataErrata::getCompositionId,compositionId)
			.eq(AnnotationDataErrata::getSubTaskId,subTaskId)
			.eq(AnnotationDataErrata::getLabelerId,userId);
		List<AnnotationDataErrata> annotationDataErrataList =  annotationDataErrataService.list(queryWrapper);
		Boolean flag = true;
		if (annotationDataErrataList != null){
			for (AnnotationDataErrata annotationDataErrata : annotationDataErrataList){
				if (annotationDataErrata.getStatus() != 3){
					flag = false;
				}
			}
		}
		return flag;
	}
}
