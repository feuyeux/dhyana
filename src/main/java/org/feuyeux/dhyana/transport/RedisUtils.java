package org.feuyeux.dhyana.transport;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.feuyeux.dhyana.config.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yukong.lxx
 * @date 2018/12/19 2:21 PM
 */
@Component
@Slf4j
class RedisUtils {
    private JedisPool jedisPool;

    @Autowired
    private RedisConfig config;

    @Setter
    @Getter
    private int dbIndex = 1;

    @PostConstruct
    public void init() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(this.config.getMaxTotal());
        config.setMinIdle(this.config.getMinIdle());
        config.setMaxIdle(this.config.getMaxIdle());
        config.setTestOnBorrow(true);
        config.setMaxWaitMillis(this.config.getMaxWaitMillis());

        jedisPool = new JedisPool(config,
            this.config.getRedisHost(), this.config.getPort(),
            this.config.getConnectionTimeout(), this.config.getSoTimeout(),
            this.config.getPwd(), Protocol.DEFAULT_DATABASE, null);
    }

    @PreDestroy
    public void destroy() {
        jedisPool.destroy();
    }

    public void flushDB() {
        log.warn("flushDB");
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            jedis.flushDB();
        }
    }

    /***** string *****/

    public String saveString(String key, String value) {
        if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(value)) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.set(key, value);
        }
    }

    public String saveString(String key, String value, int seconds) {
        if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(value)) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.setex(key, seconds, value);
        }
    }

    public String readString(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.get(key);
        }
    }

    public long deleteString(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return -1;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.del(key);
        }
    }

    /***** queue *****/

    public long pushToQueue(String key, String value) {
        return pushToQueue(key, value, 0);
    }

    public long pushToQueue(String key, String value, int seconds) {
        if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(value)) {
            return -1L;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            long result = jedis.lpush(key, value);
            if (result > 0 && seconds > 0) {
                jedis.expire(key, seconds);
            }
            return result;
        }
    }

    public String popFromQueue(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.rpop(key);
        }
    }

    public List<String> getQueue(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.lrange(key, 0, jedis.llen(key));
        }
    }

    public List<String> getQueue(String key, long start, long end) {
        if (Strings.isNullOrEmpty(key) || start < 0 || end < 0) {
            return null;
        }

        if (start < end) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.lrange(key, start, end);
        }
    }

    public String getQueueByIndex(String key, long index) {
        if (Strings.isNullOrEmpty(key) || index < 0) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.lindex(key, index);
        }
    }

    public long getQueueLength(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return -1;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.llen(key);
        }
    }

    /***** set *****/

    public long addToSet(String key, String... value) {
        return addToSet(key, 0, value);
    }

    public long addToSet(String key, int seconds, String... value) {
        if (Strings.isNullOrEmpty(key) || value == null) {
            return -1;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            long result = jedis.sadd(key, value);
            if (result > 0 && seconds > 0) {
                jedis.expire(key, seconds);
            }
            return result;
        }
    }

    public Set<String> getFromSet(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.smembers(key);
        }
    }

    public Long getSetSize(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.scard(key);
        }
    }

    public boolean isSetMember(String key, String value) {
        if (Strings.isNullOrEmpty(key)) {
            return false;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.sismember(key, value);
        }
    }

    public long deleteFromSet(String key, String... value) {
        if (Strings.isNullOrEmpty(key) || value == null) {
            return -1;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.srem(key, value);
        }
    }

    /***** map *****/

    public String saveMap(String key, Map<String, String> value) {
        return saveMap(key, value, 0);
    }

    public String saveMap(String key, Map<String, String> value, int seconds) {
        if (Strings.isNullOrEmpty(key) || value == null || value.isEmpty()) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            String result = jedis.hmset(key, value);
            if (!Strings.isNullOrEmpty(result) && seconds > 0) {
                jedis.expire(key, seconds);
            }
            return result;
        }
    }

    public long setMapValue(String key, String field, String value) {
        if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(field) || Strings.isNullOrEmpty(value)) {
            return -1;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.hset(key, field, value);
        }
    }

    public String getMapValue(String key, String field) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.hget(key, field);
        }
    }

    /***** common *****/

    public long deleteKey(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return -1;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.del(key);
        }
    }

    public String renameKey(String oldKey, String newKey) {
        if (Strings.isNullOrEmpty(oldKey) || Strings.isNullOrEmpty(newKey)) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.rename(oldKey, newKey);
        }
    }

    public long incr(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return -1;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        }
    }

    public long incrBy(String key, int addValue) {
        if (Strings.isNullOrEmpty(key)) {
            return -1;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incrBy(key, addValue);
        }
    }

    public boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }

    public long setnx(String key, String value) {
        if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(value)) {
            return -1;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.setnx(key, value);
        }
    }

    public long expire(String key, int seconds) {
        if (Strings.isNullOrEmpty(key)) {
            return -1;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(dbIndex);
            return jedis.expire(key, seconds);
        }
    }
}
