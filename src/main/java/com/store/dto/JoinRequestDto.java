package com.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class JoinRequestDto {

    @Schema(description = "사용자 이메일", example = "user2@store.com")
    @NotBlank(message = "Email cannot be empty.")
    @Size(max = 150, message = "Email must be 150 characters or less.")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "비밀번호", example = "1234")
    @NotBlank(message = "Password cannot be empty.")
    @Size(max = 200, message = "Password must be 200 characters or less.")
    @Setter
    private String password;

    @Schema(description = "사용자 닉네임", example = "user2")
    @Size(max = 100, message = "Nickname must be 100 characters or less.")
    private String nickname;

}
