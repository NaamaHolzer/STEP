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

package com.google.sps.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a comment that was submitted on the portfolio page and includes the comment's possible fields.
 * <p>Note: The private variables in this class are converted into JSON.
 */
public class Comment {
    // Builder design pattern to create Comment instances
    public static class Builder {
        private String author; 
        private int rate;
        private ArrayList<String> likedOptions;
        private String text;
        private final long timestamp;

        public Builder(long newTimestamp) {
            this.timestamp = newTimestamp;
        }
        public Builder byAuthor(String newAuthor){
            this.author = newAuthor;
            return this;
        }
        public Builder rated(int newRate){
            this.rate = newRate;
            return this;
        }
        public Builder likedTheseOptions(ArrayList<String> newLikedOptions){
            this.likedOptions = new ArrayList<String>(newLikedOptions);
            return this;
        }
        public Builder textWritten(String newText){
            this.text = newText;
            return this;
        }

        public Comment build(){
            // Create the comment
            return new Comment(this);
        }
    }

    private String author;
    // The 1-5 rate users can give.
    private int rate;
    // The options marked as 'liked' by the user.
    private ArrayList<String> likedOptions;
    private String text;
    // Timestamp to enable sorting the comments
    private final long timestamp;
    public Comment(Builder builder) {
        this.author = builder.author;
        this.rate = builder.rate;
        this.likedOptions = new ArrayList<String>(builder.likedOptions);
        this.text = builder.text;
        this.timestamp = builder.timestamp;
    }
}