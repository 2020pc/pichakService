//package com.caspian.pichak.service;
//
//import com.google.common.cache.CacheBuilder;
//import com.google.common.cache.CacheLoader;
//import com.google.common.cache.LoadingCache;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//
//@Service("requestAttemptService")
//@EnableScheduling
//public class RequestAttemptService {
//    private final LoadingCache<String, RequestAttemptObject> attemptsCache;
//    @Value("${security.security-max_request_attempt}")
//    private int MAX_ATTEMPT_REQUEST;
//    @Value("${security.security-max_request_time}")
//    private int MAX_ATTEMPT_TIME;
//
//    public RequestAttemptService() {
//        this.attemptsCache = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.DAYS).build(new CacheLoader<String, RequestAttemptObject>() {
//            public RequestAttemptObject load(final String key) {
//                return RequestAttemptService.this.new RequestAttemptObject();
//            }
//        });
//    }
//
//    private void requestAttemptSucceeded(final String key) {
//        this.attemptsCache.invalidate(key);
//    }
//
//    public void requestAttempt(final String key) {
//        RequestAttemptObject attempts;
//        try {
//            attempts = (RequestAttemptObject)this.attemptsCache.get(key);
//        } catch (ExecutionException var4) {
//            attempts = new RequestAttemptObject();
//        }
//
//        attempts.setAttempt(attempts.getAttempt() + 1);
//        attempts.setNanoTime(System.nanoTime());
//        this.attemptsCache.put(key, attempts);
//    }
//
//    public boolean isBlocked(final String key) {
//        return false;
//    }
//
//    @Scheduled(cron = "${security.security-max_request_loop_time}")
//    public void create() {
//        long nanoTime = System.nanoTime();
//        String key = this.attemptsCache.asMap().entrySet().stream().filter((e) -> e.getValue() != null).filter((e) -> ((RequestAttemptObject)e.getValue()).getNanoTime() != null).filter((e) -> (double)(nanoTime - ((RequestAttemptObject)e.getValue()).getNanoTime()) / (double)1000000.0F > (double)this.MAX_ATTEMPT_TIME).map(Map.Entry::getKey).findFirst().orElse(null);
//        if (key != null) {
//            this.requestAttemptSucceeded(key);
//        }
//
//    }
//
//    private class RequestAttemptObject {
//        private int attempt = 0;
//        private Long nanoTime = 0L;
//
//        public RequestAttemptObject() {
//        }
//
//        public int getAttempt() {
//            return this.attempt;
//        }
//
//        public void setAttempt(int attempt) {
//            this.attempt = attempt;
//        }
//
//        public Long getNanoTime() {
//            return this.nanoTime;
//        }
//
//        public void setNanoTime(Long nanoTime) {
//            this.nanoTime = nanoTime;
//        }
//    }
//}
