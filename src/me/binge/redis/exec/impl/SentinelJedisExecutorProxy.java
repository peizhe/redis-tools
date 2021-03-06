package me.binge.redis.exec.impl;

import java.lang.reflect.Method;

import me.binge.redis.exception.RedisExecExecption;
import me.binge.redis.exec.RedisExecutorProxy;
import me.binge.redis.utils.DontIntercept;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.lang3.StringUtils;

import redis.clients.jedis.Jedis;

public class SentinelJedisExecutorProxy extends RedisExecutorProxy<SentinelJedisExecutor, Jedis> {

    @Override
    public Object intercept(Object obj, Method method, Object[] args,
            MethodProxy proxy) throws Throwable {

        if (method.getAnnotation(DontIntercept.class) != null) {
            return proxy.invokeSuper(obj, args);
        }

        Jedis jedis = pool.getResource();
        this.rtl.conn(jedis);
        boolean broken = false;
        try {
            return proxy.invokeSuper(obj, args);
        } catch (Exception e) {
            broken = true;
            throw new RedisExecExecption("exec " + method + " with "
                    + StringUtils.join(args, ',') + " error.", e);
        } finally {
            this.rtl.remove();
            release(jedis, broken);
        }
    }

    @Override
    public Class<SentinelJedisExecutor> getProxiedClass() {
        return SentinelJedisExecutor.class;
    }

}
