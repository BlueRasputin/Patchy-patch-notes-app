package com.barrcon.patchy.services;

@Service
public class GeminiPatchNotesService implements AIPatchNotesService {
    private final GenerativeModel model;

    public GeminiPatchNotesService() {

    }

    @Override
    public ProcessedPatchNotesDTO processPatchNotes(String content, String version, String title) {

    }
}
