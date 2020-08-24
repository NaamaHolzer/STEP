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

import com.google.sps.data.Comment;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
// List of comments the portfolio received.
 private List<Comment> comments;

  @Override
  public void init() {
    comments = new ArrayList<>();
  }
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    String json = new Gson().toJson(comments);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      ArrayList<String> likedOptionsVal = new ArrayList<>();
      // Creating a new Comment instance based on the new comment that was received.
      String authorVal = getParameter(request,"author","Anonymous");
      // The received rate. Default value is 3.
      int rateVal = Integer.parseInt(getParameter(request, "rate", "3"));
      // Add only checked checkboxes to the 'liked' list.
      if(Boolean.parseBoolean(getParameter(request, "is_info_liked'", "false"))){
          likedOptionsVal.add("The information");
      }
      if(Boolean.parseBoolean(getParameter(request, "is_facts_liked'", "false"))){
          likedOptionsVal.add("The facts");
      }
      if(Boolean.parseBoolean(getParameter(request, "is_gallery_liked'", "false"))){
          likedOptionsVal.add("The gallery");
      }
      if(Boolean.parseBoolean(getParameter(request, "is_other_liked", "false"))){
          likedOptionsVal.add("Other");
      }
      String textVal = getParameter(request,"text","");
      Comment newComment = new Comment(authorVal,rateVal,likedOptionsVal,textVal);
      // Add the new comment to the comments list.
      comments.add(newComment);
      response.sendRedirect("/index.html"); 
  }
 /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private static String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}
