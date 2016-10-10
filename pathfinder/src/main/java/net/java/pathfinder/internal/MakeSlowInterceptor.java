package net.java.pathfinder.internal;

import java.util.Random;
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
        Thread.sleep(new Random().nextInt(400) + 200);
        return ctx.proceed();
    }
}
