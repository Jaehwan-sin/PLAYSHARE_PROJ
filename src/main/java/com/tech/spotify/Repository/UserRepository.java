package com.tech.spotify.Repository;

import com.tech.spotify.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    User findOneById(Long id);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.providerID = :providerID")
    User findByEmailAndproviderID(@Param("email") String email, @Param("providerID") String providerID);

    User findByUsername(String username);
}
