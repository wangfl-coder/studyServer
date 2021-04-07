package org.springblade.composition.feign;

import org.springblade.common.constant.LauncherConstant;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.support.Kv;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = LauncherConstant.MKAPP_COMPOSITION_CONFIG_NAME)
public interface IAnnotationDataErrataClient {
	String API_PREFIX = "/client";
	String IF_NEED_UPDATE_ANNOTATION_DATA_ERRATA_STATUS_VALUE = API_PREFIX + "/mk-composition-config/if-need-update-annotation-data-errata-status-value";
	String QUERY_ANNOTATION_DATA_ERRATA_STATUS = API_PREFIX + "/mk-composition-config/query-annotation-data-errata-status";

	/**
	 * 更新AnnotationDataErrta表的字段value和status
	 */
	@PostMapping(IF_NEED_UPDATE_ANNOTATION_DATA_ERRATA_STATUS_VALUE)
	R<Boolean> ifNeedUpdateAnnotationDataErrataStatusAndValue(@RequestParam Long compositionId, @RequestParam Long subTaskId, @RequestParam Long userId, @RequestParam String field, @RequestParam Map<String,String> fieldValues, @RequestParam(required = false) int status);

	/**
	 * 查询AnnotationDataErrata的Status
	 * @param compositionId
	 * @param subTaskId
	 * @param userId
	 * @return
	 */
	@GetMapping(QUERY_ANNOTATION_DATA_ERRATA_STATUS)
	Boolean queryAnnotationDataErrataStatus(@RequestParam Long compositionId, @RequestParam Long subTaskId, @RequestParam Long userId);
}
