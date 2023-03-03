package ru.practicum.compilation.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.event.model.Event;

import javax.persistence.*;
import java.util.List;

@Entity(name = "Compilation")
@Table(name = "compilations")
@Getter
@Setter
@Builder
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pinned")
    private Boolean pinned;

    @Column(name = "title", length = 120)
    private String title;

    @ManyToMany
    @JoinTable(name = "compilation_events",
        joinColumns = @JoinColumn(name = "compilation_id"),
        inverseJoinColumns = @JoinColumn(name = "event_id"))
    private List<Event> events;
}
