package com.cgcoding;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Public HTTP API entry point for this service.
 *
 * <p>This resource is registered under {@code /hello} and currently exposes a single endpoint:
 *
 * <ul>
 *   <li>{@code GET /hello} - returns a plain-text greeting
 * </ul>
 *
 * <p>Example:
 *
 * <pre>{@code
 * curl -i http://localhost:8080/hello
 * }</pre>
 */
@Path("/hello")
public class GreetingResource {

    /**
     * Returns a simple plain-text greeting.
     *
     * <p><b>HTTP</b>: {@code GET /hello}
     * <br><b>Produces</b>: {@code text/plain}
     * <br><b>Success</b>: {@code 200 OK} with body {@code "Hello from Quarkus REST"}
     *
     * @return a greeting message as plain text
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus REST";
    }
}
