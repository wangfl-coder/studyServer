package org.springblade.mq.rabbit.handler;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.mq.rabbit.constant.RabbitConstant;
import org.springblade.mq.rabbit.message.MessageStruct;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 延迟队列处理器
 *
 * @author yangkai.shen
 */
@Slf4j
@Component
@RabbitListener(queues = RabbitConstant.DELAY_QUEUE)
public class DelayQueueHandler {

	@RabbitHandler
	public void directHandlerManualAck(MessageStruct messageStruct, Message message, Channel channel) {
		//  如果手动ACK,消息会被监听消费,但是消息在队列中依旧存在,如果 未配置 acknowledge-mode 默认是会在消费完毕后自动ACK掉
		final long deliveryTag = message.getMessageProperties().getDeliveryTag();
		try {
			log.info("延迟队列，手动ACK，接收消息：{}", JsonUtil.toJson(messageStruct));
			// 通知 MQ 消息已被成功消费,可以ACK了
			channel.basicAck(deliveryTag, false);
		} catch (IOException e) {
			try {
				// 处理失败,重新压入MQ
				channel.basicRecover();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
