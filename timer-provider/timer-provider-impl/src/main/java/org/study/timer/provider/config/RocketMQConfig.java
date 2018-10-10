package org.study.timer.provider.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.study.common.mq.NameServerAddress;
import org.study.common.mq.producer.Producer;

@SpringBootConfiguration
@ConfigurationProperties(prefix = "config.rocketmq")
public class RocketMQConfig {
	private String nameServerAddress;
	private String producerGroupName;

	public String getNameServerAddress() {
		return nameServerAddress;
	}

	public void setNameServerAddress(String nameServerAddress) {
		this.nameServerAddress = nameServerAddress;
	}

	public String getProducerGroupName() {
		return producerGroupName;
	}

	public void setProducerGroupName(String producerGroupName) {
		this.producerGroupName = producerGroupName;
	}

	@Bean
	public NameServerAddress nameServerAddress() {
		NameServerAddress nameServerAddress = new NameServerAddress();
		nameServerAddress.setAddresses(getNameServerAddress());
		return nameServerAddress;
	}

	/**
	 * 消息发送者
	 *
	 * @return
	 */
	@Bean
	public Producer producer() {
		Producer producer = new Producer();
		producer.setNameServerAddress(nameServerAddress());
		producer.setGroupName(getProducerGroupName());
		return producer;
	}
}
