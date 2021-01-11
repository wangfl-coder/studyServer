package org.springblade.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springblade.task.vo.ExpertLabelTaskVO;
import org.springblade.task.entity.LabelTask;

import java.util.List;

public interface LabelTaskMapper extends BaseMapper<LabelTask> {

	/**
	 * 已完成子任务数
	 *
	 * @param env    	运行环境
	 * @param taskId    任务Id
	 * @return
	 */
	int completeCount(String env, Long taskId);

	/**
	 * 根据专家id查对应子任务流程实例id
	 * @param expertId    专家真正的id
	 * @return
	 */
	List<ExpertLabelTaskVO> personIdToProcessInstance(String expertId);

	/**
	 * 查询用户标注任务完成数量
	 * @param env      环境
	 * @param param2  用户名
	 * @return
	 */
	long annotationDoneCount(String env,String param2);

	/**
	 * 查询用户标注任务待办数量
	 * @param env      环境
	 * @param param2  用户名
	 * @return
	 */
	long annotationTodoCount(String env,String param2);

	/**
	 * 查询用户标注任务待签数量
	 * @param env      环境
	 * @param param2  用户名
	 * @return
	 */
	int annotationClaimCount(String env, List<String> param2);

}
