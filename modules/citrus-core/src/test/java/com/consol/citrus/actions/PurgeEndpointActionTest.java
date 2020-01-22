/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.actions;

import java.util.Collections;

import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.ActionTimeoutException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class PurgeEndpointActionTest extends AbstractTestNGUnitTest {

    @Autowired
    @Qualifier(value="mockEndpoint")
    private Endpoint mockEndpoint;

    private Endpoint emptyEndpoint = Mockito.mock(Endpoint.class);

    private Consumer consumer = Mockito.mock(Consumer.class);
    private SelectiveConsumer selectiveConsumer = Mockito.mock(SelectiveConsumer.class);

    @Test
    public void testPurgeWithEndpointNames() {
        reset(mockEndpoint, consumer, selectiveConsumer);

        when(mockEndpoint.getName()).thenReturn("mockEndpoint");
        when(mockEndpoint.createConsumer()).thenReturn(consumer);
        when(consumer.receive(context, 100L)).thenReturn(new DefaultMessage());
        doThrow(new ActionTimeoutException()).when(consumer).receive(context, 100L);

        PurgeEndpointAction purgeEndpointAction = new PurgeEndpointAction.Builder()
                .beanFactory(applicationContext)
                .endpointNames("mockEndpoint")
                .build();
        purgeEndpointAction.execute(context);
    }

	@SuppressWarnings("unchecked")
    @Test
    public void testPurgeWithEndpointObjects() {
        reset(mockEndpoint, emptyEndpoint, consumer, selectiveConsumer);

        when(mockEndpoint.getName()).thenReturn("mockEndpoint");
        when(emptyEndpoint.getName()).thenReturn("emptyEndpoint");
        when(mockEndpoint.createConsumer()).thenReturn(consumer);
        when(emptyEndpoint.createConsumer()).thenReturn(consumer);

        when(consumer.receive(context, 100L)).thenReturn(new DefaultMessage());
        doThrow(new ActionTimeoutException()).when(consumer).receive(context, 100L);
        doThrow(new ActionTimeoutException()).when(consumer).receive(context, 100L);

        PurgeEndpointAction purgeEndpointAction = new PurgeEndpointAction.Builder()
                .beanFactory(applicationContext)
                .endpoints(mockEndpoint, emptyEndpoint)
                .build();
        purgeEndpointAction.execute(context);
    }

	@Test
    public void testPurgeWithMessageSelector() throws Exception {
        reset(mockEndpoint, consumer, selectiveConsumer);

        when(mockEndpoint.getName()).thenReturn("mockEndpoint");
        when(mockEndpoint.createConsumer()).thenReturn(selectiveConsumer);
        when(selectiveConsumer.receive("operation = 'sayHello'", context, 100L)).thenReturn(new DefaultMessage());
        doThrow(new ActionTimeoutException()).when(selectiveConsumer).receive("operation = 'sayHello'", context, 100L);

        PurgeEndpointAction purgeEndpointAction = new PurgeEndpointAction.Builder()
                .beanFactory(applicationContext)
                .endpoints(mockEndpoint)
                .selector("operation = 'sayHello'")
                .build();
        purgeEndpointAction.execute(context);
    }

    @Test
    public void testPurgeWithMessageSelectorMap() throws Exception {
        reset(mockEndpoint, consumer, selectiveConsumer);

        when(mockEndpoint.getName()).thenReturn("mockEndpoint");
        when(mockEndpoint.createConsumer()).thenReturn(selectiveConsumer);
        when(selectiveConsumer.receive("operation = 'sayHello'", context, 100L)).thenReturn(new DefaultMessage());
        doThrow(new ActionTimeoutException()).when(selectiveConsumer).receive("operation = 'sayHello'", context, 100L);

        PurgeEndpointAction purgeEndpointAction = new PurgeEndpointAction.Builder()
                .beanFactory(applicationContext)
                .endpoints(mockEndpoint)
                .selector(Collections.singletonMap("operation", "sayHello"))
                .build();
        purgeEndpointAction.execute(context);
    }

}
