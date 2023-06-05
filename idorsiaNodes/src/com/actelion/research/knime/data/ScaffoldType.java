package com.actelion.research.knime.data;

public enum ScaffoldType {
    RING_SYSTEMS("Plain ring systems"),
    RING_SYSTEMS_SUBSTITUTION("Ring systems with substitution pattern"),
    RING_SYSTEMS_HETERO_SUBSTITUTION("Ring systems with carbon/hetero subst. pattern"),
    RING_SYSTEMS_ATOMIC_SUBSTITUTION("Ring systems with atomic-no subst. pattern"),
    MURCKO_SCAFFOLD("Murcko scaffold"),
    MURCKO_SKELETON("Murcko skeleton"),
    MOST_CENTRAL_RING_SYSTEM("Most central ring system");

    private String description;

    ScaffoldType(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

    public static ScaffoldType fromString(String s) {
        for (ScaffoldType scaffoldType : values()) {
            if (scaffoldType.description.equals(s)) {
                return scaffoldType;
            }
        }
        return null;
    }
}
