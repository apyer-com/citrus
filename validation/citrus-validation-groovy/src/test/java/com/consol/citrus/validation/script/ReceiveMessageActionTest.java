/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.validation.script;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.functions.DefaultFunctionLibrary;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageQueue;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.message.builder.DefaultPayloadBuilder;
import com.consol.citrus.message.builder.FileResourcePayloadBuilder;
import com.consol.citrus.message.builder.script.GroovyScriptPayloadBuilder;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.util.MessageUtils;
import com.consol.citrus.validation.DefaultMessageHeaderValidator;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.builder.DefaultMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.matcher.DefaultValidationMatcherLibrary;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionTest extends AbstractTestNGUnitTest {

    @Mock
    private Endpoint endpoint;
    @Mock
    private SelectiveConsumer consumer;
    @Mock
    private EndpointConfiguration endpointConfiguration;

    @Mock
    private MessageValidator<?> xmlMessageValidator;
    @Mock
    private MessageQueue mockQueue;

    @Override
    protected TestContextFactory createTestContextFactory() {
        MockitoAnnotations.openMocks(this);

        TestContextFactory factory = super.createTestContextFactory();
        factory.getFunctionRegistry().addFunctionLibrary(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().addValidationMatcherLibrary(new DefaultValidationMatcherLibrary());

        factory.getMessageValidatorRegistry().addMessageValidator("header", new DefaultMessageHeaderValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("groovyJson", new GroovyJsonMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("groovyText", new GroovyScriptMessageValidator());
        factory.getMessageValidatorRegistry().addMessageValidator("groovyXml", new GroovyXmlMessageValidator());

        factory.getReferenceResolver().bind("mockQueue", mockQueue);
        return factory;
    }

    @Test
    public void testReceiveMessageWithMessageBuilderScriptData() {

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        String markup = "markupBuilder.TestRequest(){\n" +
                "Message('Hello World!')\n" +
                "}";
        controlMessageBuilder.setPayloadBuilder(new GroovyScriptPayloadBuilder(markup));

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);

    }

    @Test
    public void testReceiveMessageWithMessageBuilderScriptDataVariableSupport() {
        context.setVariable("text", "Hello World!");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        String markup = "markupBuilder.TestRequest(){\n" +
                "Message('${text}')\n" +
                "}";
        controlMessageBuilder.setPayloadBuilder(new GroovyScriptPayloadBuilder(markup));

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);

    }

    @Test
    public void testReceiveMessageWithMessageBuilderScriptResource() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new FileResourcePayloadBuilder("classpath:com/consol/citrus/actions/test-request-payload.groovy"));

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);

    }

    @Test
    public void testReceiveMessageWithValidationScript() {
        ScriptValidationContext validationContext = new ScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .script("assert root.Message.name() == 'Message'\n" +
                        "assert root.Message.text() == 'Hello World!'")
                .build();

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .validator(new GroovyXmlMessageValidator())
                .validate(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    public void testReceiveMessageWithValidationScriptResource() {
        ScriptValidationContext validationContext = new ScriptValidationContext.Builder()
                .scriptType(ScriptTypes.GROOVY)
                .scriptResource("classpath:com/consol/citrus/actions/test-validation-script.groovy")
                .build();

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .validator(new GroovyXmlMessageValidator())
                .validate(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    public void testInjectedMessageValidators() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        context.getMessageValidatorRegistry().addMessageValidator("xml", xmlMessageValidator);
        when(xmlMessageValidator.supportsMessageType(any(String.class), any(Message.class)))
                .thenAnswer(invocation -> invocation.getArgument(0).equals(MessageType.XML.name())
                        && MessageUtils.hasXmlPayload(invocation.getArgument(1)));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);

        // now inject multiple validators
        Map<String, MessageValidator<? extends ValidationContext>> validators = new HashMap<>();
        validators.put("xml", xmlMessageValidator);
        validators.put("groovyXml", new GroovyXmlMessageValidator());

        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();
        messageValidatorRegistry.setMessageValidators(validators);
        context.setMessageValidatorRegistry(messageValidatorRegistry);

        ScriptValidationContext scriptValidationContext = new ScriptValidationContext(CitrusSettings.DEFAULT_MESSAGE_TYPE);

        receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(scriptValidationContext)
                .build();
        receiveAction.execute(context);

        verify(xmlMessageValidator, times(2))
                .validateMessage(any(Message.class), any(Message.class), eq(context), any(List.class));

    }
}
