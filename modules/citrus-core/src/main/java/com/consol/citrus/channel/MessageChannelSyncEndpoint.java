/*
 * Copyright 2006-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.channel;

import com.consol.citrus.message.ReplyMessageCorrelator;
import com.consol.citrus.messaging.*;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class MessageChannelSyncEndpoint extends MessageChannelEndpoint {

    /** One of producer or consumer for this endpoint */
    private MessageChannelSyncProducer messageChannelSyncProducer;
    private MessageChannelSyncConsumer messageChannelSyncConsumer;

    public MessageChannelSyncEndpoint() {
        super(new MessageChannelSyncEndpointConfiguration());
    }

    @Override
    public MessageChannelSyncEndpointConfiguration getEndpointConfiguration() {
        return (MessageChannelSyncEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public SelectiveConsumer createConsumer() {
        if (messageChannelSyncProducer != null) {
            return messageChannelSyncProducer;
        }

        if (messageChannelSyncConsumer == null) {
            messageChannelSyncConsumer = new MessageChannelSyncConsumer(getEndpointConfiguration());
        }

        return messageChannelSyncConsumer;
    }

    @Override
    public Producer createProducer() {
        if (messageChannelSyncConsumer != null) {
            return messageChannelSyncConsumer;
        }

        if (messageChannelSyncProducer == null) {
            messageChannelSyncProducer = new MessageChannelSyncProducer(getEndpointConfiguration());
        }

        return messageChannelSyncProducer;
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        getEndpointConfiguration().setCorrelator(correlator);
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return getEndpointConfiguration().getCorrelator();
    }

    /**
     * Gets the pollingInterval.
     * @return the pollingInterval the pollingInterval to get.
     */
    public long getPollingInterval() {
        return getEndpointConfiguration().getPollingInterval();
    }

    /**
     * Sets the pollingInterval.
     * @param pollingInterval the pollingInterval to set
     */
    public void setPollingInterval(long pollingInterval) {
        getEndpointConfiguration().setPollingInterval(pollingInterval);
    }

}
