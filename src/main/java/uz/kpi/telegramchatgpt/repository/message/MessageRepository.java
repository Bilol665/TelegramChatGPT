package uz.kpi.telegramchatgpt.repository.message;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.kpi.telegramchatgpt.domain.entity.message.MessageEntity;

import java.util.UUID;

@Repository
@Transactional
public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {

}
