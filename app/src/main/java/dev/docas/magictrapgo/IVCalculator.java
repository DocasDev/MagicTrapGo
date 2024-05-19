package dev.docas.magictrapgo;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class IVCalculator {
    private final double[] cpMultipliers = {
            0.094, 0.16639787, 0.21573247, 0.25572005, 0.29024988, 0.3210876,
            0.34921268, 0.37523559, 0.39956728, 0.4225, 0.44310755, 0.46279839,
            0.48168495, 0.49985844, 0.51739395, 0.53435433, 0.55079269, 0.56675452,
            0.58227891, 0.5974, 0.61215729, 0.62656713, 0.64065295, 0.65443563,
            0.667934, 0.68116492, 0.69414365, 0.70688421, 0.71939909, 0.7317,
            0.73776948, 0.74378943, 0.74976104, 0.75568551, 0.76156384, 0.76739717,
            0.7731865, 0.77893275, 0.78463697, 0.7903
    };
    private final Pokemon pokemon;

    public IVCalculator(Pokemon pokemon){
        this.pokemon = pokemon;
    }

    public ArrayList<Double> discovery(int reqCP){
        return discovery(reqCP, 1, 1);
    }

    public ArrayList<Double> discovery(int reqCP, int minimalStat){
        return discovery(reqCP, minimalStat, 1);
    }

    public ArrayList<Double> discovery(int reqCP, int minimalStat, int minimalLevel){
        ArrayList<Double> ivs = new ArrayList<>();

        if(minimalStat < 1 )
            minimalStat = 1;

        if(minimalStat > 14)
            minimalStat = 14;

        if(minimalLevel < 1)
            minimalLevel = 1;

        if(minimalLevel > 35)
            minimalLevel = 35;

        for (int nivel = minimalLevel; nivel <= 40; nivel++) {
            double cpMultiplier = cpMultipliers[nivel - 1];
            for (int ivAttack = minimalStat; ivAttack <= 15; ivAttack++) {
                for (int ivDefense = minimalStat; ivDefense <= 15; ivDefense++) {
                    for (int ivStamina = minimalStat; ivStamina <= 15; ivStamina++) {
                        if(cpCalculate(ivAttack, ivDefense, ivStamina, cpMultiplier) != reqCP)
                            continue;

                        double iv = ivPercentCalculate(ivAttack, ivDefense, ivStamina);
                        if(ivs.contains(iv))
                            continue;

                        ivs.add(iv);
                    }
                }
            }
        }

        Collections.sort(ivs);

        return reduceToMinMax(ivs);
    }

    private ArrayList<Double> reduceToMinMax(ArrayList<Double> ivs){
        if(ivs.size() <= 1)
            return ivs;

        ArrayList<Double> reduced = new ArrayList<>();
        reduced.add(ivs.get(0));
        reduced.add(ivs.get(ivs.size() - 1));

        return reduced;
    }

    private ArrayList<String> removeDuplicate(ArrayList<String> ivs){
        Set<String> set = new LinkedHashSet<>(ivs);
        ivs.clear();
        ivs.addAll(set);

        return ivs;
    }

    private int cpCalculate(int ivAttack, int ivDefense, int ivStamina, double cpMultiplier){
        int attack = pokemon.getAttack() + ivAttack;
        int defense = pokemon.getDefense() + ivDefense;
        int stamina = pokemon.getStamina() + ivStamina;

        int cp = (int) Math.floor((attack * Math.pow(defense, .5f) * Math.pow(stamina, .5f) * Math.pow(cpMultiplier, 2)) / 10);

        return Math.max(cp, 10);
    }

    private int hpCalculate(int ivStamina, double cpMultiplier){
        return (int) Math.floor((pokemon.getStamina() + ivStamina) * cpMultiplier);
    }

    private  double ivPercentCalculate(int ivAttack, int ivDefense, int ivStamina){
        return (ivAttack + ivDefense + ivStamina) * 2.2222222f;
    }
}
