package com.barrcon.patchy.dto;

public class PackageInsightMatchDTO {

    private String packageName;
    private String packageVersion;
    private String dependencyType;
    private PatchNoteResponseDTO patchNote;

    public PackageInsightMatchDTO(String packageName,
                                  String packageVersion,
                                  String dependencyType,
                                  PatchNoteResponseDTO patchNote) {
        this.packageName = packageName;
        this.packageVersion = packageVersion;
        this.dependencyType = dependencyType;
        this.patchNote = patchNote;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public String getDependencyType() {
        return dependencyType;
    }

    public PatchNoteResponseDTO getPatchNote() {
        return patchNote;
    }
}
