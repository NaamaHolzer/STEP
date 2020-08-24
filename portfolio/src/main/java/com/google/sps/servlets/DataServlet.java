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

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Comment").addSort("rate", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Comment> comments = new ArrayList<>();
    ArrayList<String> likedOptionsVal = new ArrayList<>();
    // Add all the comments in the datastore to the comments list
    for (Entity entity : results.asIterable()) {
        String authorVal = (String) entity.getProperty("author");
        int rateVal = (int)(long) entity.getProperty("rate");
        likedOptionsVal.clear();
        if(((String) entity.getProperty("is_info_liked")).equals("true")){
           likedOptionsVal.add("The info");
        }
        if(((String) entity.getProperty("is_facts_liked")).equals("true")){
            likedOptionsVal.add("The facts");
        }
        if(((String) entity.getProperty("is_gallery_liked")).equals("true")){
            likedOptionsVal.add("The gallery");
        }
        if(((String) entity.getProperty("is_other_liked")).equals("true")){
            likedOptionsVal.add("Other");
        }
        String textVal = (String) entity.getProperty("text");
        Comment comment = new Comment(authorVal,rateVal,likedOptionsVal,textVal);
        comments.add(comment);
    }
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      // Create a new Comment entity based on the new comment that was received.
      Entity commentEntity = new Entity("Comment");
      String authorVal = getParameter(request,"author","Anonymous");
      int rateVal = Integer.parseInt(getParameter(request, "rate", "3"));
      setIsItemLiked(request,commentEntity,"info");
      setIsItemLiked(request,commentEntity,"facts");
      setIsItemLiked(request,commentEntity,"gallery");
      setIsItemLiked(request,commentEntity,"other");
      String textVal = getParameter(request,"text","");
      commentEntity.setProperty("author", authorVal);
      commentEntity.setProperty("rate", rateVal);
      commentEntity.setProperty("text", textVal);
      // Add comment to the datastore
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(commentEntity);
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

  // Sets the 'is_*item*_liked' property of an entity.
  private static void setIsItemLiked(HttpServletRequest request, Entity entity, String item){
     if(Boolean.parseBoolean(getParameter(request, "is_" + item + "_liked", "false"))){
          entity.setProperty("is_" + item + "_liked", "true");
      } 
      else{
          entity.setProperty("is_" + item + "_liked", "false");
      }
  }
}
