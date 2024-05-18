package dev.docas.magictrapgo;

public class Pokemon {
    private final int id;
    private final String name;
    private final int attack;
    private final int defense;
    private final int stamina;

    public Pokemon(int id, String name, int attack, int defense, int stamina) {
        this.id = id;
        this.name = name;
        this.attack = attack;
        this.defense = defense;
        this.stamina = stamina;
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
}
