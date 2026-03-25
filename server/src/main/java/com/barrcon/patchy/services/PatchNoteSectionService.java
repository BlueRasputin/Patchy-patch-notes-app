package com.barrcon.patchy.services;

import com.barrcon.patchy.dto.PatchNoteSectionDTO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PatchNoteSectionService {

    public String serialize(List<PatchNoteSectionDTO> sections) {
        JSONArray jsonArray = new JSONArray();

        for (PatchNoteSectionDTO section : sections) {
            if (section.getCategory() == null || section.getCategory().isBlank()
                    || section.getContent() == null || section.getContent().isBlank()) {
                continue;
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("category", section.getCategory());
            jsonObject.put("content", section.getContent());
            jsonArray.put(jsonObject);
        }

        return jsonArray.toString();
    }

    public List<PatchNoteSectionDTO> parse(String rawSections) {
        if (rawSections == null || rawSections.isBlank()) {
            return List.of();
        }

        JSONArray jsonArray = new JSONArray(rawSections);
        List<PatchNoteSectionDTO> sections = new ArrayList<>();

        for (int index = 0; index < jsonArray.length(); index++) {
            JSONObject jsonObject = jsonArray.getJSONObject(index);
            sections.add(new PatchNoteSectionDTO(
                    jsonObject.optString("category"),
                    jsonObject.optString("content")
            ));
        }

        return sections;
    }
}
