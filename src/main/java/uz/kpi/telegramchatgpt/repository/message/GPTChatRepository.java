package uz.kpi.telegramchatgpt.repository.message;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.kpi.telegramchatgpt.domain.entity.message.GPTChat;
import uz.kpi.telegramchatgpt.domain.entity.user.UserEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface GPTChatRepository extends JpaRepository<GPTChat, UUID> {
    @Query("select f from chat f where f.user = :user and f.status = 'ACTIVE'")
    Optional<GPTChat> findGPTChatByUser(@Param("user") UserEntity user);
}
