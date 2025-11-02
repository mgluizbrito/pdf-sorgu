package io.github.mgluizbrito.PdfSorgu.repository;

import io.github.mgluizbrito.PdfSorgu.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
}
