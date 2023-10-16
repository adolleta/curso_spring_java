package com.juice.orangejuice.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;

import com.juice.orangejuice.user.IUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {
    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var servletPath = request.getServletPath();
        if (servletPath.startsWith("/tasks/")) {

            //pegar a autenticação do usuário (username e senha)
            var authorization = request.getHeader("Authorization");
            var encode = authorization.substring("Basic".length()).trim();

            byte[] decode = Base64.getDecoder().decode(encode);
            var auth = new String(decode);

            String[] credentials = auth.split(":");
            String username = credentials[0];
            String password = credentials[1];

            //validar o usuário
            var user = this.userRepository.findByUsername(username);
            if (user == null) {
                response.sendError(401, "Usuário ou senha inválidos");
            } else {
                //validar a senha
                var verifyPassword = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if (verifyPassword.verified) {
                    request.setAttribute("idUser", user.getId());
                    //deu bom :o)
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401, "Senha inválida.");
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
