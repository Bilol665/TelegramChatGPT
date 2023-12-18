package uz.kpi.telegramchatgpt.domain.entity.user;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import uz.kpi.telegramchatgpt.domain.entity.BaseEntity;

@Entity(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserEntity extends BaseEntity {
    private Long chatId;
    private Boolean isFirstTime;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String username;
    private String bio;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    @Enumerated(EnumType.STRING)
    private LanguageEnum language;
    @Enumerated(EnumType.STRING)
    private UserState state;
}
