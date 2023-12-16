package uz.kpi.telegramchatgpt.domain.entity.message;

import jakarta.persistence.Entity;
import lombok.*;
import uz.kpi.telegramchatgpt.domain.entity.BaseEntity;

@Entity(name = "messages")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class MessageEntity extends BaseEntity {
    private String role;
    private String content;

}
