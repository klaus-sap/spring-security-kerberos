/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.security.extensions.kerberos.web;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.extensions.kerberos.KerberosServiceRequestToken;
import org.springframework.web.filter.GenericFilterBean;

/**
 * 
 * @author Mike Wiesner
 * @since 1.0
 * @version $Id: $
 */
public class SpnegoAuthenticationProcessingFilter extends GenericFilterBean {

	private AuthenticationManager authenticationManager;

	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		String header = request.getHeader("Authorization");

		if ((header != null) && header.startsWith("Negotiate ")) {
			String base64Token = header.substring(10);
			byte[] kerberosTicket = Base64.decodeBase64(base64Token.trim()
					.getBytes());
			KerberosServiceRequestToken authenticationRequest = new KerberosServiceRequestToken(
					kerberosTicket);
			Authentication authentication;
			try {
				authentication = authenticationManager
						.authenticate(authenticationRequest);
			} catch (AuthenticationException e) {
				// That shouldn't happen, as it is most likely a wrong configuration on server side
				SecurityContextHolder.clearContext();
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.flushBuffer();
				return;
			}
			SecurityContextHolder.getContext()
					.setAuthentication(authentication);
		}

		chain.doFilter(request, response);

	}

}
