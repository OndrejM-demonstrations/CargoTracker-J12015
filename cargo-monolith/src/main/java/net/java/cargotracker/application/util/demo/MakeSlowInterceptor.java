package net.java.cargotracker.application.util.demo;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.interceptor.*;

@Interceptor
@MakeSlow
@Dependent
@Priority(1000)
public class MakeSlowInterceptor {
    @AroundInvoke
    public Object makeSlow(InvocationContext ctx) throws Exception {
        Thread.sleep(5000);
        return ctx.proceed();
    }
}
