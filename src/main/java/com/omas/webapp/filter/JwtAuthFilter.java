package com.omas.webapp.filter;

import com.omas.webapp.service.LoginAttemptService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException; 
import jakarta.servlet.http.HttpServletRequest; 
import jakarta.servlet.http.HttpServletResponse; 
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; 
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; 
import org.springframework.stereotype.Component; 
import org.springframework.web.filter.OncePerRequestFilter;
import com.omas.webapp.service.JwtService;
import com.omas.webapp.service.UserInfoDetails;
import com.omas.webapp.service.UserService;

import java.io.IOException; 

@Component
public class JwtAuthFilter extends OncePerRequestFilter { 

	@Autowired
	private JwtService jwtService; 

	@Autowired
	private UserService userService;

	@Autowired
	private LoginAttemptService loginAttemptService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		String token = null; 
		String username = null;

		if (authHeader != null && authHeader.startsWith("Bearer ")) { 
			token = authHeader.substring(7); 
			username = jwtService.extractUsername(token); 
		} 

		if (!loginAttemptService.isBlocked() && username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

			UserInfoDetails userInfoDetails = userService.loadUserByUsername(username);

			if (jwtService.validateToken(token, userInfoDetails)) {
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userInfoDetails, null, userInfoDetails.getAuthorities()); 
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); 
				SecurityContextHolder.getContext().setAuthentication(authToken); 
			} else {
				loginAttemptService.loginFailed();
			}
		}

		filterChain.doFilter(request, response);
	} 
} 
