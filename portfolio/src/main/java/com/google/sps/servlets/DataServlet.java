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
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
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
      Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
      
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);
      
      // Number of comments to display 
      int limit = Integer.parseInt(request.getParameter("limit"));
      List<Comment> comments = new ArrayList<>();
      // Add the number of comments specified by the user to the comments list
      for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(limit))) {
          String authorVal = (String) entity.getProperty("author");
          int rateVal = (int)(long) entity.getProperty("rate");
          ArrayList<String> likedOptionsVal = new ArrayList<>();
          if (Boolean.parseBoolean((String) entity.getProperty("is_info_liked"))) {
              likedOptionsVal.add("The info");
            }
          if (Boolean.parseBoolean((String) entity.getProperty("is_facts_liked"))) {
              likedOptionsVal.add("The facts");
            }
          if (Boolean.parseBoolean((String) entity.getProperty("is_gallery_liked"))) {
              likedOptionsVal.add("The gallery");
            }
          if (Boolean.parseBoolean((String) entity.getProperty("is_other_liked"))) {
              likedOptionsVal.add("Other");
            }
          String textVal = (String) entity.getProperty("text");
          long timestampVal = (long) entity.getProperty("timestamp");
          Comment comment = new Comment.Builder(timestampVal).byAuthor(authorVal).rated(rateVal).likedTheseOptions(likedOptionsVal).textWritten(textVal).build();
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

      UserService userService = UserServiceFactory.getUserService();
      String nickname = getUserNickname(userService.getCurrentUser().getUserId());
      String authorVal;

      // If the user has set a nickname - use it for the author field
      if (!nickname.equals("")) {
          authorVal = nickname;
      }
      // Otherwise - use his email address
      else {
          authorVal = userService.getCurrentUser().getEmail();
      }
      int rateVal = Integer.parseInt(getParameter(request, "rate", "3"));
      setIsItemLiked(request,commentEntity,"info");
      setIsItemLiked(request,commentEntity,"facts");
      setIsItemLiked(request,commentEntity,"gallery");
      setIsItemLiked(request,commentEntity,"other");
      String textVal = getParameter(request,"text","");
      long timestamp = System.currentTimeMillis();
      commentEntity.setProperty("author", authorVal);
      commentEntity.setProperty("rate", rateVal);
      commentEntity.setProperty("text", textVal);
      commentEntity.setProperty("timestamp",timestamp);
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
  private static void setIsItemLiked(HttpServletRequest request, Entity entity, String item) {
     String itemParameterName = "is_" + item + "_liked";
     if (Boolean.parseBoolean(getParameter(request, itemParameterName, "false"))) {
          entity.setProperty(itemParameterName, "true");
          updateChartData(item);
      } 
      else {
          entity.setProperty(itemParameterName, "false");
      }
  }

  // Returns the nickname of the user with id, or empty String if the user has not set a nickname.
  private String getUserNickname(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return "";
    }
    String nickname = (String) entity.getProperty("nickname");
    return nickname;
  }

  // Update the data of the chart representing how many likes each item got.
  private static void updateChartData(String itemLiked) {
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      // Find the entity that matches the item that was liked.
      FilterPredicate likedItemFilter = new Query.FilterPredicate("itemName", Query.FilterOperator.EQUAL, itemLiked);
      Query query = new Query("LikedItem").setFilter(likedItemFilter);
      PreparedQuery results = datastore.prepare(query);

      // If the entity exists - this item has been liked before - increment the counter by 1.
      if (results.countEntities(FetchOptions.Builder.withDefaults())>0) {
          Entity likedItemEntity = results.asSingleEntity();
          long newCount = (long)likedItemEntity.getProperty("count")+1;
          likedItemEntity.setProperty("count",newCount);
          datastore.put(likedItemEntity);
        }
      
      // If the entity doesn't exist - this is the first time this item is being liked - create the entity and set the counter to 1.
      else {
          Entity likedItemEntity = new Entity("LikedItem");
          likedItemEntity.setProperty("itemName",itemLiked);
          likedItemEntity.setProperty("count",1);
          datastore.put(likedItemEntity);
        }
    }
}
