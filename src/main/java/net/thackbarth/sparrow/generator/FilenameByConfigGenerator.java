/*
 * Copyright 2013 Thomas Hackbarth (mail@thackbarth.net)
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

package net.thackbarth.sparrow.generator;

import net.thackbarth.sparrow.dto.MusicTrack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class generates the filename from the track information.
 */
public class FilenameByConfigGenerator implements FilenameGenerator {

    private List<FieldInformation> informationList = new LinkedList<FieldInformation>();

    public FilenameByConfigGenerator() {
        String bundleTag = "sparrow.";
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle("filename");
        } catch (MissingResourceException e) {
            bundle = ResourceBundle.getBundle("filename-default");
        }
        String order = bundle.getString(bundleTag + "order");
        for (String fieldName : order.split(",")) {
            String field = fieldName.trim();
            FieldInformation info = new FieldInformation();

            info.field = bundle.getString(bundleTag + field + ".field");

            // Default
            String defaultKey = bundleTag + field + ".default";
            if (bundle.containsKey(defaultKey)) {
                info.defaultValue = bundle.getString(defaultKey);
            }

            // group
            String groupKey = bundleTag + field + ".group";
            if (bundle.containsKey(groupKey)) {
                String groupValue = bundle.getString(groupKey);
                if ("true".equals(groupValue.toLowerCase())) {
                    info.mode = FieldMode.GROUP;
                }
            }

            // Numeric
            String numericKey = bundleTag + field + ".numeric";
            if (bundle.containsKey(numericKey)) {
                String numericValue = bundle.getString(numericKey);
                if ("true".equals(numericValue.toLowerCase())) {
                    info.mode = FieldMode.NUMERIC;
                }
                String numericLengthKey = numericKey + ".length";
                if (bundle.containsKey(numericLengthKey)) {
                    Integer numericLength = convertToNumeric(bundle.getString(numericLengthKey));
                    if (numericLength != null) {
                        info.numericLength = numericLength;
                    }
                }

            }

            // Prefix
            String prefixKey = bundleTag + field + ".prefix";
            if (bundle.containsKey(prefixKey)) {
                info.prefix = bundle.getString(prefixKey);
            }

            // Suffix
            String suffixKey = bundleTag + field + ".suffix";
            if (bundle.containsKey(suffixKey)) {
                info.suffix = bundle.getString(suffixKey);
            }

            // Values
            String valuesKey = bundleTag + field + ".values";
            if (bundle.containsKey(valuesKey)) {
                String fixedValues = bundle.getString(valuesKey);
                info.mode = FieldMode.VALUE;
                for (String valueOption : fixedValues.split(",")) {
                    String valueOpt = valueOption.trim();
                    if (bundle.containsKey(valuesKey + "." + valueOpt)) {
                        info.fixedValues.put(valueOpt,
                                bundle.getString(valuesKey + "." + valueOpt));
                    }
                }
            }

            informationList.add(info);
        }
    }

    @Override
    public String generateName(MusicTrack track) {
        Map<String, String> trackMap = convertToMap(track);
        StringBuilder nameBuilder = new StringBuilder();
        for (FieldInformation info : informationList) {
            processFieldInformation(info, trackMap, nameBuilder);
        }
        return nameBuilder.toString();
    }

    private void processFieldInformation(FieldInformation info, Map<String, String> track, StringBuilder sb) {
        if ((track.containsKey(info.field)) || (info.defaultValue != null)) {
            if (info.prefix != null) {
                sb.append(info.prefix);
            }
            if (track.containsKey(info.field)) {
                String value = track.get(info.field);
                switch (info.mode) {
                    case GROUP:
                        boolean startsWithStopWord = false;
                        if ((value != null) && (value.length() >= 5)) {
                            String ff = value.substring(0, 4).toLowerCase();
                            startsWithStopWord = (("der ".equals(ff))
                                    || ("die ".equals(ff))
                                    || ("das ".equals(ff))
                                    || ("the ".equals(ff)));
                        }
                        String valueString = clearString(value);
                        if (startsWithStopWord) {
                            sb.append(valueString.substring(3, 4).toUpperCase());
                        } else {
                            sb.append(valueString.substring(0, 1).toUpperCase());
                        }
                        if (info.prefix != null) {
                            sb.append(info.prefix);
                        }
                        if (valueString.length() > 0) {
                            sb.append(valueString);
                        } else {
                            sb.append(info.defaultValue);
                        }
                        break;
                    case NUMERIC:
                        if ((value != null) && (value.length() > 0)) {
                            Integer trackNumber = convertToNumeric(value);
                            if (trackNumber != null) {
                                if (info.numericLength == null) {
                                    sb.append(trackNumber.toString());
                                } else {
                                    sb.append(String.format("%0" + info.numericLength + "d", trackNumber));
                                }
                            }
                        } else {
                            if (info.numericLength == null) {
                                sb.append("0");
                            } else {
                                sb.append(String.format("%0" + info.numericLength + "d", 0));
                            }
                        }
                        break;
                    case VALUE:
                        if (info.fixedValues.containsKey(value)) {
                            sb.append(info.fixedValues.get(value));
                        } else {
                            sb.append(info.defaultValue);
                        }
                        break;
                    default:
                        if ((value != null) && (value.length() > 0) && (clearString(value) != null)) {
                            sb.append(clearString(value));
                        } else {
                            sb.append(info.defaultValue);
                        }
                }
            } else {
                if (info.defaultValue != null) {
                    sb.append(info.defaultValue);
                }
            }
            if (info.suffix != null) {
                sb.append(info.suffix);
            }
        }
    }

    private Map<String, String> convertToMap(MusicTrack track) {
        Map<String, String> result = new HashMap<String, String>();
        result.put("GENRE", track.getGenreDescription());
        result.put("ARTIST", track.getArtist());
        result.put("ALBUM", track.getAlbum());
        result.put("TRACK", track.getTrack());
        result.put("TITLE", track.getTitle());
        return result;
    }

    private Integer convertToNumeric(String value) {
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(value);
        Integer number = null;
        if (m.find()) {
            number = Integer.valueOf(m.group(1));
        }
        return number;
    }

    /**
     * This method removes all unwanted characters from a string.
     *
     * @param value the string to clean
     * @return the cleaned result
     */
    private String clearString(String value) {
        if (value == null) {
            return null;
        }
        String tempStr = value;
        tempStr = tempStr.replaceAll("ä", "ae");
        tempStr = tempStr.replaceAll("ö", "oe");
        tempStr = tempStr.replaceAll("ü", "ue");
        tempStr = tempStr.replaceAll("ß", "ss");
        tempStr = tempStr.replaceAll("Ä", "Ae");
        tempStr = tempStr.replaceAll("Ö", "Oe");
        tempStr = tempStr.replaceAll("Ü", "Ue");

        tempStr = getCamelCase(tempStr);

        tempStr = tempStr.replaceAll("[^a-zA-Z0-9]", "");

        return tempStr;
    }

    private String getCamelCase(String tempStr) {
        StringBuilder builder = new StringBuilder();
        String[] strings = tempStr.toLowerCase().split(" ");
        for (String part : strings) {
            // remove ( and ) for the part (Disc x)
            part = part.replaceAll("[\\(\\)]", "");
            if (part.length() > 0) {
                builder.append(part.substring(0, 1).toUpperCase());
            }
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase());
            }
        }
        return builder.toString();
    }


    private static enum FieldMode {
        DEFAULT, GROUP, NUMERIC, VALUE
    }

    private static class FieldInformation {

        FieldMode mode = FieldMode.DEFAULT;

        String defaultValue;

        String field;

        Map<String, String> fixedValues = new HashMap<String, String>();

        Integer numericLength;

        String prefix;

        String suffix;

    }
}
