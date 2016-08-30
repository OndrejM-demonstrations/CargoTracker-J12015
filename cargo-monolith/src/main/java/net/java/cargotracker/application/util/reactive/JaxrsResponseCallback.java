package net.java.cargotracker.application.util.reactive;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.concurrent.ContextService;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * A generic JAR-RS client response callback that is also a CompletableFuture. It is meant to be used in async get method of JAX-RS client.
 * It is recommended to use the static {@code JaxrsResponseCallback.get} method to enable chaining callbacks to the CompletableFuture fluently:
 * 
 * <pre>
 * {@code
    JaxrsResponseCallback.get(ClientBuilder.newClient()
        .request()
        .async(), Response.class)
        .thenAccept(
            response -> {
                System.out.println("Response code " + response.getStatus()
                        + ", with content: " + response.readEntity(String.class));
            }
        )  
 * }
 * </pre>
 * 
 * Compared to {@link SimpleJaxrsResponseCallback}, it supports generics for response type. 
 * It also completes the completionStage in the context of the thread that calls {@code get()} method, thus ensuring that the subsequent callback will also be executed in a managed thread within the same context. 
 * This is to support runtimes, where JAX-RS runs the completion callback in an unmanaged thread.
 * 
 * Created by mertcaliskan
 */
public class JaxrsResponseCallback<T> extends CompletableFuture<T> implements InvocationCallback<T> {

    ThreadAsyncContext asyncContext = null;

    public JaxrsResponseCallback() {
    }

    public JaxrsResponseCallback(ContextService ctxService) {
        if (ctxService != null) {
            asyncContext = ThreadAsyncContext.create(ctxService);
        }
    }

    @Override
    public void completed(T response) {
        if (asyncContext == null) {
            super.complete(response);
        } else {
            asyncContext.run(() -> super.complete(response));
        }
    }

    @Override
    public void failed(Throwable throwable) {
        if (asyncContext == null) {
            super.completeExceptionally(throwable);
        } else {
            asyncContext.run(() -> super.completeExceptionally(throwable));
        }
    }

    public static CompletionStage<Response> get(AsyncInvoker invoker) {
        ContextService ctxService = lookupDefaultContextService();
        return get(invoker, Response.class, ctxService);
    }

    public static <RESPONSE> CompletionStage<RESPONSE> get(AsyncInvoker invoker, Class<RESPONSE> cls, ContextService ctxService) {
        final JaxrsResponseCallback<RESPONSE> completion = new JaxrsResponseCallback<>(ctxService);
        invoker.get(new InvocationCallback<Response>() {
            @Override
            public void completed(Response response) {
                if (Response.class.equals(cls)) {
                    completion.completed(cls.cast(response));
                }
                completion.completed(response.readEntity(cls));
            }

            @Override
            public void failed(Throwable throwable) {
                completion.failed(throwable);
            }
            
        });
        return completion;
    }

    private static ContextService lookupDefaultContextService() {
        ContextService ctxService = null;
        try {
            ctxService = (ContextService)(new InitialContext().lookup("java:comp/DefaultContextService"));
        } catch (NamingException ex) {
            Logger.getLogger(JaxrsResponseCallback.class.getName()).log(Level.FINE, null, ex);
        }
        return ctxService;
    }

}