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

package com.consol.citrus.camel.actions;

import java.util.Collections;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.apache.camel.CamelContext;
import org.apache.camel.FailedToStartRouteException;
import org.apache.camel.model.RouteDefinition;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CreateCamelRouteActionTest extends AbstractTestNGUnitTest {

    private CamelContext camelContext = Mockito.mock(CamelContext.class);
    private RouteDefinition route = Mockito.mock(RouteDefinition.class);

    @Test
    public void testCreateRouteContext() throws Exception {
        reset(camelContext);

        when(camelContext.getName()).thenReturn("camel_context");

        doAnswer(invocation -> {
            RouteDefinition routeDefinition = invocation.getArgument(0);
            Assert.assertEquals(routeDefinition.getId(), "route_1");
            return null;
        })
        .doAnswer(invocation -> {
            RouteDefinition routeDefinition = invocation.getArgument(0);
            Assert.assertEquals(routeDefinition.getId(), "route_2");
            return null;
        }).when(camelContext).addRouteDefinition(any(RouteDefinition.class));

        CreateCamelRouteAction action = new CreateCamelRouteAction.Builder()
                .context(camelContext)
                .routeContext("<routeContext xmlns=\"http://camel.apache.org/schema/spring\">\n" +
                        "<route id=\"route_1\">\n" +
                        "<from uri=\"direct:test1\"/>\n" +
                        "<to uri=\"mock:test1\"/>\n" +
                        "</route>\n" +
                        "<route id=\"route_2\">\n" +
                        "<from uri=\"direct:test2\"/>\n" +
                        "<to uri=\"mock:test2\"/>\n" +
                        "</route>\n" +
                        "</routeContext>")
                .build();
        action.execute(context);

        verify(camelContext, times(2)).addRouteDefinition(any(RouteDefinition.class));
    }

    @Test
    public void testCreateRouteContextVariableSupport() throws Exception {
        reset(camelContext);

        context.setVariable("routeId", "route_1");
        context.setVariable("endpointUri", "test1");

        when(camelContext.getName()).thenReturn("camel_context");

        doAnswer(invocation -> {
            RouteDefinition routeDefinition = invocation.getArgument(0);
            Assert.assertEquals(routeDefinition.getId(), "route_1");
            return null;
        }).when(camelContext).addRouteDefinition(any(RouteDefinition.class));

        CreateCamelRouteAction action = new CreateCamelRouteAction.Builder()
                .context(camelContext)
                .routeContext("<routeContext xmlns=\"http://camel.apache.org/schema/spring\">\n" +
                    "<route id=\"${routeId}\">\n" +
                        "<from uri=\"direct:${endpointUri}\"/>\n" +
                        "<to uri=\"mock:${endpointUri}\"/>\n" +
                    "</route>\n" +
                "</routeContext>")
                .build();

        action.execute(context);

        verify(camelContext).addRouteDefinition(any(RouteDefinition.class));
    }

    @Test
    public void testCreateRoute() throws Exception {
        reset(camelContext, route);

        when(camelContext.getName()).thenReturn("camel_context");
        when(route.getId()).thenReturn("route_1");

        CreateCamelRouteAction action = new CreateCamelRouteAction.Builder()
                .context(camelContext)
                .routes(Collections.singletonList(route))
                .build();
        action.execute(context);

        verify(camelContext).addRouteDefinition(route);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testCreateRouteWithException() throws Exception {
        reset(camelContext, route);

        when(camelContext.getName()).thenReturn("camel_context");
        when(route.getId()).thenReturn("route_1");

        doThrow(new FailedToStartRouteException("routeId", "Failed to start route")).when(camelContext).addRouteDefinition(route);

        CreateCamelRouteAction action = new CreateCamelRouteAction.Builder()
                .context(camelContext)
                .routes(Collections.singletonList(route))
                .build();
        action.execute(context);

    }
}
