package uz.devops.bankservice;

import feign.RetryableException;
import feign.Retryer;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.ls.LSInput;

import java.util.Collections;
import java.util.List;


@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class BankServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankServiceApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .build();
    }
}

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
class BankController{
    private  final BankRepository bankRepository;
    private final BankDetailsClient bankDetailsClient;
    private final DocumentClient documentClient;


}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;

    public Bank(String title, String description) {
        this.title = title;
        this.description = description;
    }
}

interface BankRepository extends JpaRepository<Bank, Long> {
}

record BankCreateDTO(
        String name,
        String code,
        String address,
        String phoneNumber,
        String email,
        boolean deleted) {
}

record BankDetail(
        Long id,
        String name,
        String code,
        String address,
        String phoneNumber,
        String email,
        Object body,
        List<Document> bankDocuments) {
}
record BankDetailsCreateDTO(
        Long bankId,
        String name,
        String code,
        String address,
        String phoneNumber,
        String email) {
}
record Document( Long id,
 String title,
         String description,
         boolean deleted ){}


@FeignClient(value = "${bank.details.service.baseUrl}")
interface BankDetailsClient {
    @GetMapping("/{id}")
    Object get(@PathVariable("id") Long bankId);

    @PostMapping
    Object create(@RequestBody BankDetailsCreateDTO dto);
}


@FeignClient(name = "{document.service.baseUrl}")
interface DocumentClient {
    Logger log = LoggerFactory.getLogger(DocumentClient.class.getName());

    @GetMapping("/{id}/bank")
    List<Document> getAllByBankId(@PathVariable("id") Long bankId);

    @SuppressWarnings("unused")
    default List<Document> getAllByBankIdCircuitBreakerFallback(Long bankId, Exception e) {
        log.error("Error : {}", e.getMessage(), e);
        return Collections.emptyList();
    }


    @SuppressWarnings("unused")
    default List<Document> bankDetailsRetryFallback(Integer bankID, Exception e) {
        log.error("Error : {}", e.getMessage(), e);
        log.error("Retrying : {}", System.currentTimeMillis());
        return Collections.emptyList();
    }


}

class MYRetry implements Retryer {

    private final int maxAttempts;
    private final long backoff;
    int attempt;

    public MYRetry() {
        this(100, 3);
    }

    public MYRetry(long backoff, int maxAttempts) {
        this.backoff = backoff;
        this.maxAttempts = maxAttempts;
        this.attempt = 1;
    }

    public void continueOrPropagate(RetryableException e) {
        if (attempt++ >= maxAttempts) {
            throw e;
        }

        try {
            Thread.sleep(backoff);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Retryer clone() {
        return new MYRetry(backoff, maxAttempts);
    }
}


