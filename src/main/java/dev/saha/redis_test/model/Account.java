package dev.saha.redis_test.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "account")
public class Account  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountRef;
    private String plainData;
    private String countryCode;
    private String productCode;
    private String isUsed;
    private String isPicked;
//    private LocalTime localTime;

}
