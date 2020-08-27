// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Servlet returning login status and login/logout URLs.
@WebServlet("/login-info")
public class AuthServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("text/html;");
    UserService userService = UserServiceFactory.getUserService();

    // If the user is logged in - display his email address and a logout URL.
    if (userService.isUserLoggedIn()) {
        response.getWriter().println("<p id=\"isLoggedIn\">User is logged in</p>"); 
        response.getWriter().println("<p>" + userService.getCurrentUser().getEmail() + "</p>");
        response.getWriter().println("<p id=\"logoutUrl\">Logout <a href=\"" + userService.createLogoutURL("/") + "\">here</a>.</p>");
    }
    // If the user is not logged in - display a login URL.
    else {
        response.getWriter().println("<p id=\"isLoggedIn\">User is not logged in</p>");
        response.getWriter().println("<p id=\"loginUrl\">Login <a href=\"" + userService.createLoginURL("/") + "\">here</a> to leave a comment.</p>");
    }
  }

}
