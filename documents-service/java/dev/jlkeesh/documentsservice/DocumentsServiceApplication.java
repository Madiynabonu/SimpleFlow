package dev.jlkeesh.documentsservice;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.GET;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class DocumentsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentsServiceApplication.class, args);
    }

}


@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
class DocumentController {

    private final DocumentRepository documentRepository;

    @PostMapping
    public Document create(@RequestBody DocumentCreateDTO dto) {
        Document document = Document.builder()
                .message(dto.message())
                .bankId(dto.bankId())
                .build();
        return documentRepository.save(document);
    }

    @GetMapping("/{bankId}/bank")
    public List<Document> getAllByPostID(@PathVariable Integer bankId) throws InterruptedException {
        log.info("Getting all documents by bank id: {}", bankId);
        /*if (false)
            throw new RuntimeException("Error From ELSHOD");
        *//*TimeUnit.SECONDS.sleep(2);*/
        return documentRepository.findAllByPostID(bankId);
    }

    @GetMapping("/{id}")
    public Document get(@PathVariable Integer id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));
    }
}

interface DocumentRepository extends JpaRepository<Document, Integer> {
    @Query("select t from Document t where t.bankId = :bankId")
    List<Document> findAllByPostID(@NonNull @Param("bankId") Integer id);
}

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Integer bankId;
}

record DocumentCreateDTO(@NotBlank String message, @Positive Integer bankId) {
}
