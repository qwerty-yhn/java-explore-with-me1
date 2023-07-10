package ru.practicum.explorewithme.stats.server.hit.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
@Table(name = "hits", schema = "public")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Hit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "app")
    private String app;
    @Column(name = "uri")
    private String uri;
    @Column(name = "ip")
    private String ip;
    @Column(name = "time_stamp")
    private LocalDateTime timestamp;
}
