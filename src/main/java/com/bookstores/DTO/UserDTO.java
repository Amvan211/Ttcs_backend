package com.bookstores.DTO;

import com.bookstores.common.ApiRoleNames;
import com.bookstores.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Integer id;
    private String username;
    private String fullName;
    private String mail;
    private String phone;
    private String status;
    private String roleName;
    private String avatarUrl;

    public static UserDTO fromEntity(User u) {
        return UserDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .fullName(u.getFullName())
                .mail(u.getMail())
                .phone(u.getPhone())
                .status(u.getStatus())
                .roleName(ApiRoleNames.toApi(u.getRole() != null ? u.getRole().getRoleName() : null))
                .avatarUrl(u.getAvatarUrl())
                .build();
    }
}
