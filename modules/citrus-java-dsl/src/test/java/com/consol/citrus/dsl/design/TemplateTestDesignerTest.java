/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.design;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.SleepAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.container.Template;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


public class TemplateTestDesignerTest extends AbstractTestNGUnitTest {

    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    @Test
    public void testTemplateBuilder() {
        List<TestAction> actions = new ArrayList<>();
        actions.add(new EchoAction.Builder().build());
        actions.add(new SleepAction.Builder().build());
        Template rootTemplate = new Template.Builder()
                .templateName("fooTemplate")
                .actions(actions)
                .build();

        reset(applicationContextMock);

        when(applicationContextMock.getBean("fooTemplate", Template.class)).thenReturn(rootTemplate);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                applyTemplate("fooTemplate")
                    .parameter("param", "foo")
                    .parameter("text", "Citrus rocks!");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActions().size(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Template.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "fooTemplate");

        Template container = (Template)test.getActions().get(0);
        Assert.assertTrue(container.isGlobalContext());
        Assert.assertEquals(container.getParameter().toString(), "{param=foo, text=Citrus rocks!}");
        Assert.assertEquals(container.getActions().size(), 2);
        Assert.assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(container.getActions().get(1).getClass(), SleepAction.class);
    }

    @Test
    public void testTemplateBuilderGlobalContext() {
        List<TestAction> actions = new ArrayList<TestAction>();
        actions.add(new EchoAction.Builder().build());
        Template rootTemplate = new Template.Builder()
                .templateName("fooTemplate")
                .actions(actions)
                .build();

        reset(applicationContextMock);

        when(applicationContextMock.getBean("fooTemplate", Template.class)).thenReturn(rootTemplate);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                applyTemplate("fooTemplate")
                    .globalContext(false);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActions().size(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Template.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "fooTemplate");

        Template container = (Template)test.getActions().get(0);
        Assert.assertFalse(container.isGlobalContext());
        Assert.assertEquals(container.getParameter().size(), 0L);
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
    }
}
