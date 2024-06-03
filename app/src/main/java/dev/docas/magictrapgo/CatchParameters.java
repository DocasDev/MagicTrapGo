package dev.docas.magictrapgo;

public class CatchParameters {
    private int minIvStat;
    private int maxIvStat;
    private final int[] levels;

    public CatchParameters(int minIvStat, int maxIvStat, int[] levels) {
        this.minIvStat = minIvStat;
        this.maxIvStat = maxIvStat;
        this.levels = levels;

        normalize();
    }

    private void normalize(){
        if(minIvStat < 1)
            minIvStat = 1;

        if(minIvStat > 15)
            minIvStat = 15;

        if(maxIvStat < 0)
            maxIvStat = 1;

        if(maxIvStat > 15)
            maxIvStat = 15;

        if(minIvStat > maxIvStat)
            minIvStat = maxIvStat;
    }

    public int getMinIvStat() {
        return minIvStat;
    }

    public int getMaxIvStat() {
        return maxIvStat;
    }

    public int[] getLevels() {
        return levels;
    }
}
