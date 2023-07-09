package ru.practicum.events.event.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.category.model.Category;
import ru.practicum.events.event.model.Event;
import ru.practicum.events.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findEventByCategoryIs(Category category);

    Set<Event> findAllByIdIsIn(List<Long> id);

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    @Query(value = "SELECT e " +
            "FROM Event e " +
            "WHERE (e.initiator.id IN :users OR :users = NULL) " +
            "AND (e.category.id IN :categories  OR :categories = NULL) " +
            "AND (e.eventDate >= :rangeStart)  " +
            "OR CAST(:rangeStart AS date) = NULL " +
            "AND (e.eventDate <= :rangeEnd)   " +
            "OR CAST(:rangeEnd AS date) = NULL")
    List<Event> findAllByAdmin(@Param("users") List<Long> users,
                               @Param("categories") List<Long> categories,
                               @Param("rangeStart") LocalDateTime rangeStart,
                               @Param("rangeEnd") LocalDateTime rangeEnd,
                               Pageable page);

    @Query("SELECT e FROM Event e " +
            "WHERE (e.initiator.id IN (:users) OR :users = NULL) " +
            "AND (e.state IN (:states) OR :states = NULL) " +
            "AND (e.category.id IN (:categories) OR :categories = NULL) " +
            "AND (e.eventDate>=:rangeStart OR CAST(:rangeStart AS date) = NULL) " +
            "AND (e.eventDate<=:rangeEnd OR CAST(:rangeEnd AS date) = NULL)")
    List<Event> findAllByAdminAndState(@Param("users") List<Long> users,
                                       @Param("states") List<EventState> states,
                                       @Param("categories") List<Long> categories,
                                       @Param("rangeStart") LocalDateTime rangeStart,
                                       @Param("rangeEnd") LocalDateTime rangeEnd,
                                       Pageable page);

    @Query(value = "SELECT * " +
            "FROM events  " +
            "WHERE (lower(annotation) LIKE '%'||lower(:text)||'%' OR lower(description) LIKE '%'||lower(:text)||'%') " +
            "AND (category_id IN :categories  OR :categories IS NULL) " +
            "AND (:paid IS NULL OR paid = :paid) " +
            "AND (event_date BETWEEN " +
            "to_timestamp(:rangeStart, 'yyyy-mm-dd hh24:mi:ss') AND to_timestamp(:rangeEnd, 'yyyy-mm-dd hh24:mi:ss') " +
            "OR event_date > CURRENT_TIMESTAMP) " +
            "ORDER BY lower(:sort) " +
            "OFFSET :from " +
            "LIMIT :size", nativeQuery = true)
    List<Event> findAllByPublic(@Param("text") String text,
                                @Param("categories") List<Long> categories,
                                @Param("paid") Boolean paid,
                                @Param("rangeStart") String rangeStart,
                                @Param("rangeEnd") String rangeEnd,
                                @Param("sort") String sort,
                                @Param("from") Integer from,
                                @Param("size") Integer size);

    Optional<Event> findEventByIdAndStateIs(Long id, EventState state);

}
