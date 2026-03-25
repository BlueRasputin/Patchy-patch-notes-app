package com.barrcon.patchy.dto;

public class PatchNoteSectionDTO {
    private String category;
    private String content;

    public PatchNoteSectionDTO() {
    }

    public PatchNoteSectionDTO(String category, String content) {
        this.category = category;
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
