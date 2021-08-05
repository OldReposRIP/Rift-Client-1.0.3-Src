package live.rift.module;

public enum Category {

    COMBAT("Combat"), EXPLOITS("Exploit"), MOVEMENT("Movement"), MISC("Misc"), RENDER("Render"), HUD("HUD");

    public String categoryName;

    private Category(String name) {
        this.categoryName = name;
    }

    public String toString() {
        return this.categoryName;
    }
}
