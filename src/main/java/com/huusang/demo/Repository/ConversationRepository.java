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

    @Query("SELECT c FROM Conversation c WHERE (c.buyer.id = :user1Id AND c.seller.id = :user2Id) OR (c.buyer.id = :user2Id AND c.seller.id = :user1Id)")
    Optional<Conversation> findBetweenUsers(@Param("user1Id") String user1Id, @Param("user2Id") String user2Id);

    @Query("SELECT c FROM Conversation c WHERE c.buyer = :user OR c.seller = :user ORDER BY c.lastMessageAt DESC")
    List<Conversation> findAllByUserOrderByLastMessageAtDesc(@Param("user") User user);

    List<Conversation> findByBuyerOrderByLastMessageAtDesc(User buyer);

    List<Conversation> findBySellerOrderByLastMessageAtDesc(User seller);
}
