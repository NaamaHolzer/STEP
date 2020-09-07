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
        
        Collection<TimeRange> mandatoryAvailable = new ArrayList<>();
        ArrayList<TimeRange> mandatoryBusy = new ArrayList<>();
        
        // Iterate over the events and add to the mandatoryBusy array the time ranges of events in which mandatory attendees of the request participate
        for (Event event : events) {
            Boolean areAttendeesSetsDisjoint = Collections.disjoint(request.getAttendees(), event.getAttendees());
            // If the request's attendees set and the event's attendees set aren't disjoint - the event's time range is a busy range
            if (!areAttendeesSetsDisjoint) {
                mandatoryBusy.add(event.getWhen());
            }
        }
        Collections.sort(mandatoryBusy,TimeRange.ORDER_BY_START);
        // If there's no busy range - the whole day is available
        if(mandatoryBusy.isEmpty()) {
            TimeRange wholeDay = TimeRange.WHOLE_DAY;
            addIfRangeIsLongEnough(mandatoryAvailable, wholeDay, request);
            return mandatoryAvailable;
        }
        // If the first busy range is not at the beginning of the day - the first time range is available
        if (mandatoryBusy.get(0).start() != TimeRange.START_OF_DAY) {
            TimeRange firstRange = TimeRange.fromStartDuration(TimeRange.START_OF_DAY, mandatoryBusy.get(0).start() - TimeRange.START_OF_DAY);
            addIfRangeIsLongEnough(mandatoryAvailable, firstRange, request);
        }

        TimeRange currBusy = TimeRange.fromStartDuration(mandatoryBusy.get(0).start(), mandatoryBusy.get(0).duration());
        // Iterate over the busy time ranges and try to find available ranges between them
        for (int i = 0; i < mandatoryBusy.size(); i++) {
            // If mandatoryBusy[i] is within the current busy range - move on to the next range. This doesn't change our current busy range.
            if (currBusy.contains(mandatoryBusy.get(i)) && !currBusy.equals(mandatoryBusy.get(i))) {
                continue;
            }
            // If the current busy range and mandatoryBusy[i] overlap - update the current busy range to contain mandatoryBusy[i] as well.
            if (currBusy.overlaps(mandatoryBusy.get(i)) && !currBusy.equals(mandatoryBusy.get(i))) {
                currBusy = TimeRange.fromStartDuration(currBusy.start(), mandatoryBusy.get(i).end() - currBusy.start());
            }
            // If they don't overlap - there's an available range between them
            else {
                TimeRange availableRange = TimeRange.fromStartDuration(currBusy.end(), mandatoryBusy.get(i).start() - currBusy.end());
                addIfRangeIsLongEnough(mandatoryAvailable, availableRange, request);
                //Update currBusy to be mandatoryBusy[i]
                currBusy = TimeRange.fromStartDuration(mandatoryBusy.get(i).start(), mandatoryBusy.get(i).duration());
            }
        }
        // If there's time left between the last busy range and the end of the day - it's available
        if (!currBusy.contains(TimeRange.END_OF_DAY)) {
            TimeRange lastRange = TimeRange.fromStartDuration(currBusy.end(), TimeRange.END_OF_DAY - currBusy.end() + 1);
            addIfRangeIsLongEnough(mandatoryAvailable, lastRange, request);
        }

        return mandatoryAvailable;
    }

    // Check if an available time range is long enough for the meeting request and if it is - add it to the list of the relevant ranges.
    public static void addIfRangeIsLongEnough(Collection<TimeRange> relevantRanges, TimeRange availableRange, MeetingRequest request) {
        if (availableRange.duration() >= request.getDuration()) {
            relevantRanges.add(availableRange);
        }
    }
}
