package ru.practicum.ewm.user.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> getUsersByIdIn(List<Long> ids, Pageable pageable);

    Optional<User> findByEmail(String email);

    User getUserById(Long userId);
}
