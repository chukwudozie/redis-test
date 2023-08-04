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

//    @Scheduled (cron = "0 */2 * * * *")
    private void populateRedis(){
        int redisSize = 0;

        System.out.println("I came here to run the populate redis job");
        redisSize = Math.toIntExact(redisTemplate.opsForList().size(MY_LIST));
        System.out.println("REDIS SIZE = > "+redisSize);
        List<Account> validAccounts = repository.findAllByIsPickedAndIsUsed("N","N");
        if (validAccounts.isEmpty()){
            log.error("No active record to pick from the DB");
            return;
        }
        log.info("Count of valid accounts to be pushed ==> {}",validAccounts.size());
        for (Account account : validAccounts){
            if (redisSize < REDIS_MAX_SIZE){
                redisService.storeData(account);
                updateAccount(account);
            }
        }

        System.out.println("CURRENT REDIS SIZE = > "+redisSize);
    }

    @Scheduled (cron = "0 */2 * * * *")
    private void populateRedis2(){
        List<Account> validAccounts = repository.findAllByIsPickedAndIsUsed("N","N");
        if (validAccounts.isEmpty()){
            log.error("No active record to pick from the DB");
            return;
        }
        redisService.pushAccountsToRedis(validAccounts);

    }

    private void updateAccount(Account account) {
        account.setIsPicked("Y");
        repository.save(account);

    }

    @Scheduled (cron = "0 */1 * * * *")
    private void createRecords(){
       System.out.println("I came here ro run the create Record Job");
       List<Account> validAccounts = repository.findAllByIsPickedAndIsUsed("N","N");
       if (validAccounts.isEmpty() || validAccounts.size() < MAX){
           //todo: GENERATE MAX TILL MAX
           Account account = createAccount();
           repository.save(account);
           System.out.println("Saved account to DB with pan "+account.getAccountRef());
           log.info("Current size of data == > {}",validAccounts.size());
       }
    }
    private Account createAccount (){
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
        return account;
    }

//    @Scheduled (cron = "0 */10 * ? * *")
    private void fetchRecord(){
        System.out.println("I came here to fetch single record");
        try {
           var acc  = redisService.getData();
           updateAccountToUsed(acc);
           log.info("Id of pan fetched ==> {}",acc.getId());
           log.info("PAN FETCHED ===> {}, time of fetch ===> {}",acc.getPlainData(), LocalDateTime.now());
        }catch (Exception e){
            log.error("Exception in record fetch job :: {}", e.getMessage());
        }
       log.info("Data fetched successfully at --> {}", LocalDateTime.now());
    }

    @Scheduled (cron = "0 */10 * ? * *")
    private void fetchRecord2(){
        System.out.println("I came here to fetch single record");
      Account account = redisService.retrieveAccountFromList();
        updateAccountToUsed(account);
        log.info("Id of pan fetched ==> {}",account.getId());
        log.info("PAN FETCHED ===> {}, time of fetch ===> {}",account.getPlainData(), LocalDateTime.now());
        log.info("Data fetched successfully at --> {}", LocalDateTime.now());
    }

    private void updateAccountToUsed(Account acc) {
        if (Objects.nonNull(acc.getId())){
            acc.setIsUsed("Y");
            repository.save(acc);
        }
    }


    //    @Scheduled(cron = "0 30 6 * * ?")
//    @Scheduled(cron = "0 45 11 * * ?", zone = "Africa/Lagos")
    private void deleteAllRedisData(){
        Long redisSize = redisTemplate.opsForList().size(MY_LIST);
        log.info("Size before deleting ... {}",redisSize);
        log.info("Deleting every data from DB ...");
        redisTemplate.getConnectionFactory().getConnection().flushDb();
        log.info("All Redis Data Deleted");
        log.info("Size After deleting ... {}",redisSize);
    }

}
