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

google.charts.load('current', {'packages':['corechart']});

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
 
// Invoked when the page is loaded
function preparePage() {
    getCommentsFromServer();
    displayFormIfLoggedIn();
    drawActivitiesChart();
    drawCommentsDataChart();
    createMap();
}
 
// Gets the comments list from the server and displays it.
function getCommentsFromServer() {
    const lim = document.getElementById("limit").value;
    fetch('/data?limit='+lim).then(response => response.json()).then((allComments) => {
    const totalEl = document.getElementById('comments-container');
    totalEl.innerHTML = '';
    let commentStr = '';
        for (i = 0;i < allComments.length;i++) {
            commentStr = "Author: " + allComments[i].author+": ";
            commentStr += "Rate: " + allComments[i].rate+". ";
            // If the list of the liked items is not empty - display the liked items. 
            if ((allComments[i].likedOptions).length!==0) {
                commentStr += "Liked: " + allComments[i].likedOptions+". ";
            }
            // If the comment includes text - display it.
            if (allComments[i].text!=='') {
                commentStr += "Comment: " + allComments[i].text;
            }
            commentEl = createPElement(commentStr);
            commentEl.style.border = "thin solid gray";
            commentEl.style.width = "650px";
            commentEl.style.margin = "auto";
            totalEl.appendChild(commentEl);
        }
        if (allComments.length==0) {
            document.getElementById("deleteButton").disabled = true;
        }
        else {
            document.getElementById("deleteButton").disabled = false;
        }
    });
}
 
// Fetch the login status from the servlet. If the user is logged in display comments form and a logout URL. Otherwise - display a login URL.
function displayFormIfLoggedIn() {
    fetch('/login-info').then(response => response.text()).then((textRes) => {
        // Parse the text from the response to an html page
        const parser = new DOMParser();
        const htmlRes = parser.parseFromString(textRes, "text/html");
        
        const isLoggedIn = htmlRes.getElementById('isLoggedIn').innerText;
        // Display form and logout URL if logged in
        if (isLoggedIn === "User is logged in") {
            document.getElementById("comments-form").style.display = "block";
            const logoutUrl = htmlRes.getElementById("logoutUrl");
            document.getElementById("comments-form").appendChild(logoutUrl);

            // Get nickname information from the nickname servlet
            fetch('/nickname').then(nicknameResponse => nicknameResponse.text()).then((textNickname) => {
                // Parse the text from the response to an html page
                const htmlNickname = parser.parseFromString(textNickname, "text/html");
                const nicknameForm = htmlNickname.getElementById("nickname");
                document.getElementById("nickname-setter").appendChild(nicknameForm);
            });

        }
        // Display login URL if not logged in
        else {
            const loginUrl = htmlRes.getElementById("loginUrl");
            document.getElementById("new-comment").appendChild(loginUrl);
        }
    });
}
 
// Delete all comments from the server and remove them from the portfolio page
function deleteComments() {
    fetch('/delete-data', {method: 'POST'}).then(getCommentsFromServer());
    location.reload();
}
 
// Create the activities chart and add it to the page
function drawActivitiesChart() {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Activity');
    data.addColumn('number', 'Count');
    data.addRows([
        ['Read', 2],
        ['Work', 9],
        ['Sleep', 7],
        ['Spend time on my phone', 3],
        ['Other', 3]
    ]);
    
    const options = {
        'title': 'Daily Activities',
        'width':500,
        'height':400
    };
    
    const chart = new google.visualization.PieChart(
        document.getElementById('activities-chart-container'));
    chart.draw(data, options);
}

// Draw the comments chart based on the items that were liked
function drawCommentsDataChart() {
    fetch('/chart').then(response => response.json())
    .then((likesCounters) => {
        const data = new google.visualization.DataTable();
        data.addColumn('string', 'Item');
        data.addColumn('number', 'Likes');
        Object.keys(likesCounters).forEach((itemName) => {
            data.addRow([itemName, likesCounters[itemName]]);
        });
        
        const options = {
            'title': 'Likes Received',
            'width':600,
            'height':500
        };

        const chart = new google.visualization.ColumnChart(
            document.getElementById('comments-chart-container'));
        chart.draw(data, options); 
    });
}

// Create the map and add it to the main page
function createMap() {
    const map = new google.maps.Map(
        document.getElementById('map'),
        {center: {lat: 31.779, lng: 35.224}, zoom: 13});
    
    addMapMarker(map, 31.784, 35.212, "Machane Yehuda Market");
    addMapMarker(map, 31.774, 35.177, "Mt. Herzl");
    addMapMarker(map, 31.776, 35.234, "The Old City");
    addMapMarker(map, 31.78, 35.2, "Sacher Park");
    addMapMarker(map, 31.751, 35.187, "Malcha Mall");
}

// Add a marker to the map in a given location
function addMapMarker(mapVal, latVal,lngVal,markerName) {
    const marker = new google.maps.Marker({
        position: {lat: latVal, lng: lngVal},
        map: mapVal,
        title: markerName
    });
}

function createPElement(text) {
  const pElement = document.createElement('p');
  pElement.innerText = text;
  return pElement;
}
