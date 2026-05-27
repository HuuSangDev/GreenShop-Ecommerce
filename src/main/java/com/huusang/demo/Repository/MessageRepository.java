package com.huusang.demo.Repository;

import com.huusang.demo.Entity.Conversation;
import com.huusang.demo.Entity.Message;
import com.huusang.demo.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationOrderByCreatedAtDesc(Conversation conversation, Pageable pageable);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation = :conversation AND m.sender != :currentUser AND m.isRead = false")
    void markAsReadByConversationAndOtherSender(@Param("conversation") Conversation conversation, @Param("currentUser") User currentUser);

    @Query("SELECT COUNT(m) FROM Message m WHERE (m.conversation.buyer = :currentUser OR m.conversation.seller = :currentUser) AND m.sender != :currentUser AND m.isRead = false")
    long countUnreadMessagesForUser(@Param("currentUser") User currentUser);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation = :conversation AND m.sender != :currentUser AND m.isRead = false")
    long countUnreadMessagesForUserInConversation(@Param("conversation") Conversation conversation, @Param("currentUser") User currentUser);
}
