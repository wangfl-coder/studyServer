package org.springblade.mq.rabbit.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import jodd.util.ThreadUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.secure.utils.AuthUtil;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.mq.rabbit.constant.RabbitConstant;
import org.springblade.mq.rabbit.entity.PurePerson;
import org.springblade.mq.rabbit.entity.SupPerson;
import org.springblade.mq.rabbit.magic.MagicRequest;
import org.springblade.mq.rabbit.message.MessageStruct;
import org.springblade.mq.rabbit.service.IPurePersonService;
import org.springblade.mq.rabbit.service.ISupPersonService;
import org.springblade.task.enums.MergeExpertTaskStatusEnum;
import org.springblade.task.feign.IMergeExpertTaskClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 直接队列1 处理器
 *
 * @author yangkai.shen
 */
@Slf4j
@RabbitListener(queues = RabbitConstant.DIRECT_MODE_QUEUE_PREPROCESS_PURE_SUP_PERSON)
@Component
@AllArgsConstructor
public class PreprocessPureSupHandler {

	private final IPurePersonService purePersonService;
	private final ISupPersonService supPersonService;
	private final IMergeExpertTaskClient mergeExpertTaskClient;

	/**
	 * 如果 spring.rabbitmq.listener.direct.acknowledge-mode: auto，则可以用这个方式，会自动ack
	 */
	// @RabbitHandler
	public void directHandlerAutoAck(MessageStruct message) {
		log.info("直接队列处理器，接收消息：{}", JsonUtil.toJson(message));
	}

	@RabbitHandler
	public void directHandlerManualAck(MessageStruct messageStruct, Message message, Channel channel) {
		//  如果手动ACK,消息会被监听消费,但是消息在队列中依旧存在,如果 未配置 acknowledge-mode 默认是会在消费完毕后自动ACK掉
		final long deliveryTag = message.getMessageProperties().getDeliveryTag();
		try {
			log.info("pure.sup.person队列，手动ACK，接收消息：{}", JsonUtil.toJson(messageStruct));
//			ThreadUtil.sleep(10000);
			String msg = messageStruct.getMessage();
			List<String> idList = Arrays.asList(msg.split(","));
			String taskId = idList.get(0);
			String mergeTaskId = idList.get(1);
			String expertId = idList.get(2);
			String personId = idList.get(3);
			String pureRes = pureOnePerson(expertId);
			//解析json,拿到每个学者的id
			JSONObject resObj = JSON.parseObject(pureRes);
			JSONArray dataArray = resObj.getJSONArray("data");
			JSONObject dataRes = dataArray.getJSONObject(0);
			boolean importSuccess = dataRes.getBoolean("succeed");
			if (!importSuccess) {
				throw new Exception();
			}
			JSONArray itemArray = dataRes.getJSONArray("items");
			if (itemArray != null) {
				JSONObject pureDataRes = itemArray.getJSONObject(0);
				JSONObject error_names = pureDataRes.getJSONObject("error_names");
				JSONArray need_repair_papers = pureDataRes.getJSONArray("need_repair_papers");
				JSONArray papers_to_person = pureDataRes.getJSONArray("papers_to_person");
				JSONArray remove_direct = pureDataRes.getJSONArray("remove_direct");
				JSONArray remove_from_person = pureDataRes.getJSONArray("remove_from_person");
				PurePerson purePerson = new PurePerson();
				if (error_names != null)
					purePerson.setErrorNames(error_names.toString());
				if (need_repair_papers != null)
					purePerson.setNeedRepairPapers(need_repair_papers.toString());
				if (papers_to_person != null)
					purePerson.setPapersToPerson(papers_to_person.toString());
				if (remove_direct != null)
					purePerson.setRemoveDirect(remove_direct.toString());
				if (remove_from_person != null)
					purePerson.setRemoveFromPerson(remove_from_person.toString());
				purePerson.setTaskId(Long.valueOf(taskId));
				purePerson.setMergeTaskId(Long.valueOf(mergeTaskId));
				purePerson.setExpertId(expertId);
				purePerson.setPersonId(Long.valueOf(personId));
				purePersonService.save(purePerson);
			}

			String supRes = supOnePerson(expertId);
			//解析json,拿到每个学者的id
			JSONObject resObj2 = JSON.parseObject(supRes);
			JSONArray dataArray2 = resObj2.getJSONArray("data");
			JSONObject dataRes2 = dataArray2.getJSONObject(0);
			boolean importSuccess2 = dataRes2.getBoolean("succeed");
			if (!importSuccess2) {
				throw new Exception();
			}
			JSONObject keyValues = dataRes.getJSONObject("keyValues");
			if (keyValues != null) {
				JSONObject papers = keyValues.getJSONObject("papers");
				JSONObject persons = keyValues.getJSONObject("persons");
				SupPerson supPerson = new SupPerson();
				if (papers != null)
					supPerson.setPapers(papers.toString());
				if (persons != null)
					supPerson.setPersons(persons.toString());
				supPerson.setTaskId(Long.valueOf(taskId));
				supPerson.setMergeTaskId(Long.valueOf(mergeTaskId));
				supPerson.setExpertId(expertId);
				supPerson.setPersonId(Long.valueOf(personId));
				supPersonService.save(supPerson);

				for (Map.Entry<String,Object> entry : persons.entrySet()) {
					String expert_id = entry.getKey();            //	expert_id
					Integer score = (Integer)entry.getValue();    //	score

				}
			}

			mergeExpertTaskClient.changeStatus(Long.valueOf(mergeTaskId), MergeExpertTaskStatusEnum.PURE_SUPED.getNum());
			// 通知 MQ 消息已被成功消费,可以ACK了
			channel.basicAck(deliveryTag, false);
		} catch (Exception e) {
			try {
				// 处理失败,重新压入MQ
				//channel.basicRecover();
				e.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	private String pureOnePerson(String id) {
		JSONArray requestBody = new JSONArray();
		JSONObject body = new JSONObject();

		JSONObject parameters = new JSONObject();
		parameters.put("id", id);
		parameters.put("dl_check", false);

		body.put("action", "na.PureOnePerson");
		body.put("parameters", parameters);
		requestBody.add(body);
		String res = MagicRequest.getInstance().magic(requestBody.toString());
		return res;
	}

	private String supOnePerson(String id) {
		JSONArray requestBody = new JSONArray();
		JSONObject body = new JSONObject();

		JSONObject parameters = new JSONObject();
		parameters.put("id", id);

		body.put("action", "na.SupOnePerson");
		body.put("parameters", parameters);
		requestBody.add(body);
		String res = MagicRequest.getInstance().magic(requestBody.toString());
		return res;
	}
}
