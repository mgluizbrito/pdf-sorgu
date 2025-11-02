package io.github.mgluizbrito.PdfSorgu.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
public enum RoleEnum implements GrantedAuthority {

    ROLE_ADMIN("Full access, can manage users and documents."),
    ROLE_USER("Can upload documents and RAG queries."),
    ROLE_GUEST("Can perform limited RAG queries.");

    private final String description;

    RoleEnum(String s) {
        this.description = s;
    }

    @Override
    public String getAuthority() {
        return name();
    }
}
