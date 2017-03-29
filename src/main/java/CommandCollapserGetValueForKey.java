import com.netflix.hystrix.HystrixCollapser;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixRequestLog;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * 命令调用合并允许多个请求合并到一个线程/信号下批量执行。
 */
public class CommandCollapserGetValueForKey extends HystrixCollapser<List<String>, String, Integer> {
    private final Integer key;

    public CommandCollapserGetValueForKey(Integer key) {
        this.key = key;
    }

    @Override
    public Integer getRequestArgument() {
        return key;
    }

    @Override
    protected HystrixCommand<List<String>> createCommand(final Collection<CollapsedRequest<String, Integer>> requests) {
        //创建返回command对象
        return new BatchCommand(requests);
    }

    @Override
    protected void mapResponseToRequests(List<String> batchResponse, Collection<CollapsedRequest<String, Integer>> requests) {
        int count = 0;
        for (CollapsedRequest<String, Integer> request : requests) {
            //手动匹配请求和响应
            request.setResponse(batchResponse.get(count++));
        }
    }

    private static final class BatchCommand extends HystrixCommand<List<String>> {
        private final Collection<CollapsedRequest<String, Integer>> requests;

        private BatchCommand(Collection<CollapsedRequest<String, Integer>> requests) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("GetValueForKey")));
            this.requests = requests;
        }

        @Override
        protected List<String> run() {
            ArrayList<String> response = new ArrayList<String>();
            for (CollapsedRequest<String, Integer> request : requests) {
                response.add("ValueForKey: " + request.getArgument());
            }
            return response;
        }
    }

    public static class UnitTest {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        Future<String> f1 = new CommandCollapserGetValueForKey(1).queue();
        Future<String> f2 = new CommandCollapserGetValueForKey(2).queue();
        Future<String> f3 = new CommandCollapserGetValueForKey(3).queue();
        Future<String> f4 = new CommandCollapserGetValueForKey(4).queue();

        public void test() throws ExecutionException, InterruptedException {
            f1.get();
//            f2.get();
//            f3.get();
//            f4.get();
            System.out.println(HystrixRequestLog.getCurrentRequest().getExecutedCommands().size());
//            assertEquals("ValueForKey: 1", f1.get());
//            assertEquals("ValueForKey: 2", f2.get());
//            assertEquals("ValueForKey: 3", f3.get());
//            assertEquals("ValueForKey: 4", f4.get());
//            assertEquals(1, HystrixRequestLog.getCurrentRequest().getExecutedCommands().size());
//            HystrixCommand<?> command = HystrixRequestLog.getCurrentRequest().getExecutedCommands().toArray(new HystrixCommand<?>[1])[0];
//            assertEquals("GetValueForKey", command.getCommandKey().name());
//            assertTrue(command.getExecutionEvents().contains(HystrixEventType.COLLAPSED));
//            assertTrue(command.getExecutionEvents().contains(HystrixEventType.SUCCESS));
        }
    }

    public static void main(String[] args) throws Exception{
        UnitTest test = new UnitTest();
        test.test();
    }
}