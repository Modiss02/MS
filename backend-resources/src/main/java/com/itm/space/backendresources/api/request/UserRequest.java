package com.itm.space.backendresources.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
public class UserRequest {
    @NotBlank(message = "Username should not be blank")
    @Size(min = 2, max = 30, message = "Username should be between 2 and 30 characters long")
    private  String username;
    @NotBlank(message = "Email should not be blank")
    @Email(message = "Email should be valid", regexp = ".+@.+\\..+")
    private  String email;
    @NotBlank(message = "Password should not be blank")
    @Size(min = 4, message = "Password should be greater than 4 characters long")
    private  String password;
    @NotBlank
    private  String firstName;
    @NotBlank
    private  String lastName;
}

