package com.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequestDto {

    @Schema(description = "사용자 이메일", example = "user@store.com")
    @NotBlank(message = "Email cannot be empty.")
    @Size(max = 150, message = "Email must be 150 characters or less.")
    @Email(message = "Invalid email format")
    private String username;

    @Schema(description = "비밀번호", example = "1234")
    @NotBlank(message = "Password cannot be empty.")
    @Size(max = 200, message = "Password must be 200 characters or less.")
    private String password;

}
