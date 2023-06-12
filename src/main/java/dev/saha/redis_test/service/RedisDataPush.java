package dev.saha.redis_test.service;

import dev.saha.redis_test.model.Account;
import dev.saha.redis_test.repository.AccountsRepository;
import dev.saha.redis_test.utils.Shared;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static dev.saha.redis_test.utils.CONSTANTS.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class RedisDataPush {


    private final RedisTemplate<String, Object> redisTemplate;

    private final AccountsRepository repository;
    private final RedisService redisService;
    private final Shared shared;

    @Scheduled (cron = "0 */2 * * * *")
    private void populateRedis(){
        System.out.println("I came here to run the populate redis job");
            Long redisSize = redisTemplate.opsForList().size(MY_LIST);
            if (Objects.isNull(redisSize)){
                redisSize = 0L;
            }
        System.out.println("REDIS SIZE = > "+redisSize);
            if (redisSize < REDIS_MAX_SIZE){
                List<Account> validAccounts = repository.findAllByIsPickedAndIsUsed("N","N");
                if (!validAccounts.isEmpty()){
                    validAccounts.forEach(redisService::storeData);
                    System.out.println("Data written to Redis, total of ==> "+validAccounts.size());
                }
            } else {
                log.info("MAXIMUM DATA ALREADY IN REDIS");
            }
        System.out.println("CURRENT REDIS SIZE = > "+redisSize);
    }

   @Scheduled (cron = "0 */1 * * * *")
    private void createRecords(){
       System.out.println("I came here ro run the create Record Job");
       List<Account> validAccounts = repository.findAllByIsPickedAndIsUsed("N","N");
       if (validAccounts.isEmpty() || validAccounts.size() < MAX){
           //todo: GENERATE MAX TILL MAX
           Account account = new Account();
           shared.setPlainData();
           shared.setAccountRef();
           account.setAccountRef(shared.getAccountRef());
           account.setPlainData(shared.getPlainData());
           account.setProductCode("");
           account.setCountryCode("NGA");
           account.setProductCode("0453");
           account.setIsUsed("N");
           account.setIsPicked("N");
           repository.save(account);
           System.out.println("Saved account to DB with pan "+account.getAccountRef());
       }
    }

    @Scheduled (cron = "0 */3 * * * *")
    private void fetchRecord(){
        System.out.println("I came here to fetch single record");
        try {
           var acc  = redisService.getData();
           log.info("PAN FETCHED ===> {}, time of fetch ===> {}",acc.getPlainData(), LocalDateTime.now());
        }catch (Exception e){
            log.error("Exception in record fetch job :: {}", e.getMessage());
        }
       log.info("Data fetched successfully at --> {}", LocalDateTime.now());
    }

}
