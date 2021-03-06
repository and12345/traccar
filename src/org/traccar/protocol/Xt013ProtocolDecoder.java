/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.traccar.protocol;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.traccar.BaseProtocolDecoder;
import org.traccar.helper.UnitsConverter;
import org.traccar.model.Event;
import org.traccar.model.Position;

import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Xt013ProtocolDecoder extends BaseProtocolDecoder {

    public Xt013ProtocolDecoder(Xt013Protocol protocol) {
        super(protocol);
    }

    private static final Pattern pattern = Pattern.compile(
            "(?:HI,\\d+)?" +
            "TK," +
            "(\\d+)," +                         // IMEI
            "(\\d{2})(\\d{2})(\\d{2})" +        // Date (YYMMDD)
            "(\\d{2})(\\d{2})(\\d{2})," +       // Time (HHMMSS)
            "([+-]\\d+\\.\\d+)," +              // Latitude
            "([+-]\\d+\\.\\d+)," +              // Longitude
            "(\\d+)," +                         // Speed
            "(\\d+)," +                         // Course
            "\\d+," +
            "(\\d+)," +                         // Altitude
            "([FL])," +                         // GPS fix
            "\\d+," +
            "(\\d+)," +                         // GPS level
            "\\p{XDigit}+," +
            "\\p{XDigit}+," +
            "(\\d+)," +                         // GSM level
            "[^,]*," +
            "(\\d+\\.\\d+)," +                  // Battery
            "(\\d)," +                          // Charging
            ".*");

    @Override
    protected Object decode(
            ChannelHandlerContext ctx, Channel channel, Object msg)
            throws Exception {

        // Parse message
        String sentence = (String) msg;
        Matcher parser = pattern.matcher(sentence);
        if (!parser.matches()) {
            throw new ParseException(null, 0);
        }

        // Create new position
        Position position = new Position();
        position.setProtocol(getProtocolName());

        Integer index = 1;

        // Identify device
        if (!identify(parser.group(index++), channel)) {
            return null;
        }
        position.setDeviceId(getDeviceId());

        // Time
        Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        time.clear();
        time.set(Calendar.YEAR, 2000 + Integer.valueOf(parser.group(index++)));
        time.set(Calendar.MONTH, Integer.valueOf(parser.group(index++)) - 1);
        time.set(Calendar.DAY_OF_MONTH, Integer.valueOf(parser.group(index++)));
        time.set(Calendar.HOUR_OF_DAY, Integer.valueOf(parser.group(index++)));
        time.set(Calendar.MINUTE, Integer.valueOf(parser.group(index++)));
        time.set(Calendar.SECOND, Integer.valueOf(parser.group(index++)));
        position.setTime(time.getTime());

        // Location
        position.setLatitude(Double.valueOf(parser.group(index++)));
        position.setLongitude(Double.valueOf(parser.group(index++)));
        position.setSpeed(UnitsConverter.knotsFromKph(Double.valueOf(parser.group(index++))));
        position.setCourse(Double.valueOf(parser.group(index++)));
        position.setAltitude(Double.valueOf(parser.group(index++)));
        position.setValid(parser.group(index++).equals("F"));

        // Other
        position.set(Event.KEY_GPS, parser.group(index++));
        position.set(Event.KEY_GSM, parser.group(index++));
        position.set(Event.KEY_BATTERY, parser.group(index++));
        position.set(Event.KEY_CHARGE, parser.group(index++));
        return position;
    }

}
