import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;

/**
 * 隔离本地代码或可快速返回远程调用(如memcached,redis)
 * 可以直接使用信号量隔离,降低线程隔离开销.
 */
public class SEMAPHORECommand extends HystrixCommand<String> {
    private final String name;
    public SEMAPHORECommand(String name) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("HelloWorldGroup"))
                /* 配置信号量隔离方式,默认采用线程池隔离 */
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                .withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE)));
        this.name = name;
    }
    @Override
    protected String run() throws Exception {
        return "HystrixThread:" + Thread.currentThread().getName();
    }
    public static void main(String[] args) throws Exception{
        SEMAPHORECommand command = new SEMAPHORECommand("semaphore");
        String result = command.execute();
        System.out.println(result);
        System.out.println("MainThread:" + Thread.currentThread().getName());
    }
}
