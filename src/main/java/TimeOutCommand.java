import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;

import java.util.concurrent.TimeUnit;

/**
 * 超时后调用 fallback
 */
public class TimeOutCommand extends HystrixCommand<String> {

    private final String name;

    public TimeOutCommand(String name) {
        super(Setter.withGroupKey(
            HystrixCommandGroupKey.Factory.asKey("HelloWorldGroup"))
            /* HystrixCommandKey工厂定义依赖名称 */
            /**
             *  NOTE: 每个CommandKey代表一个依赖抽象,
             *  相同的依赖要使用相同的CommandKey名称。
             *  依赖隔离的根本就是对相同CommandKey的依赖做隔离.
             */
            .andCommandKey(HystrixCommandKey.Factory.asKey("HelloWorld"))
                /* 配置依赖超时时间,500毫秒*/
            .andCommandPropertiesDefaults(
                HystrixCommandProperties.Setter().withExecutionIsolationThreadTimeoutInMilliseconds(500)
            )
        );

        this.name = name;
    }

    @Override
    protected String getFallback() {
        return "exeucute Falled";
    }

    @Override
    protected String run() throws Exception {
        //sleep 1 秒,调用会超时
        TimeUnit.MILLISECONDS.sleep(1000);
        return "Hello " + name +" thread:" + Thread.currentThread().getName();
    }

    public static void main(String[] args) throws Exception{
        HelloWorldCommand command = new HelloWorldCommand("test-Fallback");
        String result = command.execute();
    }
}
