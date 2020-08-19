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

//Defining dictionaries in order to link the selected index to the wanted category.
const nameDict = {
    1: "pisu",
    2: "karzi"
};
const actionDict = {
    1: "Sleeping",
    2: "Eating",
    3: "Funny",
    4: "Busy"
};

/**
 * Finds the URL of the requested picture.
 */
function findWantedPicture(){
    const nameSelect=document.getElementById("names");
    const actionSelect=document.getElementById("actions");
    const chosenName=nameSelect.selectedIndex;
    const chosenAction=actionSelect.selectedIndex;

    //If no item was chosen from one of the lists - don't show any image.
    if(chosenAction===0||chosenName===0){
        return;
    }
    const imgUrl='images/'+nameDict[chosenName]+actionDict[chosenAction]+'.jpg';
    showImage(imgUrl); 

}

//Finds a URL of a random picture.
function findRandomPicture(){
    //Pick a random picture
    const imageIndex = Math.floor(Math.random() * 4) + 1;
    const imgUrl = 'images/both' + imageIndex + '.jpg';
    showImage(imgUrl);
    
}

//Displays the image located at a given URL on the screen.
function showImage(picUrl){
    const imgElement = document.createElement('img');
    imgElement.src = picUrl;
    imgElement.style.maxHeight="500px";
    imgElement.style.maxWidth="500px";
    const imageContainer = document.getElementById('imageContainer');
    imageContainer.style.textAlign=
    // Remove the previous image.
    imageContainer.innerHTML = '';
    imageContainer.appendChild(imgElement);
}