package com.example.notificationbatch.placeholder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class PlaceholderResolver {

    private static final Pattern PATTERN = Pattern.compile("\\{\\{\\s*(\\w+)\\s*\\}\\}");

    public String resolve(String template, List<Map.Entry<String, String>> payloadEntries) {
        if (template == null) return null;
        Map<String, String> map = new HashMap<>();
        if (payloadEntries != null) {
            for (Map.Entry<String, String> e : payloadEntries) map.put(e.getKey(), e.getValue());
        }
        Matcher m = PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            String replacement = map.getOrDefault(key, "");
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
