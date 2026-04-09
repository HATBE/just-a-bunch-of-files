package ch.hatbe.jbof.core.security;

import ch.hatbe.jbof.user.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class CurrentUserContext {
    private User currentUser;

    public User getRequiredUser() {
        if (this.currentUser == null) {
            throw new IllegalStateException("current user is not available for this request");
        }

        return this.currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
