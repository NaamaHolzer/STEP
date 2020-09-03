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

// Class containing the query method to find available time ranges for a given meeting request.
public final class FindMeetingQuery {

    // Find optional time ranges for the meeting.
    public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
        ArrayList<TimeRange> mandatoryBusy = new ArrayList<>();
        ArrayList<TimeRange> optionalBusy = new ArrayList<>();
        ArrayList<TimeRange> mandatoryAvailable = new ArrayList<>();
        Collection<TimeRange> allAvailable = new ArrayList<>();

        // Iterate over the events and add to the mandatoryBusy array the time ranges of events in which mandatory attendees of the request participate.
        for (Event event : events) {
            Boolean areAttendeesSetsDisjoint = Collections.disjoint(request.getAttendees(), event.getAttendees());
            // If the request's attendees set and the event's attendees set aren't disjoint - the event's time range is a busy range.
            if (!areAttendeesSetsDisjoint) {
                mandatoryBusy.add(event.getWhen());
            }
            // If no mandatory attendee is in the event - check if there are optional attendees in it.
            else {
                Boolean areOptionalSetsDisjoint = Collections.disjoint(request.getOptionalAttendees(), event.getAttendees());
                if (!areOptionalSetsDisjoint) {
                    optionalBusy.add(event.getWhen());
                }
            }
        }
        Collections.sort(mandatoryBusy, TimeRange.ORDER_BY_START);
        Collections.sort(optionalBusy, TimeRange.ORDER_BY_START);

        // If there's no busy range - the whole day is available
        if(mandatoryBusy.isEmpty()) {
            TimeRange wholeDay = TimeRange.WHOLE_DAY;
            addIfRangeIsLongEnough(mandatoryAvailable, wholeDay, request);
            if (optionalBusy.isEmpty()) {
                return mandatoryAvailable;
            }
            checkOptionalAttendees(allAvailable, mandatoryAvailable, optionalBusy, request);
            return allAvailable.isEmpty() ? mandatoryAvailable : allAvailable;
        }

        // If the first busy range is not at the beginning of the day - the first time range is available.
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

        // If there are no available ranges or no optional attendees - there's no need to check the optional attendees' availability.
        if (mandatoryAvailable.isEmpty() || optionalBusy.isEmpty()) {
            return mandatoryAvailable;
        }

        // Check if there are ranges that are available for the optional attendees as well.
        checkOptionalAttendees(allAvailable, mandatoryAvailable, optionalBusy, request);
        
        return allAvailable.isEmpty() ? mandatoryAvailable : allAvailable;
    }

    public static void checkOptionalAttendees(Collection<TimeRange> allAvailable, ArrayList<TimeRange> mandatoryAvailable, ArrayList<TimeRange> optionalBusy, MeetingRequest request) {
        int opIndex = 0;
        Boolean moreOptionalBusy = true;
        // Iterate over the ranges that are available for the mandatory attendees to find ranges that are available for the optional attendees as well.
        for (int i = 0; i < mandatoryAvailable.size(); i++) {
            // If there are no more optional busy ranges then the rest of the available ranges are 'safe'.
            if (opIndex == optionalBusy.size()) {
                moreOptionalBusy = false;
            }
            if (!moreOptionalBusy || !mandatoryAvailable.get(i).overlaps(optionalBusy.get(opIndex))) {
               allAvailable.add(mandatoryAvailable.get(i));
            }
            // mandatoryAvailable[i] is not available for optional attendees.
            else {  
                // Subtract all optional busy ranges that overlap mandatoryAvailable[i] from mandatoryAvailable[i].
                ArrayList<TimeRange> subRanges = breakAvailableRange(mandatoryAvailable.get(i), optionalBusy.get(opIndex));
                while ((opIndex < (optionalBusy.size() - 1))  && (subRanges.get(subRanges.size() - 1).overlaps(optionalBusy.get(opIndex + 1)))) {
                    opIndex++;
                    ArrayList<TimeRange> lastRangeDivided = breakAvailableRange(subRanges.get(subRanges.size() - 1), optionalBusy.get(opIndex));
                    subRanges.remove(subRanges.size() - 1);
                    subRanges.addAll(lastRangeDivided);
                }
                for (TimeRange subRange : subRanges) {
                    addIfRangeIsLongEnough(allAvailable, subRange, request);
                }
                // Check if we need to increment the optionalBusy array's index.
                if (i < mandatoryAvailable.size() - 1) {
                    TimeRange nextRange = TimeRange.fromStartDuration(mandatoryAvailable.get(i + 1).start(), mandatoryAvailable.get(i + 1).duration());
                    if (nextRange.start() > optionalBusy.get(opIndex).end()) {
                        opIndex++;
                    }
                }
            }
        }
    }

    // Check if an available time range is long enough for the meeting request and if it is - add it to the list of the relevant ranges.
    public static void addIfRangeIsLongEnough (Collection<TimeRange> relevantRanges, TimeRange availableRange, MeetingRequest request) {
        if (availableRange.duration() >= request.getDuration()) {
            relevantRanges.add(availableRange);
        }
    }

    // Breaks an available range that overlaps with an optional busy range into 0/1/2 available ranges according to the situation.
    public static ArrayList<TimeRange> breakAvailableRange(TimeRange availableRange, TimeRange opBusyRange) {
        ArrayList<TimeRange> result = new ArrayList<>();

        // Option 1: (A - available, B - optional busy)
        // Events  :         |--A--|
        //                |------B----|
        // Result  :
        if (opBusyRange.contains(availableRange)) {
            return result;
        }

        // Option 2: (A - available, B - optional busy)
        // Events  :  |---------A----------|
        //                |-----B-----|
        // Result  :  |-1-|           |-2--|
        if (availableRange.contains(opBusyRange)) {
            TimeRange beforeOpBusy = TimeRange.fromStartEnd(availableRange.start(), opBusyRange.start(), false);
            TimeRange afterOpBusy = TimeRange.fromStartEnd(opBusyRange.end(), availableRange.end(), false);
            result.add(beforeOpBusy);
            result.add(afterOpBusy);
            return result;
        }

        // Option 3: (A - available, B - optional busy)
        // Events  :  |--A-------|
        //                 |-----B-----|
        // Result  :  |-1-|            
        if (availableRange.start() <= opBusyRange.start()) {
            TimeRange beforeOpBusy = TimeRange.fromStartEnd(availableRange.start(), opBusyRange.start(), false);
            result.add(beforeOpBusy);
            return result;
        }

        // Option 4: (A - available, B - optional busy)
        // Events  :       |--A-------|
        //            |-----B-----|
        // Result  :              |-1-|  
        else {
            TimeRange afterOpBusy = TimeRange.fromStartEnd(opBusyRange.end(), availableRange.end(), false);
            result.add(afterOpBusy);
            return result;
        }
    }
}
