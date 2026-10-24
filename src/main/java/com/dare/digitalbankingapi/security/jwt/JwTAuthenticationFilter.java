package com.dare.digitalbankingapi.security.jwt;

import com.dare.digitalbankingapi.security.handler.CustomAuthenticationEntryPoint;
import com.dare.digitalbankingapi.security.principal.AuthenticatedUser;
import com.dare.digitalbankingapi.user.entity.UserEntity;
import com.dare.digitalbankingapi.user.entity.UserRole;
import com.dare.digitalbankingapi.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwTAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final CustomAuthenticationEntryPoint authenticationEntryPoint;
	private final UserRepository userRepository;

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
									@NonNull HttpServletResponse response,
									@NonNull FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			String token = authHeader.substring(7);
			String email = jwtUtil.extractEmail(token);
			Long userId = jwtUtil.extractUserId(token);

			UserEntity user = userRepository.findById(userId)
					.orElseThrow(() ->
							new BadCredentialsException("Invalid JWT token")
					);

			if (!user.getEmail().equals(email)) {
				throw new BadCredentialsException("Invalid JWT token");
			}

			UserRole role = user.getUserRole();

			AuthenticatedUser principal = new AuthenticatedUser(userId, email, role);

			UsernamePasswordAuthenticationToken authenticationToken =
					new UsernamePasswordAuthenticationToken(
							principal,
							null,
							List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
					);
			SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		} catch (Exception e) {
			SecurityContextHolder.clearContext();

			log.debug("JWT authentication failed: {}", e.getClass().getSimpleName());

			authenticationEntryPoint.commence(
					request,
					response,
					new BadCredentialsException("Bad or invalid JWT token", e)
			);
			return;
		}

		filterChain.doFilter(request, response);

	}
}
