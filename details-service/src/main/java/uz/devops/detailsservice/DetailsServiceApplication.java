package uz.devops.detailsservice;


import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.*;

public class DetailsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DetailsServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/api/bank-details")
@RequiredArgsConstructor
@Slf4j
class BankDetailsController {
    private final BankDetailsRepository bankDetailsRepository;

    @PostMapping
    public BankDetails create(@RequestBody BankDetailsCreateDTO dto) {
        return bankDetailsRepository.save(new BankDetails(
                dto.name(),
                dto.code(),
                dto.address(),
                dto.phoneNumber(),
                dto.email()));
    }

    @GetMapping("/{id}")
    public BankDetails get(@PathVariable Long id) {
        log.info("Getting Bank detail by bank id: {}", id);
        return bankDetailsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BankDetails not found: %s".formatted(id)));
    }

}

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
class BankDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String code;
    @Column(nullable = false)
    private String address;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    private String email;

    public BankDetails(String name, String code, String address, String phoneNumber, String email) {
        this.name = name;
        this.code = code;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }
}

interface BankDetailsRepository extends JpaRepository<BankDetails, Long> {

}

record BankDetailsCreateDTO(Long bankId, String name, String code, String address, String phoneNumber, String email) {
}