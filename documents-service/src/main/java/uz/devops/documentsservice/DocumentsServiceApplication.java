package uz.devops.documentsservice;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SpringBootApplication
public class DocumentsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DocumentsServiceApplication.class, args);
    }
}

interface DocumentRepository extends JpaRepository<Document, Long> {
    @Query("select d from Document d where d.deleted = false")
    List<Document> findByDeletedFalse();
}


@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
class DocumentController{
    private final DocumentRepository documentRepository;

    @PostMapping
    public Document create(@RequestBody DocumentCreateDto dto){
        Document document =Document.builder()
                .title(dto.title())
                .description(dto.description())
                .deleted(false)
                .build();
        return documentRepository.save(document);
    }

    @GetMapping("/{id}")
    public Document get(@PathVariable Long id){
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found : " + id));
    }


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
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean deleted;

}

record DocumentCreateDto(
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank boolean deleted) {
}