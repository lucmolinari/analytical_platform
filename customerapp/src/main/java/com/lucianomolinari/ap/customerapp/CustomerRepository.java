package com.lucianomolinari.ap.customerapp;

import com.lucianomolinari.ap.customerapp.Customer.Gender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class CustomerRepository {

    private final RedisAtomicLong customerIdGenerator;
    private final HashOperations<String, String, Object> hashOperations;

    @Autowired
    public CustomerRepository(final StringRedisTemplate redisTemplate) {
        this.customerIdGenerator = new RedisAtomicLong("next_customer_id", redisTemplate.getConnectionFactory());
        this.hashOperations = redisTemplate.opsForHash();
    }

    public Optional<Customer> findById(final Long id) {
        final Map<String, Object> customerAsMap = hashOperations.entries(getKey(id));
        if (customerAsMap.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(getCustomerFromMap(customerAsMap));
    }

    public Long add(final Customer customer) {
        final Long nextId = this.customerIdGenerator.addAndGet(1);
        customer.setId(nextId);

        hashOperations.putAll(getKey(nextId), getCustomerAsMap(customer));
        return nextId;
    }

    private Map<String, Object> getCustomerAsMap(final Customer customer) {
        final Map<String, Object> map = new HashMap<>();
        map.put("id", customer.getId());
        map.put("name", customer.getName());
        map.put("gender", customer.getGender());
        return map;
    }

    private Customer getCustomerFromMap(final Map<String, Object> customerAsMap) {
        final Long id = Long.valueOf((String) customerAsMap.get("id"));
        final String name = (String) customerAsMap.get("name");
        final Gender gender = Gender.valueOf((String) customerAsMap.get("gender"));

        return new Customer(id, name, gender);
    }

    private String getKey(final Long id) {
        return String.format("customer:%s", id);
    }

}
