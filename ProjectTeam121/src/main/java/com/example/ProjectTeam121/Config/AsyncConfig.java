package com.example.ProjectTeam121.Config;

import org.slf4j.MDC;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Autowired
    @Qualifier("virtualThreadCustom")
    private ExecutorService virtualThreadCustom;

    @Value("${async.core-pool-size:10}")
    private int corePoolSize;

    @Value("${async.max-pool-size:50}")
    private int maxPoolSize;

    @Value("${async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${async.thread-name-prefix:async-}")
    private String threadNamePrefix;

    @Value("${async.use-virtual-threads:true}")
    private boolean useVirtualThreads;

    /**
     * Task decorator để sao chép MDC context từ calling thread sang worker thread
     */
    @Bean
    public TaskDecorator mdcContextTaskDecorator() {
        return task -> {
            // Sao chép toàn bộ MDC context hiện tại
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    // Thiết lập MDC context cho thread mới
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    // Thực thi task
                    task.run();
                } finally {
                    MDC.clear();
                }
            };
        };
    }

    /**
     * Decorator cho virtual thread executor để đảm bảo MDC được truyền đúng
     */
    @Bean(name = "mdcAwareVirtualThreadExecutor")
    @org.springframework.context.annotation.Primary
    public Executor mdcAwareVirtualThreadExecutor() {
        return task -> {
            // Sao chép MDC từ calling thread
            Map<String, String> contextMap = MDC.getCopyOfContextMap();

            virtualThreadCustom.execute(() -> {
                try {
                    // Thiết lập MDC trên thread mới
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    // Thực thi task
                    task.run();
                } finally {
                    MDC.clear();
                }
            });
        };
    }

    // ==========================================================
    // THÊM BEAN NÀY: Executor chuyên biệt cho IoT (Upload/Query)
    // ==========================================================
    @Bean(name = "ingestionExecutor")
    public Executor ingestionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // Số luồng cơ bản
        executor.setMaxPoolSize(50);  // Tăng lên khi tải cao
        executor.setQueueCapacity(1000); // Hàng đợi chờ
        executor.setThreadNamePrefix("Ingest-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "queryExecutor")
    public Executor queryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Query-");
        executor.initialize();
        return executor;
    }

    @Bean("virtualThreadTaskExecutor")
    public TaskExecutor virtualThreadTaskExecutor() {
        return new TaskExecutor() {
            @Override
            public void execute(Runnable task) {
                Thread.ofVirtual().start(task);
            }
        };
    }

    @Bean("virtualThreadExecutorService")
    public ExecutorService virtualThreadExecutorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    @NonNull
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
