package com.store.domain.account;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_jwt_token_account_id", columnNames = "account_id")
        }
)
@Getter
public class JwtToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, length = 1000)
    private String refreshToken;

    public static JwtToken of(Account account, String refreshToken) {
        JwtToken jwtToken = new JwtToken();
        jwtToken.account = account;
        jwtToken.refreshToken = refreshToken;
        return jwtToken;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}
