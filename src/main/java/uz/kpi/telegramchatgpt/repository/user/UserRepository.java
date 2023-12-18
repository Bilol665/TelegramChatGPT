package uz.kpi.telegramchatgpt.repository.user;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.kpi.telegramchatgpt.domain.entity.user.LanguageEnum;
import uz.kpi.telegramchatgpt.domain.entity.user.UserEntity;
import uz.kpi.telegramchatgpt.domain.entity.user.UserState;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findUserEntityByChatId(Long chatId);

    Optional<UserEntity> findUserEntitiesByPhoneNumber(String phoneNumber);

    @Modifying
    @Query("update users s set s.state = :state where s.chatId = :chatId")
    void updateState(@Param("state") UserState state, @Param("chatId") Long chatId);

    @Modifying
    @Query("update users s set s.language = :language where s.chatId = :chatId")
    void updateLanguage(@Param("language") LanguageEnum language,@Param("chatId") Long chatId);

    @Query("select count(u) from users u")
    Integer countAll();
}
