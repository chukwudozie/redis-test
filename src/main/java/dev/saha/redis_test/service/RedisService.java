package dev.saha.redis_test.service;

import dev.saha.redis_test.model.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static dev.saha.redis_test.utils.CONSTANTS.MY_LIST;
import static dev.saha.redis_test.utils.CONSTANTS.REDIS_EXPIRATION_TIME;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {



    private final RedisTemplate<String, Object> redisTemplate;


    public void storeData(Account data){
        redisTemplate.opsForList().leftPush(MY_LIST,data);
        redisTemplate.expire(MY_LIST, REDIS_EXPIRATION_TIME, TimeUnit.HOURS);
    }

    public Account getData() {
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
}
