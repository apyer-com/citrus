package com.consol.citrus.rmi.endpoint.builder;

import com.consol.citrus.endpoint.builder.ClientServerEndpointBuilder;
import com.consol.citrus.rmi.client.RmiClientBuilder;
import com.consol.citrus.rmi.server.RmiServerBuilder;

/**
 * @author Christoph Deppisch
 */
public final class RmiEndpoints extends ClientServerEndpointBuilder<RmiClientBuilder, RmiServerBuilder> {
    /**
     * Private constructor setting the client and server builder implementation.
     */
    private RmiEndpoints() {
        super(new RmiClientBuilder(), new RmiServerBuilder());
    }

    /**
     * Static entry method for Rmi endpoint builder.
     * @return
     */
    public static RmiEndpoints rmi() {
        return new RmiEndpoints();
    }
}
