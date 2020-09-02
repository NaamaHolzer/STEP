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

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

// Class containing the query method to find available time ranges for a given meeting request
public final class FindMeetingQuery {
    // Find optional time ranges for the meeting
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        
        // A collection to contain the relevant available time ranges
        Collection<TimeRange> optionalRanges = new ArrayList<>();
        // A collection containing the time ranges in which attendees of the request are busy
        ArrayList<TimeRange> busyRanges = new ArrayList<>();
        
        // Iterate over the events and add to the busyRanges array the time ranges of events in which attendees of the request participate
        for (Event event : events) {
            Boolean areAttendeesSetsDisjoint = Collections.disjoint(request.getAttendees(),event.getAttendees());
            // If the request's attendees set and the event's attendees set aren't disjoint - the event's time range is a busy range
            if (!areAttendeesSetsDisjoint) {
                busyRanges.add(event.getWhen());
            }
        }
        Collections.sort(busyRanges,TimeRange.ORDER_BY_START);
        // If there's no busy range - the whole day is available
        if(busyRanges.isEmpty()) {
            TimeRange wholeDay = TimeRange.WHOLE_DAY;
            addIfRangeIsLongEnough(optionalRanges,wholeDay,request);
            return optionalRanges;
        }
        // If the first busy range is not at the beginning of the day - the first time range is available
        if (busyRanges.get(0).start() != TimeRange.START_OF_DAY) {
            TimeRange firstRange = TimeRange.fromStartDuration(TimeRange.START_OF_DAY,busyRanges.get(0).start()-TimeRange.START_OF_DAY);
            addIfRangeIsLongEnough(optionalRanges,firstRange,request);
        }

        TimeRange currBusy = TimeRange.fromStartDuration(busyRanges.get(0).start(),busyRanges.get(0).duration());
        // Iterate over the busy time ranges and try to find available ranges between them
        for (int i=0; i<busyRanges.size(); i++) {
            // If busyRanges[i] is within the current busy range - move on to the next range. This doesn't change our current busy range
            if (currBusy.contains(busyRanges.get(i)) && !currBusy.equals(busyRanges.get(i))) {
                continue;
            }
            // If the current busy range and busyRanges[i] overlap - update the current busy range to contain busyRanges[i] as well.
            if (currBusy.overlaps(busyRanges.get(i)) && !currBusy.equals(busyRanges.get(i))) {
                currBusy = TimeRange.fromStartDuration(currBusy.start(),busyRanges.get(i).end() - currBusy.start());
            }
            // If they don't overlap - there's an available range between them
            else {
                TimeRange availableRange = TimeRange.fromStartDuration(currBusy.end(),busyRanges.get(i).start() - currBusy.end());
                addIfRangeIsLongEnough(optionalRanges,availableRange,request);
                //Update currBusy to be busyRanges[i]
                currBusy = TimeRange.fromStartDuration(busyRanges.get(i).start(),busyRanges.get(i).duration());
            }
        }
        // If there's time left between the last busy range and the end of the day - it's available
        if (!currBusy.contains(TimeRange.END_OF_DAY)) {
            TimeRange lastRange = TimeRange.fromStartDuration(currBusy.end(),TimeRange.END_OF_DAY - currBusy.end() + 1);
            addIfRangeIsLongEnough(optionalRanges,lastRange,request);
        }
        return optionalRanges;
    }

    // Check if an available time range is long enough for the meeting request and if it is - add it to the list of the optional ranges
    public static void addIfRangeIsLongEnough (Collection<TimeRange> optionalRanges,TimeRange availableRange, MeetingRequest request) {
        if (availableRange.duration() >= request.getDuration()) {
            optionalRanges.add(availableRange);
        }
    }
}
