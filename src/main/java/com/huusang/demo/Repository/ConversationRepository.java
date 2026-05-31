package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Conversation;
import com.huusang.demo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE (c.buyer = :user1 AND c.seller = :user2) OR (c.buyer = :user2 AND c.seller = :user1)")
    Optional<Conversation> findBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT c FROM Conversation c WHERE c.buyer = :user OR c.seller = :user ORDER BY c.lastMessageAt DESC")
    List<Conversation> findAllByUserOrderByLastMessageAtDesc(@Param("user") User user);
}
