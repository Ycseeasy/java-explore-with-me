package ru.practicum.ewm.comment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "comments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String text;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column
    private LocalDateTime created;
}