package com.ecomove.auth.service;

import jakarta.servlet.http.HttpServletResponse;

public interface SsoService {

    String initiateEntraLogin();

    String handleEntraCallback(String code, String state, HttpServletResponse httpResponse);
}
