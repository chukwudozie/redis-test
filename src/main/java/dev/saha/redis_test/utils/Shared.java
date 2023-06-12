package dev.saha.redis_test.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@RequiredArgsConstructor
public class Shared {

    private final GeneratedValues generatedValues;

    private String accountRef;

    private String plainData;

    public void setAccountRef(){
        this.setAccountRef(generatedValues.encrypt(plainData));
    }


    public void setPlainData(){
        this.setPlainData(generatedValues.generateUniqueNumber());
    }
}
