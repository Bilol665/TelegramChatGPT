package uz.kpi.telegramchatgpt.domain.entity.message;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uz.kpi.telegramchatgpt.domain.entity.BaseEntity;
import uz.kpi.telegramchatgpt.domain.entity.user.UserEntity;

import java.util.List;

@Entity(name = "chat")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class GPTChat extends BaseEntity {
    @ManyToOne(cascade = CascadeType.DETACH)
    private UserEntity user;
    @OneToMany(cascade = CascadeType.DETACH,fetch = FetchType.EAGER)
    private List<MessageEntity> messages;
    @Enumerated(EnumType.STRING)
    private ChatStatus status;
}
