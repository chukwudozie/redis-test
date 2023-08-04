package dev.saha.redis_test.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.saha.redis_test.model.Account;
import dev.saha.redis_test.repository.AccountsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static dev.saha.redis_test.utils.CONSTANTS.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {


    @Value("${spring.data.redis.host:localhost}")
    private String hostname;
    @Value("${spring.data.redis.port:6379}")
    private int port;
    private final AccountsRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;


    public void storeData(Account data){
        redisTemplate.opsForList().leftPush(MY_LIST,data);
        redisTemplate.expire(MY_LIST, REDIS_EXPIRATION_TIME, TimeUnit.HOURS);
    }

    public synchronized Account getData() {
        String data = "";
        if (Objects.nonNull(redisTemplate.opsForList().leftPop(MY_LIST).toString()))
         data = redisTemplate.opsForList().leftPop(MY_LIST).toString();
        return mapRedisOutputToAccount(data);
    }
    public List<Account> getAllAccountsFromRedis() {

        List<Account> accounts;
        var all = redisTemplate.opsForList().range(MY_LIST, 0, -1);
        if (Objects.nonNull(all)){
            all.forEach(o -> System.out.println("Value "+o));
            accounts = getAccountFromObject(all);
            return accounts;
        }
         return new ArrayList<>();
    }

    private List<Account> getAccountFromObject(List<Object> all) {
        List<Account> accounts = new ArrayList<>();
        try {
            if (all.isEmpty())
                return new ArrayList<>();
            all.forEach(account -> accounts.add(mapRedisOutputToAccount(account.toString())));
        }catch (Exception e){
            log.error("Exception while adapting account from object :: {}",e.getMessage());
        }
        return accounts;
    }


    private Account mapRedisOutputToAccount(String redisOutput){
        Account account = new Account();
        if (Objects.isNull(redisOutput) || redisOutput.isEmpty()){
            return new Account();
        }

            String cleanedPayload = redisOutput.replaceAll("[{}\\s]", "");
             cleanedPayload = cleanedPayload.replaceAll("[()]", "");
             cleanedPayload = cleanedPayload.replaceAll("Account","");
            String[] keyValuePairs = cleanedPayload.split(",");


            for (String keyValuePair : keyValuePairs) {

                try {
                String[] pair = keyValuePair.split("=");

                // Get the key and value
                String key = pair[0].trim();
                String value = pair[1].trim();

                switch (key) {
                    case "id" -> account.setId(Long.valueOf(value));
                    case "accountRef" -> account.setAccountRef(value);
                    case "plainData" -> account.setPlainData(value);
                    case "countryCode" -> account.setCountryCode(value);
                    case "productCode" -> account.setProductCode(value);
                    case "isUsed" -> account.setIsUsed(value);
                    case "isPicked" -> account.setIsPicked(value);
                }

                }catch (Exception e){
                    log.error("Exception in account mapping");
                }
            }

        return account;
    }


    public void pushAccountsToRedis(List<Account> accounts) {
        log.info("Populating redis with list of accounts => {}",accounts);
        try (Jedis jedis = new Jedis(hostname,port)) {
            // Get the current size of the Redis list
            long currentSize = jedis.llen(MY_LIST);

            // Calculate the number of accounts to push
            int remainingCapacity = MAX - (int) currentSize;
            int numAccountsToPush = Math.min(accounts.size(), remainingCapacity);

            // Push the accounts to the Redis list
            ObjectMapper objectMapper = new ObjectMapper();
            for (int i = 0; i < numAccountsToPush; i++) {
                Account account = accounts.get(i);
                String accountJson = objectMapper.writeValueAsString(account);
                log.info("Json string of account .... => {}",accountJson);
                jedis.lpush(MY_LIST, accountJson);
                updateAccount(account);
            }
            System.out.println(numAccountsToPush + " accounts pushed to Redis.");
        } catch (JsonProcessingException e) {
            log.error("Exception in serializing account :: {}",e.getMessage());
        }catch (Exception e){
            log.error("Exception in pushing to redis :: {}",e.getMessage());
        }
    }
    private void updateAccount(Account account) {
        account.setIsPicked("Y");
        repository.save(account);

    }

    public  synchronized Account retrieveAccountFromList() {

        try (Jedis jedis = new Jedis(hostname,port)) {
            String accountJson = jedis.lpop(MY_LIST);
            System.out.println("Json string gotten");
            Account account = new Account();
            if (!Objects.isNull(accountJson)) {
                account = parseAccountJson(accountJson);
            }
            return account;
        }
    }

    private Account parseAccountJson(String accountJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(accountJson, Account.class);
        } catch (JsonProcessingException e) {
            log.error("Parsing account Exception :: {}",e.getMessage());
            return new Account();
        }
    }
}
