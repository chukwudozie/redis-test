package dev.saha.redis_test.utils;

import dev.saha.redis_test.repository.AccountsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Random;
@Service
@RequiredArgsConstructor
public class GeneratedValues {
    private final AccountsRepository repository;
    private final Random random = new Random();

    public String generateUniqueNumber() {
        String uniqueNumber = "";
        do {
            uniqueNumber = generateRandomNumber();
        } while (!isUnique(uniqueNumber));
        return uniqueNumber;
    }

    private String generateRandomNumber() {
//        long randomNumber = random.nextLong() % 9000000000L + 1000000000L; // FOR TEN DIGITS
        long randomNumber = random.nextLong() % 9000000000000000L + 1000000000000000L; //FOR 16 DIGITS
        return String.valueOf(Math.abs(randomNumber));
    }

    private boolean isUnique(String number) {
        if (repository.existsAccountByPlainData(number))
            return false;
        if (number.length() != 16)
            return false;
        return !number.startsWith("0");
    }

    public String encrypt(String data) {
        System.out.println("Data => "+data);
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    public String decrypt(String data){
        return  new String(Base64.getDecoder().decode(data.getBytes()));
    }

//    public static void main(String[] args) {
//        var unique = new UniqueNumberGenerator();
////        System.out.println(new UniqueNumberGenerator().generateUniqueNumber());
//        var num = unique.generateUniqueNumber();
//        System.out.println(num);
//        var encoded = unique.encrypt(String.valueOf(num));
//        System.out.println(encoded);
//        System.out.println(unique.decrypt(encoded));
//
//
//    }
}
