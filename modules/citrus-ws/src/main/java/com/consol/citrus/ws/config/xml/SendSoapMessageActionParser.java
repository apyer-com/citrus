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

package com.consol.citrus.ws.config.xml;

import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.config.xml.AbstractSendMessageActionFactoryBean;
import com.consol.citrus.config.xml.SendMessageActionParser;
import com.consol.citrus.validation.builder.AbstractMessageContentBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.ws.actions.SendSoapMessageAction;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessageHeaders;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Parser for SOAP message sender component in Citrus ws namespace.
 *
 * @author Christoph Deppisch
 */
public class SendSoapMessageActionParser extends SendMessageActionParser {

    @Override
    public BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = super.parseComponent(element, parserContext);

        List<Element> attachmentElements = DomUtils.getChildElementsByTagName(element, "attachment");
        List<SoapAttachment> attachments = new ArrayList<SoapAttachment>();
        for (Element attachment : attachmentElements) {
            attachments.add(SoapAttachmentParser.parseAttachment(attachment));
        }

        builder.addPropertyValue("attachments", attachments);

        if (element.hasAttribute("mtom-enabled")) {
            builder.addPropertyValue("mtomEnabled", element.getAttribute("mtom-enabled"));
        }

        return builder;
    }

    @Override
    protected void parseHeaderElements(Element actionElement, AbstractMessageContentBuilder messageBuilder, List<ValidationContext> validationContexts) {
        super.parseHeaderElements(actionElement, messageBuilder, validationContexts);

        if (actionElement.hasAttribute("soap-action")) {
            messageBuilder.getMessageHeaders().put(SoapMessageHeaders.SOAP_ACTION, actionElement.getAttribute("soap-action"));
        }

        if (actionElement.hasAttribute("content-type")) {
            messageBuilder.getMessageHeaders().put(SoapMessageHeaders.HTTP_CONTENT_TYPE, actionElement.getAttribute("content-type"));
        }

        if (actionElement.hasAttribute("accept")) {
            messageBuilder.getMessageHeaders().put(SoapMessageHeaders.HTTP_ACCEPT, actionElement.getAttribute("accept"));
        }
    }

    @Override
    protected Class<? extends AbstractSendMessageActionFactoryBean<?, ?>> getBeanDefinitionClass() {
        return SendSoapMessageActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static class SendSoapMessageActionFactoryBean extends AbstractSendMessageActionFactoryBean<SendSoapMessageAction, SendSoapMessageAction.Builder> {

        private final SendSoapMessageAction.Builder builder = new SendSoapMessageAction.Builder();

        /**
         * Sets the control attachments.
         * @param attachments the control attachments
         */
        public void setAttachments(List<SoapAttachment> attachments) {
            attachments.forEach(builder::attachment);
        }

        /**
         * Enable or disable mtom attachments
         * @param mtomEnabled
         */
        public void setMtomEnabled(boolean mtomEnabled) {
            builder.mtomEnabled(mtomEnabled);
        }

        @Override
        public SendSoapMessageAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return SendSoapMessageAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public SendSoapMessageAction.Builder getBuilder() {
            return builder;
        }
    }
}
