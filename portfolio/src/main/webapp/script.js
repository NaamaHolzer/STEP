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

/**
 * Adds a random fact to the page.
 */
function addRandomFact() {
  const facts =
      ["I'm learning Arabic", 'I live in Jerusalem', 'I love animals', 'I went to ballet classes for almost 10 years'];

  // Pick a random greeting.
  const fact = facts[Math.floor(Math.random() * facts.length)];
  console.log(fact);

  // Add it to the page.
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

// Invoked when the page is loaded. Gets the comments list from the server and displays it.
function getCommentsFromServer() {
    const lim = document.getElementById("limit").value;
    fetch('/data?limit='+lim).then(response => response.json()).then((allComments) => {
    const totalEl = document.getElementById('comments-container');
    let commentStr = '';
        for (i = 0;i < allComments.length;i++) {
            commentStr = allComments[i].author+": Rate: "+allComments[i].rate+". ";
            // If the list of the liked items is not empty - display the liked items. 
            if ((allComments[i].likedOptions).length!==0) {
                commentStr += "Liked: "+allComments[i].likedOptions+". ";
            }
            // If the comment includes text - display it.
            if (allComments[i].text!=='') {
                commentStr += "Comment: "+allComments[i].text;
            }
            commentEl = createPElement(commentStr);
            totalEl.appendChild(commentEl);
        }
    });
}

// Invoked when the user sets a new maximum number of comments displayed
function changeNumberOfComments() {
    let commentCont = document.getElementById("comments-container");
    // Clear out old comments before inserting the new response
    while (commentCont.childElementCount>1) {
        commentCont.removeChild(commentCont.lastChild);
    }
    // Insert new response
    getCommentsFromServer();
}

function createPElement(text) {
  const pElement = document.createElement('p');
  pElement.innerText = text;
  return pElement;
}



