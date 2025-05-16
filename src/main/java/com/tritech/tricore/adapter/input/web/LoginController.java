package com.tritech.tricore.adapter.input.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class LoginController {

    @Value("${redirect.login.success}")
    private String redirectUrl;

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
    }

    @GetMapping("login/success")
    public void loginSuccess(HttpServletResponse response) throws IOException {
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("login/fail")
    public void loginFailed(HttpServletResponse response) throws IOException {
        response.sendRedirect("http://localhost:3000/login");
    }
}
