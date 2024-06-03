package dev.docas.magictrapgo;

public class Pokemon {
    private final int id;
    private final String name;
    private final int attack;
    private final int defense;
    private final int stamina;
    private final String type;

    public Pokemon(int id, String name, int attack, int defense, int stamina, String type) {
        this.id = id;
        this.name = name;
        this.attack = attack;
        this.defense = defense;
        this.stamina = stamina;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getStamina() {
        return stamina;
    }

    public String getType() { return type; }
}
