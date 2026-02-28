package gm.zona_fit.infrastructure.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import gm.zona_fit.application.services.JwtService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Aspect
@Component
public class UsuarioScopeRepositoryAspect {

    private static final String FILTER_NAME = "usuarioScopeFilter";

    @PersistenceContext
    private EntityManager entityManager;

    private final JwtService jwtService;

    public UsuarioScopeRepositoryAspect(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Around("execution(* gm.zona_fit.infrastructure.IUsuarioRepository.*(..))")
    public Object applyUsuarioScope(ProceedingJoinPoint joinPoint) throws Throwable {
        Filter filter = null;

        try {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            var token = extractBearerToken(authentication);

            if (token != null) {
                var role = jwtService.extractRole(token);
                if ("CLIENTE".equals(role)) {
                    var userId = jwtService.extractUserId(token);
                    if (userId != null) {
                        Session session = entityManager.unwrap(Session.class);
                        filter = session.enableFilter(FILTER_NAME);
                        filter.setParameter("usuarioId", userId);
                    }
                }
            }

            return joinPoint.proceed();
        } finally {
            if (filter != null) {
                entityManager.unwrap(Session.class).disableFilter(FILTER_NAME);
            }
        }
    }

    private String extractBearerToken(Authentication authentication) {
        if (authentication == null || authentication.getDetails() == null) {
            return null;
        }

        var details = authentication.getDetails();
        if (!(details instanceof org.springframework.security.web.authentication.WebAuthenticationDetails webDetails)) {
            return null;
        }

        if (!(webDetails instanceof TokenAwareWebAuthenticationDetails tokenAware)) {
            return null;
        }

        return tokenAware.token();
    }
}
